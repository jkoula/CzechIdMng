package eu.bcvsolutions.idm.core.bulk.action.impl.script;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmScriptService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ExecuteScriptTaskExecutor;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Delete scheduled task, after script is deleted.
 * 
 * @author Radek Tomiška
 *
 */
public class ScriptDeleteScheduledTaskProcessorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private IdmScriptService scriptService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SchedulerManager schedulerManager;
	
	@Test
	public void testDeleteScheduledTaskById() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCode(getHelper().createName());
		script.setName(getHelper().createName());
		script.setCategory(IdmScriptCategory.DEFAULT);
		//
		script = scriptService.save(script); 
		// schedule task
		Task task = createScriptTask(script.getId());
		Assert.assertNotNull(schedulerManager.getTask(task.getId()));
		// delete synchronization configuration
		scriptService.delete(script);
		//
		Assert.assertNull(schedulerManager.getTask(task.getId()));
	}
	
	@Test
	public void testDeleteScheduledTaskByCode() {
		IdmScriptDto script = new IdmScriptDto();
		script.setCode(getHelper().createName());
		script.setName(getHelper().createName());
		script.setCategory(IdmScriptCategory.DEFAULT);
		//
		script = scriptService.save(script); 
		// schedule task
		Task task = createScriptTask(script.getCode());
		Assert.assertNotNull(schedulerManager.getTask(task.getId()));
		// delete synchronization configuration
		scriptService.delete(script);
		//
		Assert.assertNull(schedulerManager.getTask(task.getId()));
	}
	
	private Task createScriptTask(Serializable scriptId) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(ExecuteScriptTaskExecutor.class);
		task.setDescription("test");
		task.getParameters().put(ExecuteScriptTaskExecutor.PARAMETER_SCRIPT_CODE, scriptId.toString());
		//
		return schedulerManager.createTask(task);
	}
}
