package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.collections.Sets;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for {@link AccountStopManagingBulkAction}.
 * 
 * @author Tomáš Doischer
 *
 */
public class AccountStopManagingBulkActionTest extends AbstractBulkActionTest {

	private static final String IDENTITY_USERNAME = "AccountStopManagingBulkActionTestUser";
	private static final String IDENTITY_USERNAME2 = "AccountStopManagingBulkActionTestUser2";
	private static final String ROLE_DEFAULT = "AccountStopManagingBulkActionTestUserRole";
	private static final String ROLE_DEFAULT2 = "AccountStopManagingBulkActionTestUserRole2";
	private static final String SYSTEM_NAME = "AccountStopManagingBulkActionTestUserSystem";
	private static final String SYSTEM_NAME2 = "AccountStopManagingBulkActionTestUserSystem2";

	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysSystemAttributeMappingService schemaAttributeHandlingService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	
	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void defaultAccountRemove() {
		initData(SYSTEM_NAME, IDENTITY_USERNAME, ROLE_DEFAULT, false);
		//
		testRunBulkAction(IDENTITY_USERNAME, ROLE_DEFAULT);
	}

	@Test
	public void protectedAccountRemove() {
		initData(SYSTEM_NAME2, IDENTITY_USERNAME2, ROLE_DEFAULT2,true);
		//
		testRunBulkAction(IDENTITY_USERNAME2, ROLE_DEFAULT2);
	}

	private void testRunBulkAction(String identityName, String roleName) {
		IdmIdentityDto identity = identityService.getByUsername(identityName);
		IdmRoleDto roleDefault = roleService.getByCode(roleName);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				helper.findResource("x" + identityName));

		IdmIdentityRoleDto irdto = new IdmIdentityRoleDto();
		irdto.setIdentityContract(identityContractService.findAllByIdentity(identity.getId()).get(0).getId());
		irdto.setRole(roleDefault.getId());
		// Set valid from to future
		irdto.setValidFrom(LocalDate.now().plusDays(1));
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		irdto = identityRoleService.save(irdto);

		AccIdentityAccountFilter iaccFilter = new AccIdentityAccountFilter();
		iaccFilter.setIdentityId(identity.getId());
		iaccFilter.setIdentityRoleId(irdto.getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		// Identity-account have to not exists after account management was started (INVALID identityRole was added)!
		Assert.assertEquals(0, identityAccounts.size());

		// Set valid from to null - Account must be created
		irdto.setValidFrom(null);
		// This evokes IdentityRole SAVE event. On this event will be start
		// account management and provisioning
		identityRoleService.save(irdto);
		//
		TestResource createdAccount = helper.findResource("x" + identityName);
		Assert.assertNotNull("Idenitity have to exist on target system",
				createdAccount);

		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid("x" + identityName);
		List<UUID> accountIds = accountService.findIds(accountFilter, null).getContent();
		Assert.assertEquals("Account needs to be created", 1,
				accountIds.size());
		final UUID accaccUuid = accountIds.get(0);
		// run the bulk action
		IdmBulkActionDto bulkAction = this.findBulkAction(AccAccount.class, AccountStopManagingBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(accountIds));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		createdAccount = helper.findResource("x" + identityName);
		Assert.assertNotNull("Identity must still exist on target system", createdAccount);
		accountIds = accountService.findIds(accountFilter, null).getContent();
		Assert.assertTrue("Account in IdM must be deleted now", accountIds.isEmpty());

		AccIdentityAccountFilter iaccFilter2 = new AccIdentityAccountFilter();
		iaccFilter2.setIdentityId(identity.getId());
		identityAccounts = identityAccountService.find(iaccFilter2, null).getContent();
		Assert.assertTrue("Account in IdM must be deleted now", identityAccounts.isEmpty());

		identityService.delete(identity);
	}

	private void initData(String systemName, String identityName, String roleName, boolean protectionEnabled) {
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true, systemName);
		//
		// Create test identity for provisioning test
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(identityName);
		identity.setFirstName(identityName);
		identity.setLastName(identityName);
		identity.setEmail(identityName + "@email.cz");
		identity = identityService.save(identity);

		// Create mapped attributes to schema
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);
		systemMapping.setProtectionEnabled(protectionEnabled);
		systemMapping = systemMappingService.save(systemMapping);
		//
		SysSystemAttributeMappingDto attributeHandlingUserName = schemaAttributeHandlingService
				.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		// username is transformed
		attributeHandlingUserName.setTransformToResourceScript("return \"" + "x" + identityName + "\";");
		attributeHandlingUserName = schemaAttributeHandlingService.save(attributeHandlingUserName);
		
		/*
		 * Create role with link on system (default)
		 */
		IdmRoleDto roleDefault = new IdmRoleDto();
		roleDefault.setCode(roleName);
		roleDefault = roleService.save(roleDefault);
		SysRoleSystemDto roleSystemDefault = new SysRoleSystemDto();
		roleSystemDefault.setRole(roleDefault.getId());
		roleSystemDefault.setSystem(system.getId());
		roleSystemDefault.setSystemMapping(systemMapping.getId());
		roleSystemDefault = roleSystemService.save(roleSystemDefault);

	}
}
