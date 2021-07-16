package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test task configuration.
 * 
 * @author Radek Tomiška
 *
 */
public class ProcessAutomaticRoleByAttributeTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private ProcessAutomaticRoleByAttributeTaskExecutor executor;
	
	@Test
	public void testFormInstance() {
		ConfigurationMap properties = new ConfigurationMap();
		//
		Assert.assertNull(executor.getFormInstance(properties));
		//
		UUID automaticRoleId = UUID.randomUUID();
		properties.put(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, automaticRoleId);
		//
		IdmFormInstanceDto formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(automaticRoleId) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
		//
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(getHelper().createRole().getId());
		properties.put(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, automaticRole.getId());
		//
		formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(automaticRole.getId())
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
	}
}
