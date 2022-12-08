package eu.bcvsolutions.idm.core.model.event;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

import java.io.Serializable;
import java.util.Map;

/**
 * Events for role assignments
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public abstract class AbstractRoleAssignmentEvent<A extends AbstractRoleAssignmentDto> extends CoreEvent<A> {


	//
	public static final String PROPERTY_PROCESSED_ROLES = RoleEvent.PROPERTY_PROCESSED_ROLES; // event property, contains Set<UUID> of processed roles (used for role composition processing for the prevent cycles)
	public static final String PROPERTY_ASSIGNED_NEW_ROLES = "idm:assigned_new_roles"; // event property, contains List<IdmIdentityRole> of new assigned roles
	public static final String PROPERTY_ASSIGNED_REMOVED_ROLES = "idm:assigned_removed_roles"; // event property, contains List<UUID> of removed assigned roles
	public static final String PROPERTY_ASSIGNED_UPDATED_ROLES = "idm:assigned_updated_roles"; // event property, contains List<IdmIdentityRole> of updated assigned roles

	private static final long serialVersionUID = 1L;


	public AbstractRoleAssignmentEvent(EventType operation, A content) {
		super(operation, content);
	}

	public AbstractRoleAssignmentEvent(EventType operation, A content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

	/**
	 * Supported identity events
	 *
	 */
	public enum RoleAssignmentEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}
}