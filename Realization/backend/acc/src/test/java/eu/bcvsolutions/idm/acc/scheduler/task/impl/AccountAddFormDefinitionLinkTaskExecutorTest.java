package eu.bcvsolutions.idm.acc.scheduler.task.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class AccountAddFormDefinitionLinkTaskExecutorTest extends AbstractIntegrationTest {
	
	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private LongRunningTaskManager longRunningTaskManager;
	
	@Test
	public void testCreateAccountFormValue() {
		helper.loginAdmin();
		// create system
		String systemName = helper.createName();
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME, systemName);
		helper.createMapping(system);
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system);
		//
		// create identity and its account
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityRole(identity, role);
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		accountFilter.setSystemId(system.getId());
		AccAccountDto account = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		IdmFormDefinitionDto formDefinition = DtoUtils.getEmbedded(account, AccAccount_.formDefinition, IdmFormDefinitionDto.class);
		// remove the form definition link
		account.setFormDefinition(null);
		account = accountService.saveInternal(account);
		assertNull(account.getFormDefinition());
		//
		// run the task
		AccountAddFormDefinitionLinkTaskExecutor taskExecutor = new AccountAddFormDefinitionLinkTaskExecutor();
		longRunningTaskManager.executeSync(taskExecutor);
		//
		account = accountService.get(account.getId());
		assertNotNull(account.getFormDefinition());
		IdmFormDefinitionDto formDefinitionRecreated = DtoUtils.getEmbedded(account, AccAccount_.formDefinition, IdmFormDefinitionDto.class);
		assertEquals(formDefinition.getId(), formDefinitionRecreated.getId());
		//
		formDefinitionService.delete(formDefinition);
		helper.logout();
	}
	
	@Test
	public void testCreateAccountFormValueSystemFilter() {
		helper.loginAdmin();
		// create system
		String systemName = helper.createName();
		SysSystemDto system = helper.createSystem(TestResource.TABLE_NAME, systemName);
		helper.createMapping(system);
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system);
		//
		// create identity and its account
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityRole(identity, role);
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		accountFilter.setSystemId(system.getId());
		AccAccountDto account = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		IdmFormDefinitionDto formDefinition = DtoUtils.getEmbedded(account, AccAccount_.formDefinition, IdmFormDefinitionDto.class);
		// remove the form definition link
		account.setFormDefinition(null);
		account = accountService.saveInternal(account);
		assertNull(account.getFormDefinition());
		//
		// run the task
		AccountAddFormDefinitionLinkTaskExecutor taskExecutor = new AccountAddFormDefinitionLinkTaskExecutor();
		Map<String, Object> properties = new HashMap<>();
		properties.put(AccountAddFormDefinitionLinkTaskExecutor.PARAM_SYSTEM, system.getId());
		taskExecutor.init(properties);
		longRunningTaskManager.executeSync(taskExecutor);
		//
		account = accountService.get(account.getId());
		assertNotNull(account.getFormDefinition());
		IdmFormDefinitionDto formDefinitionRecreated = DtoUtils.getEmbedded(account, AccAccount_.formDefinition, IdmFormDefinitionDto.class);
		assertEquals(formDefinition.getId(), formDefinitionRecreated.getId());
		//
		formDefinitionService.delete(formDefinition);
		helper.logout();
	}

}
