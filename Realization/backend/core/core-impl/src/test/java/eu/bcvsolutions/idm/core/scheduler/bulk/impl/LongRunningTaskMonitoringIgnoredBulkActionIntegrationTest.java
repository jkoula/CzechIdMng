package eu.bcvsolutions.idm.core.scheduler.bulk.impl;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.task.impl.TestTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractMonitoringIgnoredBulkActionIntegrationTest;

/**
 * Test for bulk actions which created monitoring ignored flag.
 * 
 * @author Radek Tomiška
 */
public class LongRunningTaskMonitoringIgnoredBulkActionIntegrationTest extends AbstractMonitoringIgnoredBulkActionIntegrationTest<IdmLongRunningTaskDto> {

	@Autowired private IdmLongRunningTaskService service;
	@Autowired private LongRunningTaskMonitoringIgnoredBulkAction bulkAction;
	
	@Override
	protected AbstractBulkAction<IdmLongRunningTaskDto, ?> getBulkAction() {
		return bulkAction;
	}
	
	@Override
	protected IdmLongRunningTaskDto createDto() {
		TestTaskExecutor taskExecutor = new TestTaskExecutor(); 
		IdmLongRunningTaskDto task = new IdmLongRunningTaskDto();
		task.setTaskType(taskExecutor.getClass().getCanonicalName());
		task.setTaskProperties(taskExecutor.getProperties());
		task.setTaskDescription(taskExecutor.getDescription());	
		task.setInstanceId("mock");
		task.setResult(new OperationResult.Builder(OperationState.BLOCKED).build());
		//
		return service.save(task);
	}
}
