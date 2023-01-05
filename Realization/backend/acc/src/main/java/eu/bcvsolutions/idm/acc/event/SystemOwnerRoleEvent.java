package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for system owner by role
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
public class SystemOwnerRoleEvent extends CoreEvent<SysSystemOwnerRoleDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum SystemOwnerRoleEventType implements EventType {
		CREATE, UPDATE, DELETE
	}

	public SystemOwnerRoleEvent(SystemOwnerRoleEventType operation, SysSystemOwnerRoleDto content) {
		super(operation, content);
	}

	public SystemOwnerRoleEvent(SystemOwnerRoleEventType operation, SysSystemOwnerRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}