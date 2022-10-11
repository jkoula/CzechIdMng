package eu.bcvsolutions.idm.acc.workflow.permissions;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.workflow.permissions.AbstractChangeIdentityPermissionTest;

/**
 * Test change permissions for account (account roles).
 * 
 * @author Tomáš Doischer
 *
 */
public class ChangeAccountPermissionTest extends AbstractChangeIdentityPermissionTest {

	@Autowired
	private AccAccountConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private AccAccountRoleAssignmentService roleAssignmentService;
	@Autowired
	private TestHelper helper;
	
	@Override
	public AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role, UUID assigneeId,
			UUID roleAssignmentId, ConceptRoleRequestOperation operationType, LocalDate validFrom, List<IdmFormInstanceDto> eavs) {
		return helper.createAccountConceptRoleRequest(request.getId(), role.getId(), 
				assigneeId, roleAssignmentId, operationType, validFrom, validFrom);
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
		return helper.createAccount();
	}

	@Override
	public UUID getApplicant(AbstractDto owner) {
		if (owner instanceof AccAccountDto) {
			return helper.getAccountOwner(owner.getId());
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}

	@Override
	public List<AbstractRoleAssignmentDto> findRoleAssignmentsForOwner(AbstractDto owner) {
		if (owner instanceof AccAccountDto) {
			AccAccountRoleAssignmentFilter arFilter = new AccAccountRoleAssignmentFilter();
			arFilter.setAccountId(owner.getId());
			List<AccAccountRoleAssignmentDto> accountRoles = roleAssignmentService.find(arFilter, null).getContent();
			return (List<AbstractRoleAssignmentDto>) (List<?>) accountRoles;
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}

	@Override
	public AbstractRoleAssignmentDto createRoleAssignment(UUID roleId, UUID ownerId, LocalDate validFrom,
			LocalDate validTill) {
		return helper.createAccountRoleAssignment(ownerId, roleId, validFrom, validTill);
	}
}
