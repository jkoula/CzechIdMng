package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.acc.DefaultAccTestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningContext;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.impl.ContractSynchronizationExecutor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.test.api.AbstractMonitoringIgnoredBulkActionIntegrationTest;

/**
 * Test for bulk actions which created monitoring ignored flag.
 * 
 * @author Radek Tomiška
 */
public class ProvisioningOperationMonitoringIgnoredBulkActionIntegrationTest extends AbstractMonitoringIgnoredBulkActionIntegrationTest<SysProvisioningOperationDto> {

	@Autowired private SysProvisioningOperationService service;
	@Autowired private ProvisioningOperationMonitoringIgnoredBulkAction bulkAction;
	
	@Override
	protected AbstractBulkAction<SysProvisioningOperationDto, ?> getBulkAction() {
		return bulkAction;
	}
	
	@Override
	protected DefaultAccTestHelper getHelper() {
		return (DefaultAccTestHelper) super.getHelper();
	}
	
	@Override
	protected SysProvisioningOperationDto createDto() {
		SysProvisioningOperationDto dto = new SysProvisioningOperationDto();
		SysSystemDto system = getHelper().createTestResourceSystem(false);
		dto.setSystem(system.getId());
		dto.setEntityIdentifier(UUID.randomUUID());
		dto.setOperationType(ProvisioningEventType.CANCEL);
		dto.setEntityType(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		dto.setProvisioningContext(new ProvisioningContext());
		dto.setResult(new OperationResult(OperationState.CANCELED));
		dto.setSystemEntity(getHelper().createSystemEntity(system).getId());
		//
		return service.save(dto);
	}
}
