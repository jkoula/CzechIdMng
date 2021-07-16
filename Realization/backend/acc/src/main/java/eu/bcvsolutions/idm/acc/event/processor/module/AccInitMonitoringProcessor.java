package eu.bcvsolutions.idm.acc.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.event.processor.synchronization.SyncConfigMonitoringAutoConfigurationProcessor;
import eu.bcvsolutions.idm.acc.monitoring.ProvisioningOperationMonitoringEvaluator;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningArchiveService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitMonitoringProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
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

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccInitMonitoringProcessor.class);
	public static final String PROCESSOR_NAME = "acc-init-monitoring-processor";
	
	@Autowired private SysSyncConfigService syncConfigService;
	@Autowired private EnabledEvaluator enabledEvaluator;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private SyncConfigMonitoringAutoConfigurationProcessor syncConfigMonitoringAutoConfigurationProcessor;
	@Autowired private ProvisioningOperationMonitoringEvaluator provisioningOperationMonitoringEvaluator;
	
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
		// errors is provisioning queue
		initProvisioningOperationMonitoring();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	protected IdmMonitoringDto initProvisioningOperationMonitoring() {
		String evaluatorType = AutowireHelper.getTargetType(provisioningOperationMonitoringEvaluator);
		IdmMonitoringDto monitoring = findMonitoring(evaluatorType, null, null);
		if (monitoring == null) {
			monitoring = new IdmMonitoringDto();
			monitoring.setEvaluatorType(evaluatorType);
			monitoring.setInstanceId(configurationService.getInstanceId());
			monitoring.setCheckPeriod(3600L); // ~ per hour
			monitoring.setSeq((short) 0); // ~ quick
			monitoring.setDescription(PRODUCT_PROVIDED_MONITORING_DESCRIPTION);
			ConfigurationMap evaluatorProperties = new ConfigurationMap();
			evaluatorProperties.put(
					ProvisioningOperationMonitoringEvaluator.PARAMETER_NUMBER_OF_DAYS, 
					ProvisioningOperationMonitoringEvaluator.DEFAULT_NUMBER_OF_DAYS
			);
			monitoring.setEvaluatorProperties(evaluatorProperties);
			//
			monitoring = monitoringService.save(monitoring);
			LOG.info("Provisioning operation monitoring configured automatically.");
		}
		//
		return monitoring;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 11010;
	}
}
