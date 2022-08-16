package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for system owner by identity
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
public class SystemOwnerEvent extends CoreEvent<SysSystemOwnerDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum SystemOwnerEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public SystemOwnerEvent(SystemOwnerEventType operation, SysSystemOwnerDto content) {
		super(operation, content);
	}

	public SystemOwnerEvent(SystemOwnerEventType operation, SysSystemOwnerDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}