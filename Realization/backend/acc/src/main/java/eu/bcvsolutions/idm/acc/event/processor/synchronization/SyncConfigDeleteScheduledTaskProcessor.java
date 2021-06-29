package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.event.SyncConfigEvent.SyncConfigEventType;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;

/**
 * Delete scheduled task after synchronization configuration is deleted.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(SyncConfigDeleteScheduledTaskProcessor.PROCESSOR_NAME)
@Description("Delete scheduled task after synchronization configuration is deleted.")
public class SyncConfigDeleteScheduledTaskProcessor 
		extends CoreEventProcessor<AbstractSysSyncConfigDto> 
		implements SyncConfigProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncConfigDeleteScheduledTaskProcessor.class);
	public static final String PROCESSOR_NAME = "acc-sync-config-delete-scheduled-task-processor";
	//
	@Autowired private SchedulerManager schedulerManager;
	@Autowired private SynchronizationSchedulableTaskExecutor synchronizationSchedulableTaskExecutor;

	public SyncConfigDeleteScheduledTaskProcessor() {
		super(SyncConfigEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AbstractSysSyncConfigDto> process(EntityEvent<AbstractSysSyncConfigDto> event) {
		UUID synchronizationId = event.getContent().getId();
		Assert.notNull(synchronizationId, "Synchronization identifier is required.");
		//
		// find long running task
		TaskFilter filter = new TaskFilter();
		filter.setTaskType(AutowireHelper.getTargetType(synchronizationSchedulableTaskExecutor));
		schedulerManager
				.find(filter, null)
				.stream()
				.filter(task -> {
					return synchronizationId.toString().equals(
							task.getParameters().get(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID)
					);
				})
				.map(Task::getId)
				.forEach(taskId -> {
					schedulerManager.deleteTask(taskId);
					LOG.info("Scheduled task [{}] deleted (after synchronization configuration [{}] was deleted).",
							taskId, synchronizationId);
				});
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10; // ~ referential integrity
	}
}
