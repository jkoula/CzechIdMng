package eu.bcvsolutions.idm.core.monitoring.service.impl;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.config.flyway.IdmFlywayMigrationStrategy;
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
public class H2DatabaseMonitoringEvaluatorUnitTest extends AbstractUnitTest {

	@Mock private DataSource dataSource;
	@Mock private IdmFlywayMigrationStrategy flywayMigrationStrategy;
	@Mock private ApplicationConfiguration applicationConfiguration;
	//
	@InjectMocks private H2DatabaseMonitoringEvaluator evaluator;
	
	@Test
	public void testDevelopmentWithH2() {
		Mockito.when(applicationConfiguration.isProduction()).thenReturn(false);
		Mockito.when(applicationConfiguration.getStage()).thenReturn(ApplicationConfiguration.STAGE_DEVELOPMENT);
		Mockito.when(flywayMigrationStrategy.resolveDbName(dataSource)).thenReturn(IdmFlywayMigrationStrategy.H2_DBNAME);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_H2_DATABASE_WARNING.getCode(), result.getResult().getCode());
		Assert.assertEquals(IdmFlywayMigrationStrategy.H2_DBNAME, result.getValue());
	}
	
	@Test
	public void testProductionWithH2() {
		Mockito.when(applicationConfiguration.isProduction()).thenReturn(true);
		Mockito.when(flywayMigrationStrategy.resolveDbName(dataSource)).thenReturn(IdmFlywayMigrationStrategy.H2_DBNAME);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_H2_DATABASE_ERROR.getCode(), result.getResult().getCode());
		Assert.assertEquals(IdmFlywayMigrationStrategy.H2_DBNAME, result.getValue());
	}
	
	@Test
	public void testSuccess() {
		Mockito.when(flywayMigrationStrategy.resolveDbName(dataSource)).thenReturn(IdmFlywayMigrationStrategy.POSTGRESQL_DBNAME);
		//
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setInstanceId("mock");
		IdmMonitoringResultDto result = evaluator.evaluate(monitoring);
		//
		Assert.assertEquals(CoreResultCode.MONITORING_H2_DATABASE_SUCCESS.getCode(), result.getResult().getCode());
		Assert.assertEquals(IdmFlywayMigrationStrategy.POSTGRESQL_DBNAME, result.getValue());
	}
}
