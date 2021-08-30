package eu.bcvsolutions.idm.acc.event;

import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import java.io.Serializable;
import java.util.Map;

/**
 * Event for system group.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public class SystemGroupEvent extends CoreEvent<SysSystemGroupDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum SystemGroupEventType implements EventType {
		CREATE,
		UPDATE,
		DELETE;
	}

	public SystemGroupEvent(SystemGroupEventType operation, SysSystemGroupDto content) {
		super(operation, content);
	}

	public SystemGroupEvent(SystemGroupEventType operation, SysSystemGroupDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}
