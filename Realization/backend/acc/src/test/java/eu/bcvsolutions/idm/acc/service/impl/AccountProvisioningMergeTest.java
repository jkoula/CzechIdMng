package eu.bcvsolutions.idm.acc.service.impl;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.ApplicantDto;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.ApplicantImplDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;

/**
 * Test for provisioning merge for account
 *
 * @author Tomáš Doischer
 *
 */
@Ignore
public class AccountProvisioningMergeTest extends AbstractProvisioningMergeTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private AccAccountRoleAssignmentService accountRoleService;

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
		IdmIdentityDto accountOwner = helper.createIdentity();
		SysSystemDto system = helper.createSystem("test_resource");
		SysSystemMappingDto mapping = helper.createMapping(system, AccountType.PERSONAL_OTHER);
		IdmRoleDto roleCreateAccount = helper.createRole();
		helper.createRoleSystem(roleCreateAccount, system, AccountType.PERSONAL_OTHER);
		// create the first account and rename it
		helper.createIdentityRole(accountOwner, roleCreateAccount);
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setUid(accountOwner.getUsername());
		AccAccountDto accountOne = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		String newAccountUid = "newAccountUid";
		helper.changeAccountUid(accountOne, newAccountUid);
		// create the second account
		helper.createIdentityRole(accountOwner, roleCreateAccount);
		AccAccountDto accountTwo = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		Assert.assertNotEquals(accountOne.getId(), accountTwo.getId());
		//
		IdmRoleDto roleOne = helper.createRole();
		IdmRoleDto roleTwo = helper.createRole();
		SysRoleSystemDto roleSystemOne = helper.createRoleSystem(roleOne, system, AccountType.PERSONAL_OTHER, false);
		SysRoleSystemDto roleSystemTwo = helper.createRoleSystem(roleTwo, system, AccountType.PERSONAL_OTHER, false);

		SysSchemaAttributeDto rightsSchemaAttribute = new SysSchemaAttributeDto();
		rightsSchemaAttribute.setObjectClass(mapping.getObjectClass());
		rightsSchemaAttribute.setName(RIGHTS_ATTRIBUTE);
		rightsSchemaAttribute.setMultivalued(true);
		rightsSchemaAttribute.setClassType(String.class.getName());
		rightsSchemaAttribute.setReadable(true);
		rightsSchemaAttribute.setUpdateable(true);

		rightsSchemaAttribute = schemaAttributeService.save(rightsSchemaAttribute);

		SysSystemAttributeMappingDto rightsAttribute = new SysSystemAttributeMappingDto();
		rightsAttribute.setSchemaAttribute(rightsSchemaAttribute.getId());
		rightsAttribute.setSystemMapping(mapping.getId());
		rightsAttribute.setName(RIGHTS_ATTRIBUTE);
		rightsAttribute.setStrategyType(AttributeMappingStrategyType.MERGE);
		rightsAttribute.setEntityAttribute(false);
		rightsAttribute = attributeMappingService.save(rightsAttribute);

		SysRoleSystemAttributeDto roleAttributeOne = new SysRoleSystemAttributeDto();
		roleAttributeOne.setName(RIGHTS_ATTRIBUTE);
		roleAttributeOne.setRoleSystem(roleSystemOne.getId());
		roleAttributeOne.setStrategyType(AttributeMappingStrategyType.MERGE);
		roleAttributeOne.setSystemAttributeMapping(rightsAttribute.getId());
		roleAttributeOne.setEntityAttribute(false);
		roleAttributeOne.setTransformToResourceScript("return '" + ONE_VALUE + "';");
		roleSystemAttributeService.saveInternal(roleAttributeOne);

		SysRoleSystemAttributeDto roleAttributeTwo = new SysRoleSystemAttributeDto();
		roleAttributeTwo.setName(RIGHTS_ATTRIBUTE);
		roleAttributeTwo.setRoleSystem(roleSystemTwo.getId());
		roleAttributeTwo.setStrategyType(AttributeMappingStrategyType.MERGE);
		roleAttributeTwo.setSystemAttributeMapping(rightsAttribute.getId());
		roleAttributeTwo.setEntityAttribute(false);
		roleAttributeTwo.setTransformToResourceScript("return '" + TWO_VALUE + "';");
		roleSystemAttributeService.saveInternal(roleAttributeTwo);

		getHelper().loginAdmin();

		// create request
		tryAssignRoleToAccountAndExpectValue(accountOwner, roleOne, accountOne, newAccountUid, ONE_VALUE);
		// let's try to assign the second role to the account two
		tryAssignRoleToAccountAndExpectValue(accountOwner, roleTwo, accountTwo, accountOwner.getUsername(), TWO_VALUE);
		//
		helper.deleteAllResourceData();
		helper.deleteIdentity(accountOwner.getId());
		//helper.deleteRole(roleOne.getId());
		//helper.deleteRole(roleTwo.getId());
		//helper.deleteRole(roleCreateAccount.getId());
		//helper.deleteSystem(system.getId());
	}

	private void tryAssignRoleToAccountAndExpectValue(IdmIdentityDto accountOwner, IdmRoleDto roleOne, AccAccountDto accountOne, String newAccountUid, String expectedValue) {
		ZonedDateTime now = ZonedDateTime.now();
		IdmRoleRequestDto request = new IdmRoleRequestDto();
		request.setApplicantInfo(new ApplicantImplDto(accountOwner.getId(), IdmIdentityDto.class.getCanonicalName()));
		request.setExecuteImmediately(true);
		request.setRequestedByType(RoleRequestedByType.MANUALLY);
		request.setState(RoleRequestState.EXECUTED);
		request = roleRequestService.save(request);
		Assert.assertEquals(RoleRequestState.CONCEPT, request.getState());
		AbstractConceptRoleRequestDto concept = createConceptRoleRequest(request,
				roleOne, accountOne.getId());
		Assert.assertEquals(RoleRequestState.CONCEPT, concept.getState());

		getHelper().startRequestInternal(request, true, true);
		request = roleRequestService.get(request.getId());
		List<AccAccountRoleAssignmentDto> assignedRolesOne = accountRoleService.findByAccountId(accountOne.getId());
		Assert.assertEquals(1, assignedRolesOne.size());
		Assert.assertEquals(roleOne.getId(), assignedRolesOne.get(0).getRole());
		//
		SysProvisioningOperationFilter operationFilter = new SysProvisioningOperationFilter();
		operationFilter.setSystemEntityUid(newAccountUid);
		operationFilter.setFrom(now);
		// only account one should have the value
		List<SysProvisioningArchiveDto> archives = provisioningArchiveService
				.find(operationFilter,
						PageRequest.of(0, 100, new Sort(Sort.Direction.DESC, AbstractEntity_.created.getName())))
				.getContent();
		Assert.assertEquals(1, archives.size());
		Map<ProvisioningAttributeDto, Object> accountObject = archives.get(0).getProvisioningContext().getAccountObject();
		List<String> rightsOne = (List<String>) accountObject.entrySet()
				.stream()
				.filter(entry -> entry.getKey().getSchemaAttributeName().equals(RIGHTS_ATTRIBUTE))
				.findFirst()
				.orElse(null)
				.getValue();
		Assert.assertEquals(1, rightsOne.size());

		Assert.assertEquals(expectedValue, rightsOne.get(0));
		//
		operationFilter = new SysProvisioningOperationFilter();
		operationFilter.setSystemEntityUid(accountOwner.getUsername());
		operationFilter.setFrom(now);
		// account two should have no value
		archives = provisioningArchiveService
				.find(operationFilter,
						PageRequest.of(0, 100, new Sort(Sort.Direction.DESC, AbstractEntity_.created.getName())))
				.getContent();
		//Assert.assertEquals(0, archives.size());
	}
}
