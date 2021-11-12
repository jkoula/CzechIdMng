package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Cancel running entity events from queue.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
@Component(EntityEventCancelBulkAction.NAME)
@Description("Cancel running entity events from queue.")
public class EntityEventCancelBulkAction extends AbstractBulkAction<IdmEntityEventDto, IdmEntityEventFilter> {

	public static final String NAME = "core-entity-event-cancel-bulk-action";
	//
	@Autowired private IdmEntityEventService service;
	@Autowired private EntityEventManager manager;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ENTITYEVENT_UPDATE);
	}

	@Override
	public ReadWriteDtoService<IdmEntityEventDto, IdmEntityEventFilter> getService() {
		return service;
	}
	
	@Override
	protected OperationResult processDto(IdmEntityEventDto dto) {
		manager.cancelEvent(dto);
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 200;
	}
	
	@Override
	public NotificationLevel getLevel() {
		return NotificationLevel.WARNING;
	}
}
