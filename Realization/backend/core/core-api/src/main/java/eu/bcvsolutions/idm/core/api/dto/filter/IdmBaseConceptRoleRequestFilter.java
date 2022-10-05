package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Filter for concept role request.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class IdmBaseConceptRoleRequestFilter extends DataFilter {

    private UUID roleRequestId;
    private RoleRequestState state;
    private UUID roleId;
    private String roleText;
    private UUID automaticRole;
    private ConceptRoleRequestOperation operation;
    private String roleEnvironment;
    private List<String> roleEnvironments;

	private Set<UUID> identityRoleIds;

	private UUID identityId;

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public void setIdentityRoleIds(Set<UUID> identityRoleIds) {
		this.identityRoleIds = identityRoleIds;
	}

	public void setIdentity(UUID identity) {
		setIdentityId(identity);
	}


	public IdmBaseConceptRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmBaseConceptRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmConceptRoleRequestDto.class, data);
	}

	public IdmBaseConceptRoleRequestFilter(Class<IdmConceptRoleRequestDto> idmConceptRoleRequestDtoClass, MultiValueMap<String, Object> data) {
		super(idmConceptRoleRequestDtoClass, data);
	}

	public UUID getRoleRequestId() {
        return roleRequestId;
    }

    public void setRoleRequestId(UUID roleRequestId) {
        this.roleRequestId = roleRequestId;
    }

    public RoleRequestState getState() {
        return state;
    }

    public void setState(RoleRequestState state) {
        this.state = state;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getAutomaticRole() {
		return automaticRole;
	}

	public void setAutomaticRole(UUID automaticRole) {
		this.automaticRole = automaticRole;
	}

	public ConceptRoleRequestOperation getOperation() {
        return operation;
    }

    public void setOperation(ConceptRoleRequestOperation operation) {
        this.operation = operation;
    }
    
	public String getRoleEnvironment() {
		return roleEnvironment;
	}

	public void setRoleEnvironment(String roleEnvironment) {
		this.roleEnvironment = roleEnvironment;
	}

	public List<String> getRoleEnvironments() {
		return roleEnvironments;
	}

	public void setRoleEnvironments(List<String> roleEnvironments) {
		this.roleEnvironments = roleEnvironments;
	}

	/**
	 * Role text ~ quick ~ like.
	 * 
	 * @return role text
	 * @since 11.2.0
	 */
	public String getRoleText() {
		return roleText;
	}
	
	/**
	 * Role text ~ quick ~ like.
	 * 
	 * @param roleText role text
	 * @since 11.2.0
	 */
	public void setRoleText(String roleText) {
		this.roleText = roleText;
	}

    public abstract void setRoleAssignmentUuid(UUID identityRoleId);

	public void setRoleAssignmentUuids(Set<UUID> identityRoleIds) {
		this.identityRoleIds = identityRoleIds;
	}

	public Set<UUID> getIdentityRoleIds() {
		return identityRoleIds;
	}

	public UUID getIdentity() {
		return getIdentityId();
	}
}
