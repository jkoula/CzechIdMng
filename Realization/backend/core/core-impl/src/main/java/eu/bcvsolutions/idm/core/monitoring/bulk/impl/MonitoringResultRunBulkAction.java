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
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.entity.IdmMonitoringResult_;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Run related monitoring evaluator again by monitoring result.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(MonitoringResultRunBulkAction.NAME)
@Description("Run related monitoring evaluator again by monitoring result.")
public class MonitoringResultRunBulkAction extends AbstractBulkAction<IdmMonitoringResultDto, IdmMonitoringResultFilter> {

	public static final String NAME = "core-monitoring-result-run-bulk-action";
	//
	@Autowired private IdmMonitoringResultService service;
	@Autowired private MonitoringManager manager;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	protected OperationResult processDto(IdmMonitoringResultDto dto) {
		IdmMonitoringDto monitoring = getLookupService().lookupEmbeddedDto(dto, IdmMonitoringResult_.monitoring);
		//
		// preset parameters by result - monitoring configuration cen be changed in the meantime
		monitoring.setEvaluatorProperties(dto.getEvaluatorProperties());
		//
		manager.execute(monitoring, IdmBasePermission.EXECUTE);
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(MonitoringGroupPermission.MONITORINGRESULT_EXECUTE);
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
	public ReadWriteDtoService<IdmMonitoringResultDto, IdmMonitoringResultFilter> getService() {
		return service;
	}
}
