package eu.bcvsolutions.idm.core.monitoring.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.context.ApplicationContext;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.impl.DefaultIdmIdentityService;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Monitoring evaluator unit tests.
 * 
 * @author Radek Tomiška
 *
 */
public class DatabaseTableMonitoringEvaluatorUnitTest extends AbstractUnitTest {

	@Mock private ApplicationContext context;
	@Mock private DefaultIdmIdentityService identityService;
	//
	@InjectMocks private DatabaseTableMonitoringEvaluator evaluator;
	
	@Test
	public void testSuccess() {
		Mockito.when(context.getBean("mock")).thenReturn(identityService);
		Mockito.when(identityService.count(ArgumentMatchers.any())).thenReturn(DatabaseTableMonitoringEvaluator.DEFAULT_THRESHOLD);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		monitoring.getEvaluatorProperties().put(DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, "mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_DATABASE_TABLE.getCode(), result.getResult().getCode());
		Assert.assertNull(result.getLevel());
		Assert.assertEquals(String.valueOf(DatabaseTableMonitoringEvaluator.DEFAULT_THRESHOLD), result.getValue());
	}
	
	@Test
	public void testWarning() {
		Mockito.when(context.getBean("mock")).thenReturn(identityService);
		Mockito.when(identityService.count(ArgumentMatchers.any())).thenReturn(DatabaseTableMonitoringEvaluator.DEFAULT_THRESHOLD + 1);
		Mockito.when(identityService.getEntityClass()).thenReturn(IdmIdentity.class);
		Mockito.when(identityService.getDtoClass()).thenReturn(IdmIdentityDto.class);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		monitoring.getEvaluatorProperties().put(DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, "mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_DATABASE_TABLE.getCode(), result.getResult().getCode());
		Assert.assertEquals(NotificationLevel.WARNING, result.getLevel());
		Assert.assertEquals(String.valueOf(DatabaseTableMonitoringEvaluator.DEFAULT_THRESHOLD + 1), result.getValue());
	}
	
	@Test(expected = ResultCodeException.class)
	public void testBeanNotFoud() {
		Mockito.when(context.getBean("mock")).thenReturn(null);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, "mock");
		evaluator.evaluate(monitoring);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testBeanNotReadService() {
		Mockito.when(context.getBean("mock")).thenReturn(new IdmIdentityDto());
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, "mock");
		evaluator.evaluate(monitoring);
	}
	
	@Test(expected = ResultCodeException.class)
	public void testBeanException() {
		Mockito.when(context.getBean("mock")).thenThrow(new BeanNotOfRequiredTypeException("mock", IdmIdentityDto.class, IdmIdentityDto.class));
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.getEvaluatorProperties().put(DatabaseTableMonitoringEvaluator.PARAMETER_READ_SERVICE_BEAN_NAME, "mock");
		evaluator.evaluate(monitoring);
	}
}
