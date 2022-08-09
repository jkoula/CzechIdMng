package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;
import java.util.List;

import eu.bcvsolutions.idm.acc.dto.*;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.*;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Acc tests for role-request service
 * 
 * @author Vít Švanda
 *
 */
public class DefaultIdmRoleRequestServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	@Autowired
	private ProvisioningExecutor provisioningExecutor;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

	@Autowired
	private AccAccountService accountService;

	@Before
	public void login() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	@Transactional
	public void addPermissionViaRoleRequestTest() {
		final SysSystemDto testResourceSystem = helper.createTestResourceSystem(true);
		final IdmRoleDto roleA = getHelper().createRole();
		final IdmIdentityDto identity = getHelper().createIdentity();
		final AccIdentityAccountDto identityAccount = helper.createIdentityAccount(testResourceSystem, identity);
		final AccAccountDto accAccountDto = accountService.get(identityAccount.getAccount());

		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicant(accAccountDto.getId());
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setState(RoleRequestState.EXECUTED); // can not be saved (after
		// create must be
		// CONCEPT
		request = roleRequestService.save(request);

		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());

		LocalDate validFrom = LocalDate.now().minusDays(1);
		LocalDate validTill = LocalDate.now().plusMonths(1);
		AccAccountConceptRoleRequestDto conceptA = new AccAccountConceptRoleRequestDto();
		conceptA.setRoleRequest(request.getId());
		conceptA.setState(RoleRequestState.EXECUTED); // can not be saved (after
		// create must be
		// CONCEPT
		conceptA.setOperation(ConceptRoleRequestOperation.ADD);
		conceptA.setRole(roleA.getId());
		conceptA.setValidFrom(validFrom);
		conceptA.setValidTill(validTill);
		conceptA.setAccAccount(accAccountDto.getId());
		/*conceptA = conceptRoleRequestService.save(conceptA);

		Assert.assertEquals(RoleRequestState.CONCEPT, conceptA.getState());

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());

		Assert.assertEquals(RoleRequestState.EXECUTED, request.getState());
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findAllByIdentity(testA.getId());
		Assert.assertEquals(1, identityRoles.size());
		Assert.assertEquals(validFrom, identityRoles.get(0).getValidFrom());
		Assert.assertEquals(validTill, identityRoles.get(0).getValidTill());
		Assert.assertEquals(contractA.getId(), identityRoles.get(0).getIdentityContract());
		Assert.assertEquals(roleA.getId(), identityRoles.get(0).getRole());*/

	}


	@Test
	public void testSystemStateExecuted() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateBlocked() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.executeSync(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		assertEquals(concepts.size(), 1);
		assertNull(concepts.get(0).getSystemState());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateBlockedAndCanceled() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.cancel(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		assertEquals(concepts.size(), 1);
		assertEquals(OperationState.CANCELED, concepts.get(0).getSystemState().getState());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateFailedAndCanceled() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Simulation of exception - Set blocked operation as failed
		SysProvisioningOperationDto operationDto = operations.get(0);
		operationDto.getResult().setState(OperationState.EXCEPTION);
		operationDto = provisioningOperationService.save(operationDto);
		
		// Refresh system state -> must be in exception now
		request = roleRequestService.refreshSystemState(request);
		request = roleRequestService.save(request);
		assertEquals(OperationState.EXCEPTION, request.getSystemState().getState());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.cancel(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(request.getId());
		assertEquals(concepts.size(), 1);
		assertEquals(OperationState.CANCELED, concepts.get(0).getSystemState().getState());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateNotexecuted() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Set system as read-only
		system.setReadonly(true);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.NOT_EXECUTED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Set system to write mode
		system.setReadonly(false);
		system = systemService.save(system);
		
		provisioningExecutor.executeSync(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
	
	@Test
	public void testSystemStateFailed() {
		IdmRoleDto role = helper.createRole();
		SysSystemDto system = helper.createTestResourceSystem(true);
		// Block system operations
		SysBlockedOperationDto blockedOperation = new SysBlockedOperationDto();
		blockedOperation.setCreateOperation(true);
		blockedOperation.setDeleteOperation(true);
		blockedOperation.setUpdateOperation(true);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		helper.createRoleSystem(role, system);
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto primeContract = helper.getPrimeContract(identity);
		IdmRoleRequestDto request = helper.assignRoles(primeContract, role);
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.BLOCKED, request.getSystemState().getState());
		
		SysProvisioningOperationFilter provisioningFilter = new SysProvisioningOperationFilter();
		provisioningFilter.setSystemId(system.getId());
		List<SysProvisioningOperationDto> operations = provisioningOperationService.find(provisioningFilter, null).getContent();
		assertEquals(1, operations.size());
		
		// Simulation of exception - Set blocked operation as failed
		SysProvisioningOperationDto operationDto = operations.get(0);
		operationDto.getResult().setState(OperationState.EXCEPTION);
		operationDto = provisioningOperationService.save(operationDto);
		
		// Refresh system state -> must be in exception now
		request = roleRequestService.refreshSystemState(request);
		request = roleRequestService.save(request);
		assertEquals(OperationState.EXCEPTION, request.getSystemState().getState());
		
		// Unblock system operations
		blockedOperation.setCreateOperation(false);
		blockedOperation.setDeleteOperation(false);
		blockedOperation.setUpdateOperation(false);
		system.setBlockedOperation(blockedOperation);
		system = systemService.save(system);
		
		provisioningExecutor.executeSync(operations.get(0));
		// Load the request
		request = roleRequestService.get(request.getId());
		
		assertEquals(RoleRequestState.EXECUTED, request.getState());
		assertEquals(OperationState.EXECUTED, request.getSystemState().getState());
	}
}
