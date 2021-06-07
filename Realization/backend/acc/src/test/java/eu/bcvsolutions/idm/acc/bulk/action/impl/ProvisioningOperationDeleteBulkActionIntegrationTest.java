package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableSet;

import eu.bcvsolutions.idm.InitApplicationData;
import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.SysProvisioningOperation;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete provisioning operations.
 *
 * @author Radek Tomiška
 *
 */
public class ProvisioningOperationDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private SysProvisioningOperationService provisioningOperationService;
	@Autowired private SysSystemEntityService systemEntityService;
	
	@Before
	public void init() {
		loginAsAdmin(InitApplicationData.ADMIN_USERNAME);
	}

	@After
	public void logout() {
		super.logout();
	}
	
	@Override
	protected DefaultAccTestHelper getHelper() {
		return (DefaultAccTestHelper) super.getHelper();
	}
	
	@Test
	public void testDelete() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		
		SysProvisioningOperationDto operationOne = createOperation(system);
		SysProvisioningOperationDto operationTwo = createOperation(system);

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(ImmutableSet.of(operationOne.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);

		Assert.assertNull(provisioningOperationService.get(operationOne.getId()));
		Assert.assertNotNull(provisioningOperationService.get(operationTwo.getId()));
	}
	
	@Test
	public void testDeleteInvalidOperation() {
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		
		SysProvisioningOperationDto operationOne = createOperation(system);
		systemEntityService.deleteInternalById(operationOne.getSystemEntity()); // internal - broke referential integrity

		IdmBulkActionDto bulkAction = this.findBulkAction(SysProvisioningOperation.class, ProvisioningOperationDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(ImmutableSet.of(operationOne.getId()));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);

		checkResultLrt(processAction, 1l, null, null);
	}
	
	private SysProvisioningOperationDto createOperation(SysSystemDto system) {
		SysProvisioningOperationDto dto = new SysProvisioningOperationDto();
		dto.setSystem(system.getId());
		dto.setEntityIdentifier(UUID.randomUUID());
		dto.setOperationType(ProvisioningEventType.CANCEL);
		dto.setEntityType(SystemEntityType.CONTRACT);
		dto.setProvisioningContext(new ProvisioningContext());
		dto.setResult(new OperationResult(OperationState.CANCELED));
		dto.setSystemEntity(getHelper().createSystemEntity(system).getId());
		//
		return provisioningOperationService.save(dto);
	}
}
