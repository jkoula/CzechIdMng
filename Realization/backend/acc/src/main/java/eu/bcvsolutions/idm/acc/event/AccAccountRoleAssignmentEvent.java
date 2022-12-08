package eu.bcvsolutions.idm.acc.event;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;

import java.io.Serializable;
import java.util.Map;

/**
 * Events for account roles
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public class AccAccountRoleAssignmentEvent extends AbstractRoleAssignmentEvent<AccAccountRoleAssignmentDto> {

	private static final long serialVersionUID = 1L;

	public AccAccountRoleAssignmentEvent(RoleAssignmentEventType operation, AccAccountRoleAssignmentDto content) {
		super(operation, content);
	}

	public AccAccountRoleAssignmentEvent(RoleAssignmentEventType operation, AccAccountRoleAssignmentDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}