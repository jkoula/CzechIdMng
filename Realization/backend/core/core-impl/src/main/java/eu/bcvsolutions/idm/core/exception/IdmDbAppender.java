package eu.bcvsolutions.idm.core.exception;

import ch.qos.logback.classic.spi.*;

import java.text.MessageFormat;
import java.util.Map;
import java.util.stream.Stream;

import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.CoreConstants;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventPropertyDto;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventPropertyService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.domain.LogType;
import eu.bcvsolutions.idm.core.config.ApplicationContextHolder;
import org.apache.commons.lang3.reflect.FieldUtils;

import org.springframework.context.ApplicationContext;


/**
 * Since version 1.2.10 of Logback, there is no support for database logging. For this reason, this class was implemented.
 * Some parts of the logic are taken from the original DbAppender class. {@link IdmLoggingEventService}, {@link IdmLoggingEventExceptionService} and
 * {@link IdmLoggingEventPropertyService} are used for saving logs to database. Besides that, tha class also fixes some
 * bugs regarding max message length and forbidden characters in Postgresql.
 *
 * @author Vít Švanda
 * @author Peter Štrunc <github.com/peter-strunc>
 * @since 11.0.0
 */
public class IdmDbAppender extends AppenderBase<ILoggingEvent> {

	public static final short PROPERTIES_EXIST = 0x01;
	public static final short EXCEPTION_EXISTS = 0x02;

	static final StackTraceElement EMPTY_CALLER_DATA = CallerData.naInstance();

	private IdmLoggingEventService loggingEventService;
	private IdmLoggingEventExceptionService loggingEventExceptionService;
	private IdmLoggingEventPropertyService loggingEventPropertyService;

	/**
	 * Initializes this appender. Because Logback creates this appender before Springs {@link ApplicationContext} is initialized,
	 * we need to do autowiring ourselves, when the initialization is done. See {@link ApplicationContextHolder}
	 *
	 * @return true, is this appender is initialized. Only when all services have been autowired, this method should return true.
	 */
	public boolean init() {
		if (loggingEventService != null && loggingEventExceptionService != null && loggingEventPropertyService != null) {
			return true;
		}
		//
		this.loggingEventService = getBean(IdmLoggingEventService.class);
		this.loggingEventPropertyService = getBean(IdmLoggingEventPropertyService.class);
		this.loggingEventExceptionService = getBean(IdmLoggingEventExceptionService.class);
		//
		return loggingEventService != null && loggingEventExceptionService != null && loggingEventPropertyService != null;
	}

	/**
	 * Uses {@link ApplicationContextHolder} to obtain reference to the bean of the given {@link Class}. If application
	 * context has been initialized, then this method uses {@link ApplicationContext#getBean(Class)} to obtain an
	 * instance of the bean.
	 *
	 * @param clazz {@link Class} of the desired bean.
	 * @param <E> Type parameter of desired class. Any subclass of {@link Object} is supported
	 * @return An instance of spring bean
	 */
	private <E> E getBean(Class<E> clazz) {
		if (!ApplicationContextHolder.hasApplicationContext()) {
			// Context has not been yet initialized
			return null;
		}
		return ApplicationContextHolder.getApplicationContext().getBean(clazz);
	}

	/**
	 * Empty constructor used by Logback to construct this appender
	 */
	public IdmDbAppender() {

	}

	/**
	 * Mainly for testing purposes. Allows for manual initialization of this appender.
	 *
	 * @param loggingEventService {@link IdmLoggingEventService}
	 * @param loggingEventExceptionService {@link IdmLoggingEventExceptionService}
	 * @param loggingEventPropertyService {@link IdmLoggingEventPropertyService}
	 */
	public IdmDbAppender(IdmLoggingEventService loggingEventService, IdmLoggingEventExceptionService loggingEventExceptionService,
						 IdmLoggingEventPropertyService loggingEventPropertyService) {
		this.loggingEventService = loggingEventService;
		this.loggingEventExceptionService = loggingEventExceptionService;
		this.loggingEventPropertyService = loggingEventPropertyService;
	}

