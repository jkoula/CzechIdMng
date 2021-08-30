package eu.bcvsolutions.idm.acc.event;

import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import java.io.Serializable;
import java.util.Map;

/**
 * Event for system group-system relation.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public class SystemGroupSystemEvent extends CoreEvent<SysSystemGroupSystemDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum SystemGroupSystemEventType implements EventType {
		CREATE,
		UPDATE,
		DELETE;
	}

	public SystemGroupSystemEvent(SystemGroupSystemEventType operation, SysSystemGroupSystemDto content) {
		super(operation, content);
	}

	public SystemGroupSystemEvent(SystemGroupSystemEventType operation, SysSystemGroupSystemDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}
