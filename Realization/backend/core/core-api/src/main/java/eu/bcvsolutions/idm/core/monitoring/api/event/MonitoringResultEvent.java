package eu.bcvsolutions.idm.core.monitoring.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;

/**
 * Events for monitoring results.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class MonitoringResultEvent extends CoreEvent<IdmMonitoringResultDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum MonitoringResultEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public MonitoringResultEvent(MonitoringResultEventType operation, IdmMonitoringResultDto content) {
		super(operation, content);
	}
	
	public MonitoringResultEvent(MonitoringResultEventType operation, IdmMonitoringResultDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}