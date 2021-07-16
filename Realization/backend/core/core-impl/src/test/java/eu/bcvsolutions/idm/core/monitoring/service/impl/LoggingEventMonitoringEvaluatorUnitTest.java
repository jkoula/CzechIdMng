package eu.bcvsolutions.idm.core.monitoring.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Monitoring evaluator unit tests.
 * 
 * @author Radek Tomiška
 *
 */
public class LoggingEventMonitoringEvaluatorUnitTest extends AbstractUnitTest {

	@Mock private IdmLoggingEventService loggingEventService;
	//
	@InjectMocks private LoggingEventMonitoringEvaluator evaluator;
	
	@Test
	public void testError() {
		Mockito.when(loggingEventService.count(ArgumentMatchers.any())).thenReturn(10L);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_LOGGING_EVENT_ERROR.getCode(), result.getResult().getCode());
		Assert.assertNull(result.getLevel());
	}
}
