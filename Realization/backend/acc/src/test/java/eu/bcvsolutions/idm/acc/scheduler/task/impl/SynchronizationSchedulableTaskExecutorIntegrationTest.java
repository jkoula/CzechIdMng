package eu.bcvsolutions.idm.acc.scheduler.task.impl;

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
import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test task configuration.
 * 
 * @author Radek Tomiška
 */
public class SynchronizationSchedulableTaskExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private SynchronizationSchedulableTaskExecutor executor;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService schemaAttributeMappingService;
	
	@Test
	public void testFormInstance() {
		ConfigurationMap properties = new ConfigurationMap();
		//
		Assert.assertNull(executor.getFormInstance(properties));
		//
		UUID synchronizationId = UUID.randomUUID();
		properties.put(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, synchronizationId);
		//
		IdmFormInstanceDto formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(synchronizationId) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
		//
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
		properties.put(SynchronizationService.PARAMETER_SYNCHRONIZATION_ID, syncConfig.getId());
		//
		formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(syncConfig.getId()) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
	}
	
	@Override
	protected eu.bcvsolutions.idm.acc.TestHelper getHelper() {
		return (eu.bcvsolutions.idm.acc.TestHelper) super.getHelper();
	}
}
