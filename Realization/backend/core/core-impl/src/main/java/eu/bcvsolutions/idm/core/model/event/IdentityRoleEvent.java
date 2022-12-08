package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;

/**
 * Events for identity roles
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityRoleEvent extends AbstractRoleAssignmentEvent<IdmIdentityRoleDto> {

	private static final long serialVersionUID = 1L;

	public IdentityRoleEvent(RoleAssignmentEventType operation, IdmIdentityRoleDto content) {
		super(operation, content);
	}
	
	public IdentityRoleEvent(RoleAssignmentEventType operation, IdmIdentityRoleDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}