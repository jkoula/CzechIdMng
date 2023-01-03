package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;

/**
 * Test for provisioning merge for account
 *
 * @author Tomáš Doischer
 *
 */
public class AccountProvisioningMergeTest extends AbstractProvisioningMergeTest {

	@Autowired
	private TestHelper helper;
	
	@Override
	protected AbstractConceptRoleRequestDto createConceptRoleRequest(IdmRoleRequestDto request, IdmRoleDto role, UUID ownerId) {
		return helper.createAccountConceptRoleRequest(request.getId(), role.getId(), ownerId);
	}

	@Override
	protected AbstractDto createOwner() {
		return helper.createAccount();
	}

	@Override
	protected ApplicantDto getApplicant(AbstractDto owner) {
		if (owner instanceof AccAccountDto) {
			final UUID accountOwner = helper.getAccountOwner(owner.getId());
			return new ApplicantImplDto(accountOwner, IdmIdentityDto.class.getCanonicalName());
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}
	}
}
