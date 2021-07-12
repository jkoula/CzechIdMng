package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.service.impl.DemoAdminMonitoringEvaluator;

/**
 * When stage is changed, then monitoring evaluator can be evaluated.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(ConfigurationChangeStageProcessor.PROCESSOR_NAME)
@Description("When stage is changed, then monitoring evaluator can be evaluated.")
public class ConfigurationChangeStageProcessor
		extends CoreEventProcessor<IdmConfigurationDto> 
		implements ConfigurationProcessor {
	
	public static final String PROCESSOR_NAME = "core-configuration-change-stage-processor";
	//
	@Autowired private MonitoringManager monitoringManager;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private DemoAdminMonitoringEvaluator demoAdminMonitoringEvaluator;
	
	public ConfigurationChangeStageProcessor() {
		super(ConfigurationEventType.UPDATE, ConfigurationEventType.CREATE, ConfigurationEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmConfigurationDto> event) {
		return super.conditional(event) 
				&& event.hasPriority(PriorityType.HIGH) // ~ from FE only
				&& event.getContent().getName().equals(ApplicationConfiguration.PROPERTY_STAGE);
	}

	@Override
	public EventResult<IdmConfigurationDto> process(EntityEvent<IdmConfigurationDto> event) {
		IdmMonitoringDto monitoring = findMonitoring();
		if (monitoring != null) {
			monitoringManager.execute(monitoring);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	/**
	 * find already registered monitoring evaluator with synchronization identifier
	 * 
	 * @return
	 */
	protected IdmMonitoringDto findMonitoring() {
		String evaluatorType = AutowireHelper.getTargetType(demoAdminMonitoringEvaluator);
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setEvaluatorType(evaluatorType);
		filter.setDisabled(Boolean.FALSE);
		//
		return monitoringService
				.find(filter, null)
				.stream()
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10000;
	}
}
