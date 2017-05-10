package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskDtoService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskDtoService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Schedulable task services (this services will be automatically available as scheduled tasks)
 * 
 * @author Radek Tomiška
 *
 */
public abstract class AbstractSchedulableTaskExecutor<V> extends AbstractLongRunningTaskExecutor<V> implements SchedulableTaskExecutor<V> {

	@Autowired
	protected SecurityService securityService;
	
	@Autowired
	protected IdmLongRunningTaskDtoService longRunningTaskService;
	
	@Autowired
	protected IdmScheduledTaskDtoService scheduledTaskService;
	
	private UUID scheduledTaskId;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// run as system - called from scheduler internally
		securityService.setSystemAuthentication();
		//
		// scheduled task is quartz reference to IdM entity
		IdmScheduledTaskDto taskDto = getScheduledTask(context);
		//
		// add task to queue only - quartz will start take care of the rest
		createIdmLongRunningTask(context, taskDto);
	}

	/**
	 * Returns universal task parameters. Don't forget to override this method additively.
	 */
	@Override
	public List<String> getParameterNames() {
		// any parameter for now
		return new ArrayList<>();
	}
	
	@Override
	public UUID getScheduledTaskId() {
		if (this.scheduledTaskId == null) {
			IdmScheduledTaskDto scheduledTask = scheduledTaskService
					.findByLongRunningTaskId(this.getLongRunningTaskId());
			this.scheduledTaskId = scheduledTask == null ? null : scheduledTask.getId(); 
		}
		return this.scheduledTaskId;
	}

	
	protected IdmScheduledTaskDto createIdmScheduledTask(String taskName) {
		IdmScheduledTaskDto t = new IdmScheduledTaskDto();
		t.setQuartzTaskName(taskName);
		t.setDryRun(false);
		return scheduledTaskService.save(t);
	}

	private IdmLongRunningTaskDto createIdmLongRunningTask(
			JobExecutionContext context, IdmScheduledTaskDto taskDto) {
		IdmLongRunningTaskDto longRunningTask = new IdmLongRunningTaskDto();
		longRunningTask.setTaskType(getClass().getCanonicalName());
		longRunningTask.setTaskDescription(context.getJobDetail().getDescription());
		longRunningTask.setTaskProperties(context.getMergedJobDataMap());
		longRunningTask.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		longRunningTask.setInstanceId(context.getMergedJobDataMap().getString(SchedulableTaskExecutor.PARAMETER_INSTANCE_ID));
		longRunningTask.setScheduledTask(taskDto.getId());
		//
		return longRunningTaskService.save(longRunningTask);
	}
	
	private IdmScheduledTaskDto getScheduledTask(JobExecutionContext context) {
		String taskName = context.getJobDetail().getKey().getName();
		IdmScheduledTaskDto dto = scheduledTaskService.findByQuartzTaskName(taskName);
		return dto == null ? createIdmScheduledTask(taskName) : dto;
	}

}
