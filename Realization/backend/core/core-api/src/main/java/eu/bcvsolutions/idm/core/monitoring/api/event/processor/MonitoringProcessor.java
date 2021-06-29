package eu.bcvsolutions.idm.core.monitoring.api.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;

/**
 * Monitoring processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface MonitoringProcessor extends EntityEventProcessor<IdmMonitoringDto> {
	
}
