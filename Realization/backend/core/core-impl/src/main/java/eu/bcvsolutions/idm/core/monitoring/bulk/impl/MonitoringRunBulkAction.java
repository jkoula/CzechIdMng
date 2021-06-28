package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Run monitoring evaluator.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(MonitoringRunBulkAction.NAME)
@Description("Run monitoring evaluator.")
public class MonitoringRunBulkAction extends AbstractBulkAction<IdmMonitoringDto, IdmMonitoringFilter> {

	public static final String NAME = "core-monitoring-run-bulk-action";
	//
	@Autowired private IdmMonitoringService service;
	@Autowired private MonitoringManager manager;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected OperationResult processDto(IdmMonitoringDto dto) {
		manager.execute(dto, IdmBasePermission.EXECUTE);
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(MonitoringGroupPermission.MONITORING_EXECUTE);
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
	
	@Override
	public NotificationLevel getLevel() {
		return NotificationLevel.SUCCESS;
	}

	@Override
	public ReadWriteDtoService<IdmMonitoringDto, IdmMonitoringFilter> getService() {
		return service;
	}
}
