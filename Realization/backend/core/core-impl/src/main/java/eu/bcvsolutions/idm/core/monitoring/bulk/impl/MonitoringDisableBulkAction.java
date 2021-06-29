package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;

/**
 * Disable monitoring evaluator.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(MonitoringDisableBulkAction.NAME)
@Description("Disable monitoring evaluator.")
public class MonitoringDisableBulkAction extends AbstractBulkAction<IdmMonitoringDto, IdmMonitoringFilter> {

	public static final String NAME = "core-monitoring-disable-bulk-action";
	//
	@Autowired private IdmMonitoringService service;
	
	@Override
	public String getName() {
		return MonitoringDisableBulkAction.NAME;
	}

	@Override
	protected OperationResult processDto(IdmMonitoringDto monitoring) {
		if (!monitoring.isDisabled()) {
			monitoring.setDisabled(true);
			getService().save(monitoring);
		}
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public ReadWriteDtoService<IdmMonitoringDto, IdmMonitoringFilter> getService() {
		return service;
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		authorities.add(MonitoringGroupPermission.MONITORING_UPDATE);
		//
		return authorities;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 300;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
