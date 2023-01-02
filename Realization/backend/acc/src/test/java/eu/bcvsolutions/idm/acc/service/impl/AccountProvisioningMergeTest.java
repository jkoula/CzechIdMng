package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

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

	@Test
	public void testMergeAttributesForMultiplePersonalAccounts() {
		AbstractDto owner = this.createOwner();
		SysSystemDto system;
		SysSystemMappingDto mapping;
		ApplicantDto applicant = getApplicant(owner);
		if (owner instanceof AccAccountDto) {
			system = DtoUtils.getEmbedded(owner, AccAccount_.system, SysSystemDto.class, null);
			mapping = DtoUtils.getEmbedded(owner, AccAccount_.systemMapping, SysSystemMappingDto.class, null);
		} else {
			throw new UnsupportedOperationException(String.format("This owner type is not supported! Owner: [{}]", owner));
		}

		IdmRoleDto roleOne = helper.createRole();

		SysRoleSystemDto roleSystemOne = helper.createRoleSystem(roleOne, system);

//		SysSchemaAttributeDto rightsSchemaAttribute = new SysSchemaAttributeDto();
//		rightsSchemaAttribute.setObjectClass(mapping.getObjectClass());
//		rightsSchemaAttribute.setName(RIGHTS_ATTRIBUTE);
//		rightsSchemaAttribute.setMultivalued(true);
//		rightsSchemaAttribute.setClassType(String.class.getName());
//		rightsSchemaAttribute.setReadable(true);
//		rightsSchemaAttribute.setUpdateable(true);
//
//		rightsSchemaAttribute = schemaAttributeService.save(rightsSchemaAttribute);
//
//		SysSystemAttributeMappingDto rightsAttribute = new SysSystemAttributeMappingDto();
//		rightsAttribute.setSchemaAttribute(rightsSchemaAttribute.getId());
//		rightsAttribute.setSystemMapping(mapping.getId());
//		rightsAttribute.setName(RIGHTS_ATTRIBUTE);
//		rightsAttribute.setStrategyType(AttributeMappingStrategyType.MERGE);
//		rightsAttribute.setEntityAttribute(false);
//		rightsAttribute = attributeMappingService.save(rightsAttribute);
//
//		SysRoleSystemAttributeDto roleAttributeOne = new SysRoleSystemAttributeDto();
//		roleAttributeOne.setName(RIGHTS_ATTRIBUTE);
//		roleAttributeOne.setRoleSystem(roleSystemOne.getId());
//		roleAttributeOne.setStrategyType(AttributeMappingStrategyType.SET);
//		roleAttributeOne.setSystemAttributeMapping(rightsAttribute.getId());
//		roleAttributeOne.setEntityAttribute(false);
//		roleAttributeOne.setTransformToResourceScript("return '" + ONE_VALUE + "';");
//		roleAttributeOne = roleSystemAttributeService.saveInternal(roleAttributeOne);
//
//		getHelper().loginAdmin();
//
//		// create request
//		IdmRoleRequestDto request = new IdmRoleRequestDto();
//		request.setApplicant(new ApplicantImplDto(applicantId, IdmIdentityDto.class.getCanonicalName()));
//		request.setExecuteImmediately(true);
//		request.setRequestedByType(RoleRequestedByType.MANUALLY);
//		request.setState(RoleRequestState.EXECUTED);
//		request = roleRequestService.save(request);
//		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());
//		AbstractConceptRoleRequestDto concept = createConceptRoleRequest(request,
//				roleOne, owner.getId());
//		Assert.assertEquals(RoleRequestState.CONCEPT, concept.getState());
//
//		getHelper().startRequestInternal(request, true, true);
//		request = roleRequestService.get(request.getId());
	}
}
