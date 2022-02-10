package eu.bcvsolutions.idm.core.exception;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventDto;
import eu.bcvsolutions.idm.core.api.audit.dto.IdmLoggingEventExceptionDto;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventExceptionFilter;
import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventExceptionService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventPropertyService;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Component
class IdmDbAppenderTest extends AbstractIntegrationTest {



    @Autowired
    IdmLoggingEventService loggingEventService;

    @Autowired
    IdmLoggingEventPropertyService propertyService;

    @Autowired
    IdmLoggingEventExceptionService eventExceptionService;



    @Test
    void append() {

        final IdmDbAppender appender = new IdmDbAppender(loggingEventService, eventExceptionService, propertyService);

        LoggerContext context = new LoggerContext();

        final Logger logger = context.getLogger(IdmDbAppenderTest.class);

        final RuntimeException runtimeException = new RuntimeException();
        runtimeException.setStackTrace(new StackTraceElement[] {
               new StackTraceElement(
                       IdmDbAppenderTest.class.getCanonicalName(),
                       "test",
                       "file",
                       0
               )
        });

        ILoggingEvent loggingEvent = new LoggingEvent(IdmDbAppenderTest.class.getCanonicalName(),
                logger, Level.ERROR, "TEST", runtimeException, new String[] {});

        appender.append(loggingEvent);

        IdmLoggingEventFilter eventFilter = new IdmLoggingEventFilter();
        eventFilter.setLoggerName(IdmDbAppenderTest.class.getCanonicalName());
        final List<IdmLoggingEventDto> foundEvents = loggingEventService.find(eventFilter, null).getContent();

        Assert.assertEquals(1, foundEvents.size());

        final IdmLoggingEventDto loggingEventDto = foundEvents.get(0);

        Assert.assertEquals("TEST", loggingEventDto.getFormattedMessage());
        Assert.assertEquals(IdmDbAppenderTest.class.getCanonicalName(), loggingEventDto.getLoggerName());
        Assert.assertEquals("ERROR", loggingEventDto.getLevelString().name());

        IdmLoggingEventExceptionFilter loggingEventExceptionFilter = new IdmLoggingEventExceptionFilter();
        loggingEventExceptionFilter.setEvent((Long) loggingEventDto.getId());
        //
        final List<IdmLoggingEventExceptionDto> exceptions = eventExceptionService.find(loggingEventExceptionFilter, null).getContent();
        Assert.assertEquals(2, exceptions.size());

        IdmLoggingEventExceptionDto exceptionDto = exceptions.stream().filter(e -> ((long)e.getId()) == 1l).findFirst().get();

        Assert.assertTrue(exceptionDto.getTraceLine().contains("test"));
        Assert.assertTrue(exceptionDto.getTraceLine().contains("file"));
        Assert.assertTrue(exceptionDto.getTraceLine().contains("0"));

    }
}