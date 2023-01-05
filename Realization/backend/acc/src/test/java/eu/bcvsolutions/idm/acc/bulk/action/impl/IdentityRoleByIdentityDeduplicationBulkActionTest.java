package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.bulk.action.impl.IdentityRoleByIdentityDeduplicationBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import static org.junit.Assert.assertEquals;

/**
 * @author Tomáš Doischer
 */
public class IdentityRoleByIdentityDeduplicationBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;

	@Test
	public void otherPersonalAccountsCreatingRoles() {
		helper.loginAdmin();

		IdmIdentityDto accountOwner = helper.createIdentity();
		SysSystemDto system = helper.createSystem("test_resource");
		SysSystemMappingDto mapping = helper.createMapping(system, AccountType.PERSONAL_OTHER);
		IdmRoleDto roleCreateAccount = helper.createRole();
		IdmRoleDto roleNotCreateAccount = helper.createRole();
		helper.createRoleSystem(roleCreateAccount, system, AccountType.PERSONAL_OTHER);
		// create the first account and rename it
		helper.createIdentityRole(accountOwner, roleCreateAccount);
		helper.createIdentityRole(accountOwner, roleNotCreateAccount);
		helper.createIdentityRole(accountOwner, roleNotCreateAccount);
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid(accountOwner.getUsername());
		AccAccountDto accountOne = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		String newAccountUid = "newAccountUid";
		helper.changeAccountUid(accountOne, newAccountUid);
		// create the second account
		helper.createIdentityRole(accountOwner, roleCreateAccount);
		AccAccountDto accountTwo = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		Assert.assertNotEquals(accountOne.getId(), accountTwo.getId());

		List<IdmIdentityRoleDto> roles = identityRoleService.findAllByIdentity(accountOwner.getId());
		assertEquals(4, roles.size());

		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityRoleByIdentityDeduplicationBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(accountOwner.getId()));

		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);

		roles = identityRoleService.findAllByIdentity(accountOwner.getId());
		assertEquals(3, roles.size());

		accountOne = accountService.get(accountOne.getId());
		Assert.assertNotNull(accountOne);
		accountTwo = accountService.get(accountTwo.getId());
		Assert.assertNotNull(accountTwo);

		helper.deleteAllResourceData();
		helper.deleteIdentity(accountOwner.getId());
		helper.deleteRole(roleCreateAccount.getId());
		helper.deleteSystem(system.getId());

		helper.logout();
	}
}
