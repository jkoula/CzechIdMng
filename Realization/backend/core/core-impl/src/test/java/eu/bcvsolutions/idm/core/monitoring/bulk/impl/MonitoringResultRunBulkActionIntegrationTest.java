package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult;
import eu.bcvsolutions.idm.core.monitoring.service.impl.H2DatabaseMonitoringEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Run monitoring tests.
 * 
 * @author Radek Tomiška
 *
 */
public class MonitoringResultRunBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private IdmMonitoringResultService monitoringResultService;
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
		IdmMonitoringResultDto monitoringResult = createDto();
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmMonitoringResult.class, MonitoringResultRunBulkAction.NAME);
		
		Set<UUID> ids = Sets.newHashSet(monitoringResult.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		monitoringService.deleteById(monitoringResult.getMonitoring());
	}
	
	protected IdmMonitoringResultDto createDto() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring =  monitoringService.save(monitoring);
		//
		IdmMonitoringResultDto dto = new IdmMonitoringResultDto();
		dto.setMonitoring(monitoring.getId());
		dto.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		dto.setInstanceId(configurationService.getInstanceId());
		dto.setResult(new OperationResultDto(OperationState.BLOCKED));
		//
		return monitoringResultService.save(dto);
	}
}
