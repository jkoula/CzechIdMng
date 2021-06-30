package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.event.SyncConfigEvent.SyncConfigEventType;
import eu.bcvsolutions.idm.acc.monitoring.SynchronizationMonitoringEvaluator;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.processor.module.InitMonitoringProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Auto configure (create / delete) monitoring evaluator after synchronization is created or deleted.
 * Synchronization monitoring evaluator has to be enabled, when synchronization is created.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(SyncConfigMonitoringAutoConfigurationProcessor.PROCESSOR_NAME)
@Description("Auto configure (create / delete) monitoring evaluator after synchronization is created or deleted.")
public class SyncConfigMonitoringAutoConfigurationProcessor 
		extends CoreEventProcessor<AbstractSysSyncConfigDto>
		implements SyncConfigProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncConfigMonitoringAutoConfigurationProcessor.class);
	public static final String PROCESSOR_NAME = "acc-sync-config-monitoring-auto-configuration-processor";
	//
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private SynchronizationMonitoringEvaluator synchronizationMonitoringEvaluator;
	@Autowired private ConfigurationService configurationService;
	
	public SyncConfigMonitoringAutoConfigurationProcessor() {
		super(SyncConfigEventType.CREATE, SyncConfigEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AbstractSysSyncConfigDto> process(EntityEvent<AbstractSysSyncConfigDto> event) {
		UUID synchronizationId = event.getContent().getId();
		Assert.notNull(synchronizationId, "Synchronization identifier is required.");
		//
		if (event.hasType(SyncConfigEventType.CREATE)) {
			configureMonitoring(synchronizationId);
		} else { // delete
			deleteMonitoring(synchronizationId);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * Configure monitoring evaluator for given synchronization configuration, if monitoring evaluator is not already configured.
	 * 
	 * @param synchronizationId synchronization configuration identifier
	 * @return configured monitoring evaluator 
	 */
	public IdmMonitoringDto configureMonitoring(UUID synchronizationId) {
		IdmMonitoringDto monitoring = findMonitoring(synchronizationId);
		if (monitoring != null) {
			return monitoring;
		}
		//
		monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(3600L); // once per hour
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(synchronizationMonitoringEvaluator));
		monitoring.getEvaluatorProperties().put(
				SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, 
				synchronizationId
		);
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring.setDescription(InitMonitoringProcessor.PRODUCT_PROVIDED_MONITORING_DESCRIPTION);
		//
		monitoring = monitoringService.save(monitoring);
		LOG.info("Monitoring [{}] configured automatically for synchronization configuration [{}]",
				monitoring.getId(), synchronizationId);
		//
		return monitoring;
	}
	
	/**
	 * Delete configured monitoring evaluator for given synchronization configuration, if monitoring evaluator is configured.
	 * 
	 * @param synchronizationId synchronization configuration identifier
	 */
	public void deleteMonitoring(UUID synchronizationId) {
		IdmMonitoringDto monitoring = findMonitoring(synchronizationId);
		if (monitoring == null) {
			return; // nothing to remove
		}
		//
		monitoringService.delete(monitoring);
		LOG.info("Monitoring [{}] deleted automatically for deteled synchronization configuration [{}]",
				monitoring.getId(), synchronizationId);
	}
	
	/**
	 * find already registered monitoring evaluator with synchronization identifier
	 * 
	 * @return
	 */
	protected IdmMonitoringDto findMonitoring(UUID synchronizationId) {
		String evaluatorType = AutowireHelper.getTargetType(synchronizationMonitoringEvaluator);
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setEvaluatorType(evaluatorType);
		//
		return monitoringService
				.find(filter, null)
				.stream()
				.filter(m -> {
					// lookout - FE raw string properties can be given
					return synchronizationId.equals(
							getParameterConverter().toUuid(m.getEvaluatorProperties(), SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION)
					);
				})
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 100; // ~ automatism on 100 order
	}
	
}