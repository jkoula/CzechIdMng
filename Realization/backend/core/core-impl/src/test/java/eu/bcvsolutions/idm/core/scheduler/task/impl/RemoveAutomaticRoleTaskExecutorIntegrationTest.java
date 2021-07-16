package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test task configuration.
 * 
 * @author Radek Tomiška
 *
 */
public class RemoveAutomaticRoleTaskExecutorIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private RemoveAutomaticRoleTaskExecutor executor;
	
	@Test
	public void testFormInstance() {
		ConfigurationMap properties = new ConfigurationMap();
		//
		Assert.assertNull(executor.getFormInstance(properties));
		//
		UUID automaticRoleId = UUID.randomUUID();
		properties.put(RemoveAutomaticRoleTaskExecutor.PARAMETER_AUTOMATIC_ROLE_ATTRIBUTE, automaticRoleId);
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
		properties.put(RemoveAutomaticRoleTaskExecutor.PARAMETER_AUTOMATIC_ROLE_TREE, automaticRoleId);
		formInstance = executor.getFormInstance(properties);
		Assert.assertNotNull(formInstance);
		Assert.assertNotNull(formInstance
				.getValues()
				.stream()
				.anyMatch(
						v -> v.getUuidValue().equals(automaticRoleId) 
							&& v.getEmbedded().get(IdmFormValueDto.PROPERTY_UUID_VALUE) != null)
				);
		
	}
}
