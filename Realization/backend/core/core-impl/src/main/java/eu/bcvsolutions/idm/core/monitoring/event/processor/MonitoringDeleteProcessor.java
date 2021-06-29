package eu.bcvsolutions.idm.core.monitoring.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent.MonitoringEventType;
import eu.bcvsolutions.idm.core.monitoring.api.event.processor.MonitoringProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.repository.IdmMonitoringResultRepository;

/**
 * Deletes configured monitoring evaluators - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(MonitoringDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes monitoring from repository.")
public class MonitoringDeleteProcessor
		extends CoreEventProcessor<IdmMonitoringDto>
		implements MonitoringProcessor {
	
	public static final String PROCESSOR_NAME = "monitoring-delete-processor";
	//
	@Autowired private IdmMonitoringService service;
	@Autowired private IdmMonitoringResultRepository monitoringResultRepository;
	
	public MonitoringDeleteProcessor() {
		super(MonitoringEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmMonitoringDto> process(EntityEvent<IdmMonitoringDto> event) {
		IdmMonitoringDto monitoring = event.getContent();
		UUID monitoringId = monitoring.getId();
		Assert.notNull(monitoringId, "Monitoring identifier is required.");
		//
		monitoringResultRepository.deleteByMonitoring(monitoringId);
		//
		service.deleteInternal(monitoring);
		//
		return new DefaultEventResult<>(event, this);
	}
}