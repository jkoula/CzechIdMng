package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.monitoring.ProvisioningOperationMonitoringEvaluator;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.bulk.impl.AbstractMonitoringIgnoredBulkAction;

/**
 * Ignore provisioning operation in monitoring.
 * 	
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(ProvisioningOperationMonitoringIgnoredBulkAction.NAME)
@Description("Ignore provisioning operation in monitoring.")
public class ProvisioningOperationMonitoringIgnoredBulkAction extends AbstractMonitoringIgnoredBulkAction<SysProvisioningOperationDto, SysProvisioningOperationFilter> {

	public static final String NAME = "acc-provisioning-operation-monitoring-ignored-bulk-action";
	//
	@Autowired private SysProvisioningOperationService service;
	@Autowired private ProvisioningOperationMonitoringEvaluator monitoringEvaluator;
	
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
		return Lists.newArrayList(AccGroupPermission.PROVISIONING_OPERATION_READ);
	}

	@Override
	public ReadWriteDtoService<SysProvisioningOperationDto, SysProvisioningOperationFilter> getService() {
		return service;
	}
}
