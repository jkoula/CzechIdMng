package eu.bcvsolutions.idm.core.bulk.action.impl.rolerequest;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleRequest;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Integration tests for {@link RoleDeleteBulkAction}.
 *
 * @author Ondrej Husnik
 */
public class RoleRequestDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired
	private IdmRoleRequestService roleRequestService;
	
	@Before
	public void login() {
		IdmIdentityDto adminIdentity = this.createUserWithAuthorities(IdmBasePermission.DELETE, IdmBasePermission.READ);
		loginAsNoAdmin(adminIdentity.getUsername());
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void deleteRoleRequestConcept() {
		IdmRoleDto role = this.createRoles(1).get(0);
		IdmIdentityDto identity = createIdentities(1).get(0);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		
		IdmRoleRequestDto roleRequest = roleRequestService.createRequest(contract, role);
		Assert.assertNotNull(roleRequest);
		Assert.assertNotNull(roleRequest.getId());
		Assert.assertEquals(RoleRequestState.CONCEPT, roleRequest.getState());
				
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleRequest.class, RoleRequestDeleteBulkAction.NAME);
		
		Set<UUID> ids = new HashSet<UUID>();
		ids.add(roleRequest.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		IdmRoleRequestDto deletedRoleRequest = roleRequestService.get(roleRequest.getId());
		Assert.assertNull(deletedRoleRequest);
	}
	
	/**
	 * When already executed in IdM and not in a system,
	 * request is set canceled on the target system
	 */
	@Test
	public void deleteRoleRequestExecuted() {
		IdmRoleDto role = this.createRoles(1).get(0);
		IdmIdentityDto identity = createIdentities(1).get(0);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		
		IdmRoleRequestDto roleRequest = roleRequestService.createRequest(contract, role);
		Assert.assertEquals(RoleRequestState.CONCEPT, roleRequest.getState());
		getHelper().executeRequest(roleRequest, false);
		roleRequest = roleRequestService.get(roleRequest);
		Assert.assertEquals(RoleRequestState.EXECUTED, roleRequest.getState());
		
		Assert.assertNotNull(roleRequest);
		Assert.assertNotNull(roleRequest.getId());
				
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleRequest.class, RoleRequestDeleteBulkAction.NAME);
		
		Set<UUID> ids = new HashSet<UUID>();
		ids.add(roleRequest.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		roleRequest = roleRequestService.get(roleRequest.getId());
		Assert.assertEquals(OperationState.CANCELED,roleRequest.getSystemState().getState());
		
		// request cannot be deleted because it is already executed
		processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, null, 1l, null);
	}
	
	
	/**
	 * Running request has to be canceled rather than deleted
	 */
	@Test
	public void deleteRoleRequestInProgress() {
		IdmRoleDto role = this.createRoles(1).get(0);
		IdmIdentityDto identity = createIdentities(1).get(0);
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity);
		
		IdmRoleRequestDto roleRequest = roleRequestService.createRequest(contract, role);
		Assert.assertNotNull(roleRequest);
		Assert.assertNotNull(roleRequest.getId());
		Assert.assertEquals(RoleRequestState.CONCEPT, roleRequest.getState());
		// manual change state to cover particular way the code goes
		roleRequest.setState(RoleRequestState.IN_PROGRESS);
		roleRequest = roleRequestService.save(roleRequest);
		Assert.assertEquals(RoleRequestState.IN_PROGRESS, roleRequest.getState());
				
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmRoleRequest.class, RoleRequestDeleteBulkAction.NAME);
		
		Set<UUID> ids = new HashSet<UUID>();
		ids.add(roleRequest.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
		
		roleRequest = roleRequestService.get(roleRequest.getId());
		roleRequest.setState(RoleRequestState.CANCELED);
	}
}
