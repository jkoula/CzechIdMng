package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncIdentityConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.scheduler.task.impl.SynchronizationSchedulableTaskExecutor;
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulerManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Delete scheduled task, after synchronization config is deleted.
 * 
 * @author Radek Tomiška
 *
 */
public class SyncConfigDeleteScheduledTaskProcessorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService schemaAttributeMappingService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SchedulerManager schedulerManager;
	
	@Test
	public void testDeleteScheduledTask() {
		SysSystemDto system = getHelper().createTestResourceSystem(true);
		
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(system.getId());
		List<SysSystemMappingDto> mappings = systemMappingService.find(mappingFilter, null).getContent();
		SysSystemMappingDto mapping = mappings.get(0);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mapping.getId());

		List<SysSystemAttributeMappingDto> attributes = schemaAttributeMappingService.find(attributeMappingFilter, null)
				.getContent();
		SysSystemAttributeMappingDto nameAttribute = attributes
				.stream()
				.filter(attribute -> {
					return attribute.getName().equals(TestHelper.ATTRIBUTE_MAPPING_NAME);
				})
				.findFirst()
				.get();
		
		// Create default synchronization config.
		AbstractSysSyncConfigDto syncConfigCustom = new SysSyncIdentityConfigDto();
		syncConfigCustom.setCustomFilter(true);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(nameAttribute.getId());
		syncConfigCustom.setName(getHelper().createName());
	
		AbstractSysSyncConfigDto syncConfig = syncConfigService.save(syncConfigCustom); 
		// schedule task
		Task task = createSyncTask(syncConfig.getId());
		Assert.assertNotNull(schedulerManager.getTask(task.getId()));
		// delete synchronization configuration
		syncConfigService.delete(syncConfig);
		//
		Assert.assertNull(schedulerManager.getTask(task.getId()));
	}
	
	private Task createSyncTask(UUID syncConfId) {
		Task task = new Task();
		task.setInstanceId(configurationService.getInstanceId());
		task.setTaskType(SynchronizationSchedulableTaskExecutor.class);
		task.setDescription("test");
		task.getParameters().put(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, syncConfId.toString());
		//
		return schedulerManager.createTask(task);
	}

	@Override
	protected TestHelper getHelper() {
		return helper;
	}
}
