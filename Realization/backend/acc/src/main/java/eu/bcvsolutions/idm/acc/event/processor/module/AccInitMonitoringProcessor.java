package eu.bcvsolutions.idm.acc.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.event.processor.synchronization.SyncConfigMonitoringAutoConfigurationProcessor;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitMonitoringProcessor;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Init product provided monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(AccInitMonitoringProcessor.PROCESSOR_NAME)
@Description("Init product provided monitoring evaluators.")
public class AccInitMonitoringProcessor extends InitMonitoringProcessor {

	public static final String PROCESSOR_NAME = "acc-init-monitoring-processor";
	
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private EnabledEvaluator enabledEvaluator;
	@Autowired private SyncConfigMonitoringAutoConfigurationProcessor syncConfigMonitoringAutoConfigurationProcessor;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		// init database table monitoring evaluators
		initDatabaseTableMonitoring(SysProvisioningArchiveService.class);
		initDatabaseTableMonitoring(SysProvisioningOperationService.class);
		//
		// init synchronization monitoring evaluators
		if (enabledEvaluator.isEnabled(syncConfigMonitoringAutoConfigurationProcessor)) {
			syncConfigService
				.find(null)
				.forEach(syncConfig -> {
					syncConfigMonitoringAutoConfigurationProcessor.configureMonitoring(syncConfig.getId());
				});
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 11010;
	}
}
