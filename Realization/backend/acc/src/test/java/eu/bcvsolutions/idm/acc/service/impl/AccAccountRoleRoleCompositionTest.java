package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests for account role and business roles.
 * 
 * @author Tomáš Doischer
 *
 */
public class AccAccountRoleRoleCompositionTest extends AbstractIntegrationTest {
	
	@Autowired
	private AccAccountRoleAssignmentService accountRoleService;
	@Autowired
	private TestHelper helper;
	
	@Before
	public void init() {
		helper.loginAdmin();
	}
	
	@After
	public void logout() {
		helper.logout();
	}
	
	@Test
	public void roleCompositionAssignmentViaRequestTest() {
		IdmRoleDto role = helper.createRole();
		IdmRoleDto roleSubOne = helper.createRole();
		helper.createRoleComposition(role, roleSubOne);
		IdmRoleDto roleSubTwo = helper.createRole();
		helper.createRoleComposition(role, roleSubTwo);
		//
		AccAccountDto account = helper.createAccount();
		helper.assignRoleToAccountViaRequest(account, true, role.getId());
		//
		List<AccAccountRoleAssignmentDto> accountRoles = accountRoleService.findByAccountId(account.getId());
		Assert.assertEquals(3, accountRoles.size());
		Assert.assertTrue(accountRoles.stream().anyMatch(aR -> aR.getRole().equals(role.getId())));
		Assert.assertTrue(accountRoles.stream().anyMatch(aR -> aR.getRole().equals(roleSubOne.getId())));
		Assert.assertTrue(accountRoles.stream().anyMatch(aR -> aR.getRole().equals(roleSubTwo.getId())));
		//
		helper.removeRoleFromAccountViaRequest(account, true, role.getId());
		//
		accountRoles = accountRoleService.findByAccountId(account.getId());
		Assert.assertEquals(0, accountRoles.size());
	}
	
	@Test
	public void roleCompositionAssignmentDirectlyTest() {
		IdmRoleDto role = helper.createRole();
		IdmRoleDto roleSubOne = helper.createRole();
		helper.createRoleComposition(role, roleSubOne);
		IdmRoleDto roleSubTwo = helper.createRole();
		helper.createRoleComposition(role, roleSubTwo);
		//
		AccAccountDto account = helper.createAccount();
		AccAccountRoleAssignmentDto roleAssignment = helper.createAccountRoleAssignment(account.getId(), role.getId());
		//
		List<AccAccountRoleAssignmentDto> accountRoles = accountRoleService.findByAccountId(account.getId());
		Assert.assertEquals(3, accountRoles.size());
		Assert.assertTrue(accountRoles.stream().anyMatch(aR -> aR.getRole().equals(role.getId())));
		Assert.assertTrue(accountRoles.stream().anyMatch(aR -> aR.getRole().equals(roleSubOne.getId())));
		Assert.assertTrue(accountRoles.stream().anyMatch(aR -> aR.getRole().equals(roleSubTwo.getId())));
		//
		helper.removeAccountRoleAssignment(roleAssignment);
		accountRoles = accountRoleService.findByAccountId(account.getId());
		Assert.assertEquals(0, accountRoles.size());
	}
}
