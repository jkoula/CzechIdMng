package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;

/**
 * Test for provisioning merge
 *
 * @author Vít Švanda
 * @author Tomáš Doischer
 *
 */
public class ProvisioningMergeTest extends AbstractProvisioningMergeTest {

	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired
	private TestHelper helper;
	
	@Override
    protected AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role, UUID ownerId) {
		IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
		concept.setRoleRequest(request.getId());
		concept.setState(RoleRequestState.EXECUTED);
		concept.setOperation(ConceptRoleRequestOperation.ADD);
		concept.setRole(role.getId());
		concept.setIdentityContract(ownerId);
		return (IdmConceptRoleRequestDto) conceptRoleRequestService.save(concept);
	}

	@Override
	protected AbstractDto createOwner() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityContractDto contract = helper.createContract(identity);
		return contract;
	}

	@Override
	protected ApplicantDto getApplicant(AbstractDto owner) {
		if (owner instanceof IdmIdentityContractDto) {
			final UUID identity = ((IdmIdentityContractDto) owner).getIdentity();
			return new ApplicantImplDto(identity, IdmIdentityDto.class.getCanonicalName());
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}
}
