package eu.bcvsolutions.idm.core.exception;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.function.Consumer;

import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventPropertyService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.domain.LogType;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Implementation of log DBAppender. Only purpose is fix bugs in DBAppender (max message length, forbidden characters in Postgresql).
 *
 * @author Vít Švanda
 * @since 11.0.0
 */
@Component
public class IdmDbAppender extends AsyncAppender {

	public static final short PROPERTIES_EXIST = 0x01;
	public static final short EXCEPTION_EXISTS = 0x02;

	static final StackTraceElement EMPTY_CALLER_DATA = CallerData.naInstance();

	private final IdmLoggingEventService loggingEventService;
	private final IdmLoggingEventExceptionService loggingEventExceptionService;
	private final IdmLoggingEventPropertyService loggingEventPropertyService;

	@Override
	public void start() {
		System.out.println("QQQQQQQ starting db apender");
		super.start();
	}

	@PostConstruct
	public void init() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.getLoggerList().forEach(logger -> logger.addAppender(IdmDbAppender.this));
		setContext(context);
		start();
	}

	@Autowired
	public IdmDbAppender(IdmLoggingEventService loggingEventService, IdmLoggingEventExceptionService loggingEventExceptionService, IdmLoggingEventPropertyService loggingEventPropertyService) {
		this.loggingEventService = loggingEventService;
		this.loggingEventExceptionService = loggingEventExceptionService;
		this.loggingEventPropertyService = loggingEventPropertyService;
	}

	@Override
	protected void append(ILoggingEvent eventObject) {

		IdmLoggingEventDto loggingEventDto = new IdmLoggingEventDto();
		loggingEventDto.setTimestmp(eventObject.getTimeStamp());
		loggingEventDto.setFormattedMessage(eventObject.getFormattedMessage());
		loggingEventDto.setLoggerName(eventObject.getLoggerName());
		loggingEventDto.setLevelString(LogType.valueOf(eventObject.getLevel().toString()));
		loggingEventDto.setThreadName(eventObject.getThreadName());
		loggingEventDto.setReferenceFlag((int) computeReferenceMask(eventObject));

		loggingEventDto.setArg0(fixMessage(getArrayIndex(eventObject.getArgumentArray(), 0)));
		loggingEventDto.setArg1(fixMessage(getArrayIndex(eventObject.getArgumentArray(), 1)));
		loggingEventDto.setArg2(fixMessage(getArrayIndex(eventObject.getArgumentArray(), 2)));
		loggingEventDto.setArg3(fixMessage(getArrayIndex(eventObject.getArgumentArray(), 3)));

		final StackTraceElement stackTraceElement = extractFirstCaller(eventObject.getCallerData());

		loggingEventDto.setCallerClass(stackTraceElement.getClassName());
		loggingEventDto.setCallerFilename(stackTraceElement.getFileName());
		loggingEventDto.setCallerMethod(stackTraceElement.getMethodName());
		loggingEventDto.setCallerLine(String.valueOf(stackTraceElement.getLineNumber()));


		loggingEventService.save(loggingEventDto);
	}

	@Override
	public void doAppend(ILoggingEvent eventObject) {
		fixFormattedMessage(eventObject);
		IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
		while (throwableProxy != null) {
			fixMessage(throwableProxy);
			throwableProxy = throwableProxy.getCause();
		}
		super.doAppend(eventObject);
	}

	/**
	 * Fix message in DBAppender (max message length, forbidden characters in Postgresql).
	 */
	private void fixMessage(IThrowableProxy throwableProxy) {
		if (throwableProxy != null) {
			try {
				String message = throwableProxy.getMessage();
				String fixedMessage = fixMessage(message);
				if (message != null && !message.equals(fixedMessage)) {
					FieldUtils.writeField(throwableProxy, "message", fixedMessage, true);
				}
			} catch (IllegalAccessException e) {
				// System out is OK here.
				System.out.println(MessageFormat.format("IdmDbAppender error during fixing message: {0}", e.getMessage()));
			}
		}
	}

	/**
	 * Fix formatted message in DBAppender (max message length, forbidden characters in Postgresql).
	 */
	private void fixFormattedMessage(ILoggingEvent eventObject) {
		if (eventObject != null) {
			try {
				String formattedMessage = eventObject.getFormattedMessage();
				String fixedMessage = fixMessage(formattedMessage);
				if (formattedMessage != null && !formattedMessage.equals(fixedMessage)) {
					FieldUtils.writeField(eventObject, "formattedMessage", fixedMessage, true);
				}
				String message = eventObject.getMessage();
				fixedMessage = fixMessage(message);
				if (message != null && !message.equals(fixedMessage)) {
					FieldUtils.writeField(eventObject, "message", fixedMessage, true);
				}
			} catch (IllegalAccessException e) {
				// System out is OK here.
				System.out.println(MessageFormat.format("IdmDbAppender error during fixing message: {0}", e.getMessage()));
			}
		}
	}

	private String fixMessage(String message) {
		int maxLength = 200; // Only 200 because prefix is added lately.
		String fixedMessage = message;
		if (message != null
				&& (message.contains("\u0000")
				|| message.contains("\\x00")
				|| message.length() >= maxLength)) {
			// Workaround -> We have replace null characters by empty space, for case when exception will persisted in a Postgresql DB.
			fixedMessage = message.replace("\u0000", "").replace("\\x00", "");
			// Workaround for https://jira.qos.ch/browse/LOGBACK-493. -> DB tables has limitation for max 254 characters.
			if (fixedMessage.length() >= maxLength) {
				fixedMessage = fixedMessage.substring(0, maxLength - 1);
			}
		}
		return fixedMessage;
	}

	private short computeReferenceMask(ILoggingEvent event) {
		short mask = 0;

		int mdcPropSize = 0;
		if (event.getMDCPropertyMap() != null) {
			mdcPropSize = event.getMDCPropertyMap().keySet().size();
		}
		int contextPropSize = 0;
		if (event.getLoggerContextVO().getPropertyMap() != null) {
			contextPropSize = event.getLoggerContextVO().getPropertyMap().size();
		}

		if (mdcPropSize > 0 || contextPropSize > 0) {
			mask = PROPERTIES_EXIST;
		}
		if (event.getThrowableProxy() != null) {
			mask |= EXCEPTION_EXISTS;
		}
		return mask;
	}

	private String getArrayIndex(Object[] array, int index) {
		if (array == null || array.length <= index) {
			return null;
		}
		return String.valueOf(array[index]);
	}

	private StackTraceElement extractFirstCaller(StackTraceElement[] callerDataArray) {
		StackTraceElement caller = EMPTY_CALLER_DATA;
		if (hasAtLeastOneNonNullElement(callerDataArray))
			caller = callerDataArray[0];
		return caller;
	}

	private boolean hasAtLeastOneNonNullElement(StackTraceElement[] callerDataArray) {
		return callerDataArray != null && callerDataArray.length > 0 && callerDataArray[0] != null;
	}
}
