package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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
	private static final String ROLE_DEFAULT = "AccountStopManagingBulkActionTestUserRole";
	private static final String SYSTEM_NAME = "AccountStopManagingBulkActionTestUserSystem";
	
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
		initData();
		//
		IdmIdentityDto identity = identityService.getByUsername(IDENTITY_USERNAME);
		IdmRoleDto roleDefault = roleService.getByCode(ROLE_DEFAULT);

		Assert.assertNull("No account for this identity can be found, before account management start!",
				helper.findResource("x" + IDENTITY_USERNAME));

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
		irdto = identityRoleService.save(irdto);

		identityAccounts = identityAccountService.find(iaccFilter, null).getContent();
		//
		TestResource createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Idenitity have to exist on target system",
				createdAccount);

		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid("x" + IDENTITY_USERNAME);
		List<UUID> accountIds = accountService.findIds(accountFilter, null).getContent();
		Assert.assertEquals("Account needs to be created", 1,
				accountIds.size());
		// run the bulk action
		IdmBulkActionDto bulkAction = this.findBulkAction(AccAccount.class, AccountStopManagingBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(accountIds));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		createdAccount = helper.findResource("x" + IDENTITY_USERNAME);
		Assert.assertNotNull("Identity must still exist on target system", createdAccount);
		accountIds = accountService.findIds(accountFilter, null).getContent();
		Assert.assertTrue("Account in IdM must be deleted now", accountIds.isEmpty());
		
		identityService.delete(identity);
	}
	
	private void initData() {
		// create test system
		SysSystemDto system = helper.createTestResourceSystem(true, SYSTEM_NAME);
		//
		// Create test identity for provisioning test
		IdmIdentityDto identity = new IdmIdentityDto();
		identity.setUsername(IDENTITY_USERNAME);
		identity.setFirstName(IDENTITY_USERNAME);
		identity.setLastName(IDENTITY_USERNAME);
		identity.setEmail(IDENTITY_USERNAME + "@email.cz");
		identity = identityService.save(identity);

		// Create mapped attributes to schema
		SysSystemMappingDto systemMapping = helper.getDefaultMapping(system);
		SysSystemAttributeMappingDto attributeHandlingUserName = schemaAttributeHandlingService
				.findBySystemMappingAndName(systemMapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		// username is transformed
		attributeHandlingUserName.setTransformToResourceScript("return \"" + "x" + IDENTITY_USERNAME + "\";");
		attributeHandlingUserName = schemaAttributeHandlingService.save(attributeHandlingUserName);
		
		/*
		 * Create role with link on system (default)
		 */
		IdmRoleDto roleDefault = new IdmRoleDto();
		roleDefault.setCode(ROLE_DEFAULT);
		roleDefault = roleService.save(roleDefault);
		SysRoleSystemDto roleSystemDefault = new SysRoleSystemDto();
		roleSystemDefault.setRole(roleDefault.getId());
		roleSystemDefault.setSystem(system.getId());
		roleSystemDefault.setSystemMapping(systemMapping.getId());
		roleSystemDefault = roleSystemService.save(roleSystemDefault);

	}
}
