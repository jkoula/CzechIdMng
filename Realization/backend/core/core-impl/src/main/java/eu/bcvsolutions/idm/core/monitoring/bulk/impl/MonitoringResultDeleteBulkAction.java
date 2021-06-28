package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;

/**
 * Delete selected monitoring results.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(MonitoringResultDeleteBulkAction.NAME)
@Description("Delete selected monitoring results.")
public class MonitoringResultDeleteBulkAction extends AbstractRemoveBulkAction<IdmMonitoringResultDto, IdmMonitoringResultFilter> {

	public static final String NAME = "core-monitoring-result-delete-bulk-action";

	@Autowired private IdmMonitoringResultService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(MonitoringGroupPermission.MONITORINGRESULT_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmMonitoringResultDto, IdmMonitoringResultFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
