package eu.bcvsolutions.idm.core.rest.impl;

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.role.RoleCanBeRequestedEvaluator;
import eu.bcvsolutions.idm.test.api.AbstractRoleAssignmentControllerRestTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.security.evaluator.identity.IdentityRoleByRoleEvaluator;

/**
 * Controller tests
 * - CRUD
 * - filter tests
 * 
 * @author Radek Tomiška
 *
 */
public class IdmIdentityRoleControllerRestTest extends AbstractRoleAssignmentControllerRestTest<IdmIdentityRoleDto, IdmIdentityRoleFilter> {

	@Autowired private IdmIdentityRoleController controller;


	@Autowired private IdmIdentityContractService contractService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmIdentityRoleDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmIdentityRoleDto prepareDto() {
		IdmIdentityRoleDto dto = new IdmIdentityRoleDto();
		dto.setIdentityContractDto(getHelper().getPrimeContract(getHelper().createIdentity().getId()));
		dto.setRole(getHelper().createRole().getId());
		dto.setValidFrom(LocalDate.now());
		dto.setValidTill(LocalDate.now().plusDays(1));
		return dto;
	}

	// Automatic roles are specific for IdentityRoles
	@Test
	public void testFindAutomaticRoles() {
		IdmIdentityContractDto contract = getHelper().getPrimeContract(getHelper().createIdentity().getId());
		IdmIdentityRoleDto normal = getHelper().createIdentityRole(contract, getHelper().createRole()); // normal
		// automatic
		IdmIdentityRoleDto automaticIdentityRole = new IdmIdentityRoleDto();
		automaticIdentityRole.setIdentityContract(contract.getId());
		IdmRoleDto role = getHelper().createRole();
		automaticIdentityRole.setRole(role.getId());
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(role.getId());
		automaticIdentityRole.setAutomaticRole(automaticRole.getId());
		IdmIdentityRoleDto automatic = createDto(automaticIdentityRole);
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityContractId(contract.getId());
		filter.setAutomaticRole(Boolean.TRUE);
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(automatic.getId())));
		//
		filter.setAutomaticRole(Boolean.FALSE);
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(normal.getId())));
		//
		// find by automatic role
		filter.setAutomaticRole(null);
		filter.setAutomaticRoleId(automaticRole.getId());
		results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(automatic.getId())));
	}


	// Not really a way to abstract this, but it is forced to be implemented
	@Override
	public void findByOwnerId() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmContractPositionDto contractPositionOne = getHelper().createContractPosition(contract);
		IdmContractPositionDto contractPositionOther = getHelper().createContractPosition(contract);
		IdmIdentityRoleDto one = getHelper().createIdentityRole(contractPositionOne, getHelper().createRole());
		getHelper().createIdentityRole(contractPositionOther, getHelper().createRole()); // other
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setIdentityId(identity.getId());
		filter.setContractPositionId(contractPositionOne.getId());
		List<IdmIdentityRoleDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(ir -> ir.getId().equals(one.getId())));
	}
	

	@Override
	protected IdmIdentityRoleDto getEmptyRoleAssignment(UUID id) {
		final IdmIdentityRoleDto identityRoleDto = new IdmIdentityRoleDto();
		identityRoleDto.setIdentityContract(id);
		return identityRoleDto;
	}

	@Override
	protected String getOwnerCode(IdmIdentityRoleDto roleAssignment) {
		final IdmIdentityContractDto idmIdentityContractDto = contractService.get(roleAssignment.getIdentityContract());
		final IdmIdentityDto identity = DtoUtils.getEmbedded(idmIdentityContractDto, IdmIdentityContract_.identity);
		return identity.getUsername();
	}

	@Override
	protected IdmIdentityRoleFilter getFilter() {
		return new IdmIdentityRoleFilter();
	}

	@Override
	protected IdmIdentityRoleDto createRoleAssignment(UUID ownerId, IdmRoleDto role) {
		return getHelper().createIdentityRole(contractService.get(ownerId), role);
	}

	@Override
	protected List<IdmIdentityRoleDto> createTestInvalidRoleAssignments(IdmIdentityDto identity) {
		List<IdmIdentityRoleDto> result = new ArrayList<>();
		final IdmIdentityRoleDto invalidByDate = getHelper().createIdentityRole(identity, getHelper().createRole(), null, LocalDate.now().minusDays(2));// inValidByDate
		IdmIdentityContractDto invalidContract = getHelper().createContract(identity, null, null, LocalDate.now().minusDays(2));
		final IdmIdentityRoleDto invalidByContractValididty = getHelper().createIdentityRole(invalidContract, getHelper().createRole());// inValidByContract
		result.add(invalidByDate);
		result.add(invalidByContractValididty);
		return result;
	}

	@Override
	protected IdmAuthorizationPolicyDto getPolicyForRole(UUID roleId) {
		getHelper().createAuthorizationPolicy(
				roleId,
				CoreGroupPermission.ROLE,
				IdmRole.class,
				RoleCanBeRequestedEvaluator.class,
				RoleBasePermission.CANBEREQUESTED, IdmBasePermission.UPDATE, IdmBasePermission.READ);

		ConfigurationMap evaluatorProperties = new ConfigurationMap();
		evaluatorProperties.put(IdentityRoleByRoleEvaluator.PARAMETER_CAN_BE_REQUESTED_ONLY, false);
		return getHelper().createAuthorizationPolicy(
				roleId,
				CoreGroupPermission.IDENTITYROLE,
				IdmIdentityRole.class,
				IdentityRoleByRoleEvaluator.class,
				evaluatorProperties);
	}

	@Override
	protected IdmIdentityRoleDto createRoleAssignment(IdmIdentityDto identity, IdmRoleDto roleOne) {
		return getHelper().createIdentityRole(identity, roleOne);
	}
}
