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
		// find already registered monitoring evaluator with synchronization identifier
		String evaluatorType = AutowireHelper.getTargetType(synchronizationMonitoringEvaluator);
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setEvaluatorType(evaluatorType);
		IdmMonitoringDto monitoring = monitoringService
				.find(filter, null)
				.stream()
				.filter(m -> {
					return synchronizationId.equals(
							m.getEvaluatorProperties().get(SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION)
					);
				})
				.findFirst()
				.orElse(null);
		if (event.hasType(SyncConfigEventType.CREATE)) {
			if (monitoring == null) { // create
				monitoring = new IdmMonitoringDto();
				monitoring.setCheckPeriod(3600L); // once per hour
				monitoring.setEvaluatorType(evaluatorType);
				monitoring.getEvaluatorProperties().put(
						SynchronizationMonitoringEvaluator.PARAMETER_SYNCHRONIZATION, 
						synchronizationId
				);
				monitoring.setInstanceId(configurationService.getInstanceId());
				monitoring.setDescription("Monitoring configured automatically.");
				//
				monitoring = monitoringService.save(monitoring);
				LOG.info("Monitoring [{}] configured automatically for synchronization configuration [{}]",
						monitoring.getId(), synchronizationId);
			}
		} else if (monitoring != null) { // delete
			monitoringService.delete(monitoring);
			LOG.info("Monitoring [{}] deleted automatically for deteled synchronization configuration [{}]",
					monitoring.getId(), synchronizationId);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 100; // ~ automatism on 100 order
	}
	
}