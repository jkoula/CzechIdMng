package eu.bcvsolutions.idm.acc.provisioning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.TestResource;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test for personal other account use cases
 * @author Roman Kucera
 */
public class ProvisioningOtherAccountsTest extends AbstractIntegrationTest {

	@Autowired
	private DefaultAccTestHelper helper;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;

	@Test
	public void createOtherAccountAlone() {
		IdmIdentityDto identity = helper.createIdentity();
		SysSystemDto system = helper.createTestResourceSystem(false);
		helper.createMapping(system, SystemEntityType.IDENTITY, AccountType.PERSONAL_OTHER);
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system, AccountType.PERSONAL_OTHER);

		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		long accountsCount = accountService.count(accountFilter);
		assertEquals(0, accountsCount);

		helper.createIdentityRole(identity, role);

		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		assertEquals(1, accounts.size());

		AccAccountDto account = accounts.get(0);
		TestResource resource = helper.findResource(account.getUid());
		assertNotNull(resource);

		SysSystemMappingDto accountMapping = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class, null);
		assertNotNull(accountMapping);
		assertEquals(AccountType.PERSONAL_OTHER, accountMapping.getAccountType());

		helper.deleteAllResourceData();
		helper.deleteIdentity(identity.getId());
		helper.deleteRole(role.getId());
		helper.deleteSystem(system.getId());
	}

	@Test
	public void createOtherAccountWithDifferentUid() {
		IdmIdentityDto identity = helper.createIdentity();
		SysSystemDto system = helper.createTestResourceSystem(false);
		helper.createMapping(system, SystemEntityType.IDENTITY, AccountType.PERSONAL_OTHER);
		SysSystemMappingDto mapping = helper.createMapping(system, SystemEntityType.IDENTITY, AccountType.PERSONAL);
		SysSystemAttributeMappingDto uidAttribute = systemAttributeMappingService.findBySystemMappingAndName(mapping.getId(), TestHelper.ATTRIBUTE_MAPPING_NAME);
		uidAttribute.setTransformToResourceScript("return 'test_' + attributeValue");
		systemAttributeMappingService.save(uidAttribute);

		IdmRoleDto role = helper.createRole();
		IdmRoleDto rolePersonal = helper.createRole();
		helper.createRoleSystem(role, system, AccountType.PERSONAL);
		helper.createRoleSystem(rolePersonal, system, AccountType.PERSONAL_OTHER);

		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		long accountsCount = accountService.count(accountFilter);
		assertEquals(0, accountsCount);

		helper.createIdentityRole(identity, role);
		helper.createIdentityRole(identity, rolePersonal);

		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		assertEquals(2, accounts.size());

		AccAccountDto account = accounts.stream().filter(accAccountDto -> accAccountDto.getUid().equals(identity.getUsername())).findFirst().orElse(null);
		AccAccountDto account1 = accounts.stream().filter(accAccountDto -> !accAccountDto.getUid().equals(identity.getUsername())).findFirst().orElse(null);
		assertNotNull(account);
		assertNotNull(account1);
		TestResource resource = helper.findResource(account.getUid());
		TestResource resource1 = helper.findResource(account1.getUid());
		assertNotNull(resource);
		assertNotNull(resource1);
		SysSystemMappingDto accountMapping = DtoUtils.getEmbedded(account, AccAccount_.systemMapping, SysSystemMappingDto.class, null);
		SysSystemMappingDto accountMapping1 = DtoUtils.getEmbedded(account1, AccAccount_.systemMapping, SysSystemMappingDto.class, null);
		assertNotNull(accountMapping);
		assertNotNull(accountMapping1);
		assertEquals(AccountType.PERSONAL_OTHER, accountMapping.getAccountType());
		assertEquals(AccountType.PERSONAL, accountMapping1.getAccountType());

		helper.deleteAllResourceData();
		helper.deleteIdentity(identity.getId());
		helper.deleteRole(role.getId());
		helper.deleteRole(rolePersonal.getId());
		helper.deleteSystem(system.getId());
	}

	@Test
	public void createOtherAccountWithSameUid() {
		IdmIdentityDto identity = helper.createIdentity();
		SysSystemDto system = helper.createTestResourceSystem(false);
		helper.createMapping(system, SystemEntityType.IDENTITY, AccountType.PERSONAL_OTHER);
		helper.createMapping(system, SystemEntityType.IDENTITY, AccountType.PERSONAL);

		IdmRoleDto role = helper.createRole();
		IdmRoleDto rolePersonal = helper.createRole();
		helper.createRoleSystem(role, system, AccountType.PERSONAL);
		helper.createRoleSystem(rolePersonal, system, AccountType.PERSONAL_OTHER);

		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		long accountsCount = accountService.count(accountFilter);
		assertEquals(0, accountsCount);

		helper.createIdentityRole(identity, role);
		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		assertEquals(1, accounts.size());
		TestResource resource = helper.findResource(accounts.get(0).getUid());
		assertNotNull(resource);
		SysSystemMappingDto accountMapping = DtoUtils.getEmbedded(accounts.get(0), AccAccount_.systemMapping, SysSystemMappingDto.class, null);
		assertNotNull(accountMapping);
		assertEquals(AccountType.PERSONAL, accountMapping.getAccountType());

		assertThrows(ResultCodeException.class, () -> helper.createIdentityRole(identity, rolePersonal));

		helper.deleteAllResourceData();
		helper.deleteIdentity(identity.getId());
		helper.deleteRole(role.getId());
		helper.deleteRole(rolePersonal.getId());
		helper.deleteSystem(system.getId());
	}

}
