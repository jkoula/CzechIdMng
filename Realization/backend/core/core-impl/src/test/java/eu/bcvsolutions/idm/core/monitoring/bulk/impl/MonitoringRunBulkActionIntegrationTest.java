package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoring;
import eu.bcvsolutions.idm.core.monitoring.service.impl.H2DatabaseMonitoringEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Run monitoring tests.
 * 
 * @author Radek Tomiška
 *
 */
public class MonitoringRunBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmMonitoringService service;
	@Autowired private ConfigurationService configurationService;
	@Autowired private H2DatabaseMonitoringEvaluator h2DatabaseMonitoringEvaluator;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testRun() {
		IdmMonitoringDto monitoring = createDto();
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmMonitoring.class, MonitoringRunBulkAction.NAME);
		
		Set<UUID> ids = Sets.newHashSet(monitoring.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		service.delete(monitoring);
	}
	
	protected IdmMonitoringDto createDto() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		//
		return service.save(monitoring);
	}
}
