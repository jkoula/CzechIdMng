package eu.bcvsolutions.idm.acc.event.processor.account;

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
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests that after account is deleted, account roles are deleted
 * as well.
 * 
 * @author Tomáš Doischer
 *
 */
public class AccountDeleteProcessorTest extends AbstractIntegrationTest {

	@Autowired
	private AccAccountRoleAssignmentService accountRoleService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private AccAccountService accountService;
	
	@Before
	public void init() {
		helper.loginAdmin();
	}
	
	@After
	public void logout() {
		helper.logout();
	}
	
	@Test
	public void accountRoleDeleteAfterAccountDelete() {
		IdmRoleDto role = helper.createRole();
		//
		AccAccountDto account = helper.createAccount();
		AccAccountRoleAssignmentDto accountRole = helper.createAccountRoleAssignment(account.getId(), role.getId());
		//
		List<AccAccountRoleAssignmentDto> accountRoles = accountRoleService.findByAccountId(account.getId());
		Assert.assertEquals(1, accountRoles.size());
		//
		accountService.delete(account);
		account = accountService.get(account.getId());
		Assert.assertNull(account);
		accountRole = accountRoleService.get(accountRole.getId());
		Assert.assertNull(accountRole);
	}
}
