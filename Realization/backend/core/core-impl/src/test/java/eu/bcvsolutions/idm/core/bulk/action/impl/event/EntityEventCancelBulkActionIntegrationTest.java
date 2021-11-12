package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Cancel running entity event from queue integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class EntityEventCancelBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmEntityEventService service;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		IdmEntityEventDto event = new IdmEntityEventDto();
		event.setOwnerId(UUID.randomUUID());
		event.setOwnerType("mock");
		event.setInstanceId("mock");
		event.setPriority(PriorityType.NORMAL);
		event.setResult(new OperationResultDto(OperationState.RUNNING));
		event = service.save(event);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmEntityEvent.class, EntityEventCancelBulkAction.NAME);
		
		Set<UUID> ids = Sets.newHashSet(event.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 1l, null, null);
		
		Assert.assertEquals(OperationState.CANCELED, service.get(event.getId()).getResult().getState());
	}
}