	@Override
	protected void append(ILoggingEvent eventObject) {

		// If ApplicationContext has not been yet initialied, this appender won't append
		if (!init()) {
			return;
		}

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

		loggingEventDto = loggingEventService.save(loggingEventDto);

		processProperties(eventObject, loggingEventDto);
		insertThrowable(eventObject, loggingEventDto);
	}

	protected void insertThrowable(ILoggingEvent eventObject, IdmLoggingEventDto loggingEventDto) {
		final long eventId = (long) loggingEventDto.getId();

		IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
		long baseIndex = 0;
		while (throwableProxy != null) {
			baseIndex = processError(throwableProxy, baseIndex, eventId);
			throwableProxy = throwableProxy.getCause();
		}
	}

	/**
	 *
	 * This method is mostly taken from Logback-1.2.3
	 *
	 * @param throwableProxy {@link IThrowableProxy} must not be null.
	 * @param eventId {@link Long} identifier autogenerated from database. Must not be null.
	 */
	private long processError(IThrowableProxy throwableProxy, long baseIndex, long eventId) {

		StringBuilder buf = new StringBuilder();

		ThrowableProxyUtil.subjoinFirstLine(buf, throwableProxy);
		saveStacktraceLine(buf.toString(), baseIndex++, eventId);

		final int commonFrames = throwableProxy.getCommonFrames();
		final StackTraceElementProxy[] stepArray = throwableProxy.getStackTraceElementProxyArray();
		for (int i = 0; i < stepArray.length - commonFrames; i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(CoreConstants.TAB);
			ThrowableProxyUtil.subjoinSTEP(sb, stepArray[i]);
			saveStacktraceLine(sb.toString(), baseIndex++, eventId);
		}
		if (commonFrames > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(CoreConstants.TAB).append("... ").append(commonFrames).append(" common frames omitted");
			saveStacktraceLine(sb.toString(), baseIndex++, eventId);
		}

		return baseIndex;
	}

	/**
	 * Saves one line of stack trace related to the given event.
	 *
	 * @param traceLine One line of stacktrace
	 * @param baseIndex line number of stacktrace
	 * @param eventId {@link Long} autogenerated id of related event
	 */
	private void saveStacktraceLine(String traceLine, long baseIndex, long eventId) {
		IdmLoggingEventExceptionDto dto = new IdmLoggingEventExceptionDto();
		dto.setEvent(eventId);
		dto.setTraceLine(traceLine);
		dto.setId(baseIndex);
		final IdmLoggingEventExceptionDto save = loggingEventExceptionService.save(dto);
	}


	/**
	 * Saves all properties related to given event
	 *
	 * @param event Original event data
	 * @param loggingEventDto Saved {@link IdmLoggingEventDto}
	 */
	private void processProperties(ILoggingEvent event, IdmLoggingEventDto loggingEventDto) {
		Map<String, String> loggerContextMap = event.getLoggerContextVO().getPropertyMap();
		Map<String, String> mdcMap = event.getMDCPropertyMap();
		Stream.concat(
				loggerContextMap.entrySet().stream(),
				mdcMap.entrySet().stream()
		).forEach(property -> saveProperty(loggingEventDto, property));
	}

	/**
	 * Saves single property to database
	 *
	 * @param event Saved event (must have non null identifier)
	 * @param property Property to save
	 */
	private void saveProperty(IdmLoggingEventDto event, Map.Entry<String, String> property) {
		IdmLoggingEventPropertyDto dto = new IdmLoggingEventPropertyDto();
		dto.setEventId((Long) event.getId());
		dto.setMappedKey(property.getKey());
		dto.setMappedValue(property.getValue());
		loggingEventPropertyService.save(dto);
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

	/**
	 * Taken from original DbAppender
	 * @param event Original event data
	 * @return PROPERTIES_EXIST or EXCEPTION_EXISTS constants
	 */
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
