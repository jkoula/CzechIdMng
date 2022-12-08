package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractIdmIncompatibleRoleServiceIntegrationTest;

/**
 * Incompatible role tests for accounts.
 * 
 * @author Radek Tomiška
 * @author Tomáš Doischer
 *
 */
public class DefaultAccountIncompatibleRoleServiceIntegrationTest extends AbstractIdmIncompatibleRoleServiceIntegrationTest {
	
	@Autowired
	private TestHelper helper;
	//
	@Override
	public AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role, UUID assigneeId,
			ConceptRoleRequestOperation operationType) {
		return helper.createAccountConceptRoleRequest(request.getId(), role.getId(), assigneeId, null, operationType);
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
}
