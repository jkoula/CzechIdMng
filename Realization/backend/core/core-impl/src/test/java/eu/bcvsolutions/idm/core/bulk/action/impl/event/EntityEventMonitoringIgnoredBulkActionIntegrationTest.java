package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.test.api.AbstractMonitoringIgnoredBulkActionIntegrationTest;

/**
 * Test for bulk actions which created monitoring ignored flag.
 * 
 * @author Radek Tomiška
 */
public class EntityEventMonitoringIgnoredBulkActionIntegrationTest extends AbstractMonitoringIgnoredBulkActionIntegrationTest<IdmEntityEventDto> {

	@Autowired private IdmEntityEventService service;
	@Autowired private EntityEventMonitoringIgnoredBulkAction bulkAction;
	
	@Override
	protected AbstractBulkAction<IdmEntityEventDto, ?> getBulkAction() {
		return bulkAction;
	}
	
	@Override
	protected IdmEntityEventDto createDto() {
		IdmEntityEventDto dto = new IdmEntityEventDto();
		dto.setOwnerId(UUID.randomUUID());
		dto.setOwnerType("mock");
		dto.setInstanceId("mock");
		dto.setPriority(PriorityType.NORMAL);
		dto.setResult(new OperationResultDto(OperationState.CREATED));
		//
		return service.save(dto);
	}
}
