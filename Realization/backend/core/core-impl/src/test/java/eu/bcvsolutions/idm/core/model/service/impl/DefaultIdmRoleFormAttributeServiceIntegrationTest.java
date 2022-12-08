package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;

/**
 * Basic role form attribute service test
 * 
 * @author Vít Švanda
 * @author Tomáš Doischer
 */
public class DefaultIdmRoleFormAttributeServiceIntegrationTest extends AbstractIdmRoleFormAttributeServiceIntegrationTest {

	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private IdmIdentityRoleService roleAssignmentService;

	@Override
	public AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role, UUID assigneeId,
			UUID roleAssignmentId, ConceptRoleRequestOperation operationType, LocalDate validFrom) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setIdentityContract(assigneeId);
		concept.setIdentityRole(roleAssignmentId);
		concept.setRole(role.getId());
		concept.setOperation(operationType);
		concept.setRoleRequest(request.getId());
		concept.setValidFrom(validFrom);

		return conceptRoleRequestService.save(concept);
	}
	
	@Override
	public IdmGeneralConceptRoleRequestService getConceptRoleService() {
		return conceptRoleRequestService;
	}

	@Override
	public IdmRoleAssignmentService getRoleAssignmentService() {
		return roleAssignmentService;
	}

	@Override
	public AbstractDto createOwner() {
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);
		return contract;
	}

	@Override
	public UUID getApplicant(AbstractDto owner) {
		if (owner instanceof IdmIdentityContractDto) {
			return ((IdmIdentityContractDto) owner).getIdentity();
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AbstractRoleAssignmentDto> findRoleAssignmentsForOwner(AbstractDto owner) {
		if (owner instanceof IdmIdentityContractDto) {
			IdmIdentityRoleFilter irFilter = new IdmIdentityRoleFilter();
			irFilter.setIdentityContractId(owner.getId());
			List<IdmIdentityRoleDto> identityRoles = roleAssignmentService.find(irFilter, null).getContent();
			return (List<AbstractRoleAssignmentDto>) (List<?>) identityRoles;
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}

	@Override
	public AbstractRoleAssignmentDto createRoleAssignment(UUID roleId, UUID ownerId, LocalDate validFrom, LocalDate validTill) {
		IdmIdentityRoleDto identityRole = new IdmIdentityRoleDto();
		identityRole.setIdentityContract(ownerId);
		identityRole.setRole(roleId);
		identityRole.setValidFrom(validFrom);
		identityRole.setValidTill(validTill);
		return roleAssignmentService.save(identityRole);
	}
}
