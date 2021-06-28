package eu.bcvsolutions.idm.core.monitoring.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringResultEvent.MonitoringResultEventType;
import eu.bcvsolutions.idm.core.monitoring.api.event.processor.MonitoringResultProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;

/**
 * Persists monitoring result.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component
@Description("Persists monitoring result.")
public class MonitoringResultSaveProcessor
		extends CoreEventProcessor<IdmMonitoringResultDto>
		implements MonitoringResultProcessor  {
	
	public static final String PROCESSOR_NAME = "monitoring-result-save-processor";
	//
	@Autowired private IdmMonitoringResultService service;
	
	public MonitoringResultSaveProcessor() {
		super(MonitoringResultEventType.UPDATE, MonitoringResultEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmMonitoringResultDto> process(EntityEvent<IdmMonitoringResultDto> event) {
		IdmMonitoringResultDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
