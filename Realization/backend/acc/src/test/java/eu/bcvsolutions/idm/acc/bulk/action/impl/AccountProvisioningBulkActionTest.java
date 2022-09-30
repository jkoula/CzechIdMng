package eu.bcvsolutions.idm.acc.bulk.action.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.collections.Sets;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.IdentitySynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

public class AccountProvisioningBulkActionTest extends AbstractBulkActionTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private SysProvisioningOperationService provisioningService;
	@Autowired
	private SysSystemService systemService;
	
	@Before
	public void init() {
		helper.loginAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testProcessBulkAction() {
		IdmRoleDto role = getHelper().createRole();
		IdmIdentityDto identity = getHelper().createIdentity();

		SysSystemDto system = helper.createTestResourceSystem(true);
		system.setDisabled(true);
		system = systemService.save(system);

		helper.createRoleSystem(role, system);
		
		getHelper().createIdentityRole(identity, role);

		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setEntityIdentifier(identity.getId());
		filter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		List<SysProvisioningOperationDto> operations = provisioningService.find(filter, null).getContent();
		assertEquals(1, operations.size());
		
		//
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setSystemId(system.getId());
		List<UUID> accountIds = accountService.findIds(accountFilter, null).getContent();
		//
		IdmBulkActionDto bulkAction = this.findBulkAction(AccAccount.class, AccountProvisioningBulkAction.NAME);
		bulkAction.setIdentifiers(Sets.newHashSet(accountIds));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		operations = provisioningService.find(filter, null).getContent();
		assertEquals(2, operations.size());
	}
}
