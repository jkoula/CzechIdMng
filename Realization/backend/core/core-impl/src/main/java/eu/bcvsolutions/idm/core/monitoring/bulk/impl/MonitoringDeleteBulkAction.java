package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;

/**
 * Delete selected configured monitoring evaluators.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(MonitoringDeleteBulkAction.NAME)
@Description("Delete selected configured monitoring evaluators.")
public class MonitoringDeleteBulkAction extends AbstractRemoveBulkAction<IdmMonitoringDto, IdmMonitoringFilter> {

	public static final String NAME = "core-monitoring-delete-bulk-action";

	@Autowired private IdmMonitoringService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(MonitoringGroupPermission.MONITORING_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmMonitoringDto, IdmMonitoringFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
