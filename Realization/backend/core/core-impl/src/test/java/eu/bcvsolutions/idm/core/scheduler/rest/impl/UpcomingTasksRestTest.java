package eu.bcvsolutions.idm.core.scheduler.rest.impl;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.ZonedDateTime;
import java.util.List;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.scheduler.ObserveLongRunningTaskEndProcessor;
import eu.bcvsolutions.idm.core.scheduler.api.dto.CronTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.DependentTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.SimpleTaskTrigger;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.TaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableTaskExecutor;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.TestSchedulableTask;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

public class UpcomingTasksRestTest extends AbstractRestTest {
	@Autowired
	private SchedulerManager manager;
	@Autowired
	SchedulerController schedulerController;

	@Test
	public void testOneTaskWithCronTrigger() {
		String instanceId = getHelper().createName();
		Task task = createTask(TestSchedulableTask.class, instanceId, "mock" + getHelper().createName());

		CronTaskTrigger cronTrigger = new CronTaskTrigger();
		cronTrigger.setTaskId(task.getId());
		cronTrigger.setCron("5 * * * * ?");
		manager.createTrigger(task.getId(), cronTrigger);

		TaskFilter filter = new TaskFilter();
		filter.setInstanceId(instanceId);
		List<Task> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(results.get(0).getTriggers());
		Assert.assertEquals(1, results.get(0).getTriggers().size());
	}

	@Test
	public void testOneTaskWithSimpleTrigger() {
		String instanceId = getHelper().createName();
		Task task = createTask(TestSchedulableTask.class, instanceId, "mock" + getHelper().createName());

		SimpleTaskTrigger trigger = new SimpleTaskTrigger();
		trigger.setTaskId(task.getId());
		trigger.setFireTime(ZonedDateTime.now().plusMinutes(5));
		manager.createTrigger(task.getId(), trigger);

		TaskFilter filter = new TaskFilter();
		filter.setInstanceId(instanceId);
		List<Task> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertNotNull(results.get(0).getTriggers());
		Assert.assertEquals(1, results.get(0).getTriggers().size());
	}

	@Test
	public void testTwoTasksCronAndDependent() {
		String instanceId = getHelper().createName();
		Task task = createTask(TestSchedulableTask.class, instanceId, "mock" + getHelper().createName());
		Task taskDependent = createTask(TestSchedulableTask.class, getHelper().createName(), "mock" + getHelper().createName());

		SimpleTaskTrigger simpleTaskTrigger = new SimpleTaskTrigger();
		simpleTaskTrigger.setFireTime(ZonedDateTime.now().plusMinutes(5));
		manager.createTrigger(task.getId(), simpleTaskTrigger);

		DependentTaskTrigger trigger = new DependentTaskTrigger();
		trigger.setInitiatorTaskId(task.getId());
		manager.createTrigger(taskDependent.getId(), trigger);

		TaskFilter filter = new TaskFilter();
		filter.setTaskType(TestSchedulableTask.class.getName());
		List<Task> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Task taskFetched = results.get(0);
		// only the cron task fetched, not the dependent
		// tasks without nextFireTime are not directly returned
		Assert.assertEquals(task.getId(), taskFetched.getId());
		Assert.assertNotNull(taskFetched.getTriggers());
		Assert.assertEquals(1, taskFetched.getTriggers().size());
		Assert.assertEquals(1, taskFetched.getDependentTasks().size());
		Assert.assertEquals(taskDependent.getId(), taskFetched.getDependentTasks().get(0).getId());
	}
	protected List<Task> find(TaskFilter filter) {
		MultiValueMap<String, String> queryParams = toQueryParams(filter);
		queryParams.set("size", "10000");
		//
		try {
			String response = getMockMvc().perform(get(BaseController.BASE_PATH + "/upcoming-tasks")
							.with(authentication(getAdminAuthentication()))
							.params(queryParams)
							.contentType(TestHelper.HAL_CONTENT_TYPE))
					.andExpect(status().isOk())
					.andExpect(content().contentType(TestHelper.HAL_CONTENT_TYPE))
					.andReturn()
					.getResponse()
					.getContentAsString();
			//
			return toDtos(response, Task.class);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to find entities", ex);
		}
	}

	private Task createTask(
			Class<? extends SchedulableTaskExecutor<?>> taskType,
			String instanceId,
			String description) {
		Task task = new Task();
		task.setInstanceId(instanceId);
		task.setTaskType(taskType);
		task.setDescription(description);
		task.getParameters().put(ObserveLongRunningTaskEndProcessor.RESULT_PROPERTY, "mock");
		//
		return manager.createTask(task);
	}
}
