package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.RoleDeleteBulkAction;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Test for role force delete for roles which
 * are assigned to accounts.
 * 
 * @author Tomáš Doischer
 *
 */
public class AccRoleDeleteBulkActionTest extends AbstractBulkActionTest {
	
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private AccAccountRoleAssignmentService accountRoleService;
	@Autowired
	private TestHelper helper;
	
	@Test
	public void testForceDeleteAsync() {
		logout();
		loginAsAdmin();
		// create account
		AccAccountDto account = helper.createAccount();
		// create role
		IdmRoleDto role = getHelper().createRole();
		// assign role to account
		helper.createAccountRoleAssignment(account.getId(), role.getId());
		//
		List<AccAccountRoleAssignmentDto> roleAssignments = accountRoleService.findByAccountId(account.getId());
		Assert.assertEquals(1, roleAssignments.size());
		AccAccountRoleAssignmentDto roleAssignment = roleAssignments.get(0);
		Assert.assertEquals(role.getId(), roleAssignment.getRole());
		// remove role async
		try {
			getHelper().enableAsynchronousProcessing();
			
			Map<String, Object> properties = new HashMap<>();
			properties.put(RoleProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
			// delete by bulk action
			IdmBulkActionDto bulkAction = this.findBulkAction(IdmRole.class, RoleDeleteBulkAction.NAME);
			bulkAction.setIdentifiers(Sets.newHashSet(role.getId()));
			bulkAction.setProperties(properties);
			IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
			//
			getHelper().waitForResult(res -> roleService.get(role) != null);
			checkResultLrt(processAction, 1l, 0l, 0l);
			//
			roleAssignments = accountRoleService.findByAccountId(account.getId());
			Assert.assertEquals(0, roleAssignments.size());
			roleAssignment = accountRoleService.get(roleAssignment.getId());
			Assert.assertNull(roleAssignment);
		} finally {
			getHelper().disableAsynchronousProcessing();
		}
	}
}
