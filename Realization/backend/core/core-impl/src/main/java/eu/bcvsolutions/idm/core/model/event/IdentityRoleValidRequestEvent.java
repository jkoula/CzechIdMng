package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for identity role valid request
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public class IdentityRoleValidRequestEvent<T extends AbstractRoleValidRequestDto<?>> extends CoreEvent<T> {

	private static final long serialVersionUID = -2854160263156190911L;
	
	public enum IdentityRoleValidRequestEventType implements EventType {
		IDENTITY_ROLE_VALID
	}
	
	public IdentityRoleValidRequestEvent(EventType type, T content,
			Map<String, Serializable> properties) {
		super(type, content, properties);
	}
	
	public IdentityRoleValidRequestEvent(EventType type, T content) {
		super(type, content);
	}
}
