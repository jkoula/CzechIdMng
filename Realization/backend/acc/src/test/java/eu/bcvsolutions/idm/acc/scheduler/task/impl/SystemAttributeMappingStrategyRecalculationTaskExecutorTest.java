package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for SystemAttributeMappingStrategyRecalculationTaskExecutorTest
 *
 * @author Roman Kucera
 */
@Transactional
public class SystemAttributeMappingStrategyRecalculationTaskExecutorTest extends AbstractIntegrationTest {

	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;

	@Test
	public void testLrt() {
		SystemAttributeMappingStrategyRecalculationTaskExecutor recalculationTaskExecutor = AutowireHelper.createBean(SystemAttributeMappingStrategyRecalculationTaskExecutor.class);

		SysSystemDto system = helper.createTestResourceSystem(true, helper.createName());
		IdmRoleDto role = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(role, system);

		assertNotNull(system);

		SysSystemMappingDto provisioningMapping = systemMappingService.findProvisioningMapping(system.getId(), SystemEntityType.IDENTITY);

		assertNotNull(provisioningMapping);

		List<SysSystemAttributeMappingDto> attributeMappingDtos = systemAttributeMappingService.findBySystemMapping(provisioningMapping);

		SysSystemAttributeMappingDto firstNameAttr = attributeMappingDtos.stream()
				.filter(sysSystemAttributeMappingDto -> sysSystemAttributeMappingDto.getName().equals(TestHelper.ATTRIBUTE_MAPPING_FIRSTNAME))
				.findFirst()
				.orElse(null);

		assertNotNull(firstNameAttr);

		SysRoleSystemAttributeDto roleSystemAttributeDto = new SysRoleSystemAttributeDto();
		roleSystemAttributeDto.setStrategyType(AttributeMappingStrategyType.MERGE);
		roleSystemAttributeDto.setRoleSystem(roleSystem.getId());
		roleSystemAttributeDto.setName(firstNameAttr.getName());
		roleSystemAttributeDto.setSystemAttributeMapping(firstNameAttr.getId());
		roleSystemAttributeDto.setEntityAttribute(firstNameAttr.isEntityAttribute());
		roleSystemAttributeDto.setExtendedAttribute(false);
		roleSystemAttributeDto.setSchemaAttribute(firstNameAttr.getSchemaAttribute());
		roleSystemAttributeDto.setIdmPropertyName(firstNameAttr.getIdmPropertyName());
		roleSystemAttributeDto = roleSystemAttributeService.save(roleSystemAttributeDto);

		assertNotNull(roleSystemAttributeDto.getId());

		Map<String, Object> properties = new HashMap<>();
		properties.put(ParameterConverter.PARAMETER_MAPPING_ATTRIBUTES, "{ \"system\": \"" + system.getId() +
				"\", \"systemMapping\": \"" + provisioningMapping.getId() +
				"\", \"mappingAttributes\": [ \"" + firstNameAttr.getId() + "\" ] }");
		recalculationTaskExecutor.init(properties);
		longRunningTaskManager.executeSync(recalculationTaskExecutor);

		roleSystemAttributeDto = roleSystemAttributeService.get(roleSystemAttributeDto.getId());
		assertEquals(firstNameAttr.getStrategyType(), roleSystemAttributeDto.getStrategyType());
	}
}