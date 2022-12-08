package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;

/**
 * Incompatible role tests
 * 
 * @author Radek Tomiška
 * @author Tomáš Doischer
 *
 */
public class DefaultIdmIncompatibleRoleServiceIntegrationTest extends AbstractIdmIncompatibleRoleServiceIntegrationTest {

	@Override
	public AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role, UUID assigneeId,
			ConceptRoleRequestOperation operationType) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setIdentityContract(assigneeId);
		concept.setRole(role.getId());
		concept.setOperation(operationType);
		concept.setRoleRequest(request.getId());
		return getHelper().getService(IdmConceptRoleRequestService.class).save(concept);
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
}
