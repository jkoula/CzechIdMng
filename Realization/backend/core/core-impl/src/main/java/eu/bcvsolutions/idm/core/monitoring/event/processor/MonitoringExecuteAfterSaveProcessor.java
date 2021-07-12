package eu.bcvsolutions.idm.core.monitoring.event.processor;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent.MonitoringEventType;
import eu.bcvsolutions.idm.core.monitoring.api.event.processor.MonitoringProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;

/**
 * Execute newly configured monitoring evaluator after evaluator is saved.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(MonitoringExecuteAfterSaveProcessor.PROCESSOR_NAME)
@Description("Execute newly configured monitoring evaluator after evaluator is saved.")
public class MonitoringExecuteAfterSaveProcessor
		extends CoreEventProcessor<IdmMonitoringDto>
		implements MonitoringProcessor  {
	
	public static final String PROCESSOR_NAME = "monitoring-execute-after-saved-processor";
	//
	@Autowired private MonitoringManager manager;
	@Autowired private ConfigurationService configurationService;
	
	public MonitoringExecuteAfterSaveProcessor() {
		super(MonitoringEventType.CREATE, MonitoringEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmMonitoringDto> event) {
		IdmMonitoringDto monitoring = event.getContent();
		ZonedDateTime executeDate = monitoring.getExecuteDate();
		//
		return super.conditional(event)
				&& configurationService.getInstanceId().equals(monitoring.getInstanceId()) // ~ same instance only
				&& event.hasPriority(PriorityType.HIGH) // ~ from FE only
				&& !monitoring.isDisabled()
				&& (executeDate == null || executeDate.isBefore(ZonedDateTime.now()));
	}

	@Override
	public EventResult<IdmMonitoringDto> process(EntityEvent<IdmMonitoringDto> event) {
		manager.execute(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10000;
	}
}
