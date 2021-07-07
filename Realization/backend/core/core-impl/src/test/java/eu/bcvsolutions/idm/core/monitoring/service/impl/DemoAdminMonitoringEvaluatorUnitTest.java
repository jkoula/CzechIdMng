package eu.bcvsolutions.idm.core.monitoring.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitAdminIdentityProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Monitoring evaluator unit tests.
 * 
 * @author Radek Tomiška
 *
 */
public class DemoAdminMonitoringEvaluatorUnitTest extends AbstractUnitTest {

	@Mock private AuthenticationManager authenticationManager;
	@Mock private ApplicationConfiguration applicationConfiguration;
	@Mock private LookupService lookupService;
	//
	@InjectMocks private DemoAdminMonitoringEvaluator evaluator;
	
	@Test
	public void testDevelopmentWithDemoAdmin() {
		Mockito.when(applicationConfiguration.isDevelopment()).thenReturn(true);
		Mockito.when(lookupService.lookupDto(IdmIdentityDto.class, InitAdminIdentityProcessor.ADMIN_USERNAME)).thenReturn(new IdmIdentityDto(InitAdminIdentityProcessor.ADMIN_USERNAME));
		Mockito.when(authenticationManager.validate(ArgumentMatchers.any())).thenReturn(true);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_DEMO_ADMIN_WARNING.getCode(), result.getResult().getCode());
		Assert.assertNull(result.getLevel());
	}
	
	@Test
	public void testProductionWithDemoAdmin() {
		Mockito.when(applicationConfiguration.isDevelopment()).thenReturn(false);
		Mockito.when(lookupService.lookupDto(IdmIdentityDto.class, InitAdminIdentityProcessor.ADMIN_USERNAME)).thenReturn(new IdmIdentityDto(InitAdminIdentityProcessor.ADMIN_USERNAME));
		Mockito.when(authenticationManager.validate(ArgumentMatchers.any())).thenReturn(true);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_DEMO_ADMIN_WARNING.getCode(), result.getResult().getCode());
		Assert.assertEquals(NotificationLevel.ERROR, result.getLevel());
	}
	
	@Test
	public void testProductionWithoutDemoAdmin() {
		Mockito.when(lookupService.lookupDto(IdmIdentityDto.class, InitAdminIdentityProcessor.ADMIN_USERNAME)).thenReturn(null);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_DEMO_ADMIN_NOT_FOUND.getCode(), result.getResult().getCode());
	}
	
	@Test
	public void testProductionWithoutDemoAdminCredentials() {
		Mockito.when(lookupService.lookupDto(IdmIdentityDto.class, InitAdminIdentityProcessor.ADMIN_USERNAME)).thenReturn(new IdmIdentityDto(InitAdminIdentityProcessor.ADMIN_USERNAME));
		Mockito.when(authenticationManager.validate(ArgumentMatchers.any())).thenReturn(false);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.OK.getCode(), result.getResult().getCode());
	}
}
