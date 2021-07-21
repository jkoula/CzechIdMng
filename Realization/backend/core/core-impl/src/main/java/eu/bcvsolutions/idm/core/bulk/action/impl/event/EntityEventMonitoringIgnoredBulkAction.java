package eu.bcvsolutions.idm.core.bulk.action.impl.event;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.bulk.impl.AbstractMonitoringIgnoredBulkAction;
import eu.bcvsolutions.idm.core.monitoring.service.impl.EntityEventMonitoringEvaluator;

/**
 * Ignore entity event in monitoring.
 * 	
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(EntityEventMonitoringIgnoredBulkAction.NAME)
@Description("Ignore entity event in monitoring.")
public class EntityEventMonitoringIgnoredBulkAction extends AbstractMonitoringIgnoredBulkAction<IdmEntityEventDto, IdmEntityEventFilter> {

	public static final String NAME = "core-entity-event-monitoring-ignored-bulk-action";
	//
	@Autowired private IdmEntityEventService service;
	@Autowired private EntityEventMonitoringEvaluator monitoringEvaluator;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected List<MonitoringEvaluator> getMonitoringEvaluators() {
		return Lists.newArrayList(monitoringEvaluator);
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ENTITYEVENT_READ);
	}

	@Override
	public ReadWriteDtoService<IdmEntityEventDto, IdmEntityEventFilter> getService() {
		return service;
	}
}
