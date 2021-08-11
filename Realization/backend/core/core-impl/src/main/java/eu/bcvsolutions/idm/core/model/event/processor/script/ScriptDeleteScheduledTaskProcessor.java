package eu.bcvsolutions.idm.core.model.event.processor.script;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ScriptProcessor;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.ScriptEvent.ScriptEventType;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ExecuteScriptTaskExecutor;

/**
 * Delete scheduled task after script is deleted.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(ScriptDeleteScheduledTaskProcessor.PROCESSOR_NAME)
@Description("Delete scheduled task after script is deleted.")
public class ScriptDeleteScheduledTaskProcessor 
		extends CoreEventProcessor<IdmScriptDto> 
		implements ScriptProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ScriptDeleteScheduledTaskProcessor.class);
	public static final String PROCESSOR_NAME = "core-script-delete-scheduled-task-processor";
	//
	@Autowired private SchedulerManager schedulerManager;
	@Autowired private ExecuteScriptTaskExecutor executeScriptTaskExecutor;

	public ScriptDeleteScheduledTaskProcessor() {
		super(ScriptEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmScriptDto> process(EntityEvent<IdmScriptDto> event) {
		UUID scriptId = event.getContent().getId();
		String scriptCode = event.getContent().getCode();
		Assert.notNull(scriptId, "Script uuid identifier is required.");
		Assert.notNull(scriptCode, "Script codeable identifier is required.");
		//
		// find long running task
		TaskFilter filter = new TaskFilter();
		filter.setTaskType(AutowireHelper.getTargetType(executeScriptTaskExecutor));
		schedulerManager
				.find(filter, null)
				.stream()
				.filter(task -> {
					String scriptCodeableIdentifier = task.getParameters().get(ExecuteScriptTaskExecutor.PARAMETER_SCRIPT_CODE);
					//
					return scriptId.toString().equals(scriptCodeableIdentifier)
							|| scriptCode.toString().equals(scriptCodeableIdentifier);
				})
				.map(Task::getId)
				.forEach(taskId -> {
					schedulerManager.deleteTask(taskId);
					LOG.info("Scheduled task [{}] deleted (after script [{}] was deleted).",
							taskId, scriptId);
				});
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10; // ~ referential integrity
	}
}
