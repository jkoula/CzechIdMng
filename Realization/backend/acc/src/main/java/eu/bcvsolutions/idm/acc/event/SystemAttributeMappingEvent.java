package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto}
 * 
 * @author Roman Kucera
 *
 */

public class SystemAttributeMappingEvent extends CoreEvent<SysSystemAttributeMappingDto> {

	private static final long serialVersionUID = -2993824895457334827L;

	/**
	 *
	 * Supported attribute mapping event
	 *
	 */
	public enum SystemAttributeMappingEventType implements EventType {
		CREATE, UPDATE, DELETE;
	}

	public SystemAttributeMappingEvent(SystemAttributeMappingEventType operation, SysSystemAttributeMappingDto content) {
		super(operation, content);
	}

	public SystemAttributeMappingEvent(SystemAttributeMappingEventType operation, SysSystemAttributeMappingDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
