package eu.bcvsolutions.idm.core.monitoring.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringResultEvent.MonitoringResultEventType;
import eu.bcvsolutions.idm.core.monitoring.api.event.processor.MonitoringResultProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;

/**
 * Clear last result cache, when monitoring result is deleted.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(MonitoringResultEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear last result cache, when monitoring result is deleted..")
public class MonitoringResultEvictCacheProcessor 
		extends CoreEventProcessor<IdmMonitoringResultDto> 
		implements MonitoringResultProcessor {

	public static final String PROCESSOR_NAME = "core-monitoring-result-evict-cache-processor";
	//
	@Autowired private IdmCacheManager cacheManager;

	public MonitoringResultEvictCacheProcessor() {
		super(MonitoringResultEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmMonitoringResultDto> process(EntityEvent<IdmMonitoringResultDto> event) {
		cacheManager.evictCache(MonitoringManager.LAST_RESULT_CACHE_NAME);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
