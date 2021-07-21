package eu.bcvsolutions.idm.core.scheduler.bulk.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.bulk.impl.AbstractMonitoringIgnoredBulkAction;
import eu.bcvsolutions.idm.core.monitoring.service.impl.LongRunningTaskMonitoringEvaluator;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Ignore long running task in monitoring.
 * 	
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(LongRunningTaskMonitoringIgnoredBulkAction.NAME)
@Description("Ignore long running task in monitoring.")
public class LongRunningTaskMonitoringIgnoredBulkAction extends AbstractMonitoringIgnoredBulkAction<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> {

	public static final String NAME = "core-long-running-task-monitoring-ignored-bulk-action";
	//
	@Autowired private IdmLongRunningTaskService service;
	@Autowired private LongRunningTaskMonitoringEvaluator monitoringEvaluator;
	
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
		return Lists.newArrayList(CoreGroupPermission.SCHEDULER_READ);
	}

	@Override
	public ReadWriteDtoService<IdmLongRunningTaskDto, IdmLongRunningTaskFilter> getService() {
		return service;
	}
}
