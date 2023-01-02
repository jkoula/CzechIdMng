package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningArchiveDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tomáš Doischer
 */
public class IdentityAccountManagementBulkActionTest extends AbstractBulkActionTest {
	@Autowired
	private TestHelper helper;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysProvisioningArchiveService provisioningArchiveService;
	@Autowired
	private SysSystemEntityService systemEntityService;
	@Autowired
	private IdmFormAttributeService formAttributeService;

	@Before
	public void init() {
		helper.loginAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void createOtherAccountWithDifferentUid() {
		IdmIdentityDto identity = helper.createIdentity();
		SysSystemDto system = helper.createTestResourceSystem(false, helper.createName());
		SysSystemMappingDto mapping = helper.createMapping(system, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, AccountType.PERSONAL_OTHER);
		//
		IdmRoleDto roleOtherOne = helper.createRole();
		IdmRoleDto roleOtherTwo = helper.createRole();
		helper.createRoleSystem(roleOtherOne, system, AccountType.PERSONAL_OTHER);
		helper.createRoleSystem(roleOtherTwo, system, AccountType.PERSONAL_OTHER);
		//
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		long accountsCount = accountService.count(accountFilter);
		Assert.assertEquals(0, accountsCount);
		//
		helper.createIdentityRole(identity, roleOtherOne);
		//
		List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
		Assert.assertEquals(1, accounts.size());
		// Let's change the account uid to prevent duplicate uid
		SysSystemEntityDto sysSystemEntityDto = systemEntityService.get(accounts.get(0).getSystemEntity());
		String newUid = "newDifferentUid";
		sysSystemEntityDto.setUid(newUid);
		systemEntityService.save(sysSystemEntityDto);
		AccAccountDto accountDto = accounts.get(0);
		accountDto.setUid(newUid);
		accountService.save(accountDto);
		// set the eav value as well
		IdmFormDefinitionDto formDefinitionDto = DtoUtils.getEmbedded(accountDto, AccAccount_.formDefinition, IdmFormDefinitionDto.class, null);
		IdmFormAttributeFilter formAttributeFilter = new IdmFormAttributeFilter();
		formAttributeFilter.setDefinitionId(formDefinitionDto.getId());
		formAttributeFilter.setCode(TestHelper.ATTRIBUTE_MAPPING_NAME);
		List<IdmFormAttributeDto> formAttributes = formAttributeService.find(formAttributeFilter, null).getContent();
		IdmFormAttributeDto uidFormAttribute = formAttributes
				.stream()
				.filter(formAttribute -> formAttribute.getCode().equals(TestHelper.ATTRIBUTE_MAPPING_NAME))
				.findFirst()
				.orElse(null);
		helper.setEavValue(accountDto, uidFormAttribute, AccAccount.class, newUid, PersistentType.SHORTTEXT, formDefinitionDto);
		//
		helper.createIdentityRole(identity, roleOtherTwo);
		//
		accounts = accountService.find(accountFilter, null).getContent();
		Assert.assertEquals(2, accounts.size());
		// Ww now have two accounts and can recalculate them
		ZonedDateTime now = ZonedDateTime.now();
		IdmBulkActionDto bulkAction = this.findBulkAction(IdmIdentity.class, IdentityAccountManagementBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(identity.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		checkResultLrt(processAction, 1l, null, null);
		//
		accounts = accountService.find(accountFilter, null).getContent();
		assertEquals(2, accounts.size());
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setSystemId(accountDto.getSystem());
		filter.setFrom(now);
		List<SysProvisioningArchiveDto> operations = provisioningArchiveService.find(filter, null).getContent();
		assertEquals(2, operations.size());
		operations.forEach(operation -> {
			Assert.assertTrue(operation.getSystemEntityUid().equals(newUid) || operation.getSystemEntityUid().equals(identity.getUsername()));
		});
		//
		helper.deleteAllResourceData();
		helper.deleteIdentity(identity.getId());
		helper.deleteRole(roleOtherOne.getId());
		helper.deleteRole(roleOtherTwo.getId());
		helper.deleteSystem(system.getId());
	}
}
