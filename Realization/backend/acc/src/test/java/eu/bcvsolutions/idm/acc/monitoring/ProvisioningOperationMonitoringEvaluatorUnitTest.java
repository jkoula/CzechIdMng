package eu.bcvsolutions.idm.acc.monitoring;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.service.impl.AbstractDailyMonitoringEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Monitoring evaluator unit tests.
 * 
 * @author Radek Tomiška
 *
 */
public class ProvisioningOperationMonitoringEvaluatorUnitTest extends AbstractUnitTest {

	@Mock private SysProvisioningOperationService provisioningOperationService;
	//
	@InjectMocks private ProvisioningOperationMonitoringEvaluator evaluator;
	
	@Test
	public void testError() {
		Mockito.when(provisioningOperationService.count(ArgumentMatchers.any())).thenReturn(10L);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_PROVISIONING_OPERATION_ERROR.getCode(), result.getResult().getCode());
		Assert.assertNull(result.getLevel());
	}
	
	@Test
	public void testErrorWithNumberOfDays() {
		Mockito.when(provisioningOperationService.count(ArgumentMatchers.any())).thenReturn(10L);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		monitoring.getEvaluatorProperties().put(AbstractDailyMonitoringEvaluator.PARAMETER_NUMBER_OF_DAYS, 2);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(AccResultCode.MONITORING_PROVISIONING_OPERATION_ERROR.getCode(), result.getResult().getCode());
		Assert.assertNull(result.getLevel());
	}
	
	@Test
	public void testOk() {
		Mockito.when(provisioningOperationService.count(ArgumentMatchers.any())).thenReturn(0L);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		monitoring.getEvaluatorProperties().put(AbstractDailyMonitoringEvaluator.PARAMETER_NUMBER_OF_DAYS, 2);
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.OK.getCode(), result.getResult().getCode());
	}
}
