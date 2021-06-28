package eu.bcvsolutions.idm.core.monitoring.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;

/**
 * Events for configured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class MonitoringEvent extends CoreEvent<IdmMonitoringDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum MonitoringEventType implements EventType {
		CREATE, UPDATE, DELETE, EXECUTE
	}

	public MonitoringEvent(MonitoringEventType operation, IdmMonitoringDto content) {
		super(operation, content);
	}
	
	public MonitoringEvent(MonitoringEventType operation, IdmMonitoringDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}