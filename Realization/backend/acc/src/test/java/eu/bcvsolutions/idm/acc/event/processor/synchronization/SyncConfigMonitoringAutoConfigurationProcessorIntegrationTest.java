package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import java.util.List;

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
import eu.bcvsolutions.idm.acc.monitoring.SynchronizationMonitoringEvaluator;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Auto configure monitoring evaluator test.
 * 
 * @author Radek Tomiška
 *
 */
public class SyncConfigMonitoringAutoConfigurationProcessorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private SynchronizationMonitoringEvaluator synchronizationMonitoringEvaluator;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysSystemAttributeMappingService schemaAttributeMappingService;
	
	@Test
	public void testAutoConfigure() {
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
		
		// Create default synchronization config
		AbstractSysSyncConfigDto syncConfigCustom = new SysSyncIdentityConfigDto();
		syncConfigCustom.setCustomFilter(true);
		syncConfigCustom.setSystemMapping(mapping.getId());
		syncConfigCustom.setCorrelationAttribute(nameAttribute.getId());
		syncConfigCustom.setName(getHelper().createName());
	
		AbstractSysSyncConfigDto syncConfig = syncConfigService.save(syncConfigCustom); 
		
		String evaluatorType = AutowireHelper.getTargetType(synchronizationMonitoringEvaluator);
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setEvaluatorType(evaluatorType);
		IdmMonitoringDto monitoring = monitoringService
				.find(filter, null)
				.stream()
				.filter(m -> {
					return syncConfig.getId().equals(
							m.getEvaluatorProperties().get(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION)
					);
				})
				.findFirst()
				.orElse(null);
		
		Assert.assertNotNull(monitoring);
		
		syncConfigService.delete(syncConfig); 
		
		monitoring = monitoringService
				.find(filter, null)
				.stream()
				.filter(m -> {
					return syncConfig.getId().equals(
							m.getEvaluatorProperties().get(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION)
					);
				})
				.findFirst()
				.orElse(null);
		
		Assert.assertNull(monitoring);
	}
	
	@Override
	protected TestHelper getHelper() {
		return helper;
	}
}
