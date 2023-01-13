package eu.bcvsolutions.idm.acc.event.processor;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Tests which check account role assignment with validity.
 * 
 * @author Tomáš Doischer
 *
 */
public class AccountRoleAssignmentValidityTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private AccAccountRoleAssignmentService accountRoleService;
	
	@Before
	public void init() {
		helper.loginAdmin();
	}
	
	@After
	public void after() {
		helper.logout();
	}
	
	@Test
	public void testStartValidity() {
		IdmRoleDto role = helper.createRole();
		AccAccountDto account = helper.createAccount();
		AccAccountRoleAssignmentFilter accountRoleFilter = new AccAccountRoleAssignmentFilter();
		accountRoleFilter.setValid(Boolean.TRUE);
		final long initialCountOfRoles = accountRoleService.find(accountRoleFilter, null).getTotalElements();
		// create request that is valid in the future
		helper.assignRoleToAccountViaRequest(account, LocalDate.now().plusDays(5l), null, true, role.getId());
		//

		List<AccAccountRoleAssignmentDto> accountRoles = accountRoleService.find(accountRoleFilter, null).getContent();
		Assert.assertEquals(initialCountOfRoles, accountRoles.size());
		//
		// update role assigment via request and set the role as valid
		helper.updateAssignedAccountRoleViaRequest(account, LocalDate.now(), null, true, role.getId());
		//
		accountRoles = accountRoleService.find(accountRoleFilter, null).getContent();
		Assert.assertEquals(initialCountOfRoles + 1, accountRoles.size());
	}
	
	@Test
	public void testEndValidity() {
		IdmRoleDto role = helper.createRole();
		AccAccountDto account = helper.createAccount();
		// create request that is valid
		helper.assignRoleToAccountViaRequest(account, LocalDate.now().minusDays(5l), null, true, role.getId());
		//
		AccAccountRoleAssignmentFilter accountRoleFilter = new AccAccountRoleAssignmentFilter();
		accountRoleFilter.setValid(Boolean.TRUE);
		accountRoleFilter.setAccountId(account.getId());
		List<AccAccountRoleAssignmentDto> accountRoles = accountRoleService.find(accountRoleFilter, null).getContent();
		Assert.assertEquals(1, accountRoles.size());
		//
		// update role assigment via request and set the role as no longer valid
		helper.updateAssignedAccountRoleViaRequest(account, LocalDate.now().minusDays(6l), LocalDate.now().minusDays(3l), true, role.getId());
		//
		accountRoles = accountRoleService.find(accountRoleFilter, null).getContent();
		Assert.assertEquals(0, accountRoles.size());
	}
}
