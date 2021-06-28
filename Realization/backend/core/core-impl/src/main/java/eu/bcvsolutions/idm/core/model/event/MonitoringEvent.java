package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for monitoring
 * 
 * @author Vít Švanda
 * @deprecated use {@link eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent}
 */
@Deprecated(since = "11.1.0")
public class MonitoringEvent extends CoreEvent<IdmMonitoringTypeDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum MonitoringEventType implements EventType {
		CHECK
	}
	
	public MonitoringEvent(MonitoringEventType operation, IdmMonitoringTypeDto content) {
		super(operation, content);
	}
	
	public MonitoringEvent(MonitoringEventType operation, IdmMonitoringTypeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}