package eu.bcvsolutions.idm.acc.event;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;

import java.io.Serializable;
import java.util.Map;

/**
 * Events for account roles
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public class AccAccountRoleEvent extends AbstractRoleAssignmentEvent<AccAccountRoleDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum AccountRoleEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}

	public AccAccountRoleEvent(AccountRoleEventType operation, AccAccountRoleDto content) {
		super(operation, content);
	}

	public AccAccountRoleEvent(AccountRoleEventType operation, AccAccountRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}