package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filter for concept role request.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class IdmBaseConceptRoleRequestFilter extends DataFilter implements OwnerTypeFilter {

	public static final String IDENTITY_ROLE_IDS_PARAMETER = "identityRoleIds";
	public static final String ROLE_REQUEST_ID_PARAMETER = "roleRequestId";
	public static final String ROLE_REQ_STATE_PARAMETER = "identityRoleIds";
	public static final String AUTOMATIC_ROLE_PARAMETER = "automaticRole";
	public static final String AUTOMATIC_ROLE_ID_PARAMETER = "automaticRoleId";
	public static final String DIRECT_ROLE_PARAMETER = "directRole";
	public static final String DIRECT_ROLE_ID_PARAMETER = "directRoleId";
	public static final String CONCEPT_ROLE_OPERATION_PARAMETER = "identityRoleIds";
	public static final String ENVIRONMENT_PARAMETER = "identityRoleIds";
	public static final String LOAD_ASSIGNMENTS_PARAMETER = "loadAssignments";


    private RoleRequestState state;
    private UUID roleId;
    private String roleText;

	private ConceptRoleRequestOperation operation;
    private String roleEnvironment;
    private List<String> roleEnvironments;

	private UUID identityId;

	public UUID getDirectRoleId() {
		return getParameterConverter().toUuid(getData(), DIRECT_ROLE_ID_PARAMETER);
	}

	public void setDirectRoleId(UUID directRole) {
		set(DIRECT_ROLE_ID_PARAMETER, directRole);
	}

	public Boolean isDirectRole() {
		return getParameterConverter().toBoolean(getData(), DIRECT_ROLE_PARAMETER);
	}

	public void setDirectRole(Boolean directRole) {
		set(DIRECT_ROLE_PARAMETER, directRole);
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
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
        return getParameterConverter().toUuid(getData(), ROLE_REQUEST_ID_PARAMETER);
    }

    public void setRoleRequestId(UUID roleRequestId) {
        set(ROLE_REQUEST_ID_PARAMETER, roleRequestId);
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

    public Boolean getAutomaticRole() {
		return getParameterConverter().toBoolean(getData(), AUTOMATIC_ROLE_PARAMETER);
	}

	public void setAutomaticRole(Boolean automaticRole) {
		set(AUTOMATIC_ROLE_PARAMETER, automaticRole);
	}

	public UUID getAutomaticRoleId() {
		return getParameterConverter().toUuid(getData(), AUTOMATIC_ROLE_ID_PARAMETER);
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		set(AUTOMATIC_ROLE_ID_PARAMETER, automaticRoleId);
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

	public Set<UUID> getIdentityRoleIds() {
		if (!getData().containsKey(IDENTITY_ROLE_IDS_PARAMETER)) {
			return null;
		}
		final List<Object> objects = getData().get(IDENTITY_ROLE_IDS_PARAMETER);


		return objects.stream().map(DtoUtils::toUuid).collect(Collectors.toSet());
	}

	public void setIdentityRoleIds(Set<UUID> identityRoleIds) {
		if (identityRoleIds != null) {
			put(IDENTITY_ROLE_IDS_PARAMETER, new ArrayList(identityRoleIds));
		}
	}

	public UUID getIdentity() {
		return getIdentityId();
	}

	public void setRoleAssignmentUuids(Set<UUID> identityRoleIds) {
		setIdentityRoleIds(identityRoleIds);
	}

	public Boolean isLoadRoleAssignments() {
		return getParameterConverter().toBoolean(getData(), LOAD_ASSIGNMENTS_PARAMETER, false);
	}

	public void setLoadAssignments(Boolean loadAssignments) {
		set(LOAD_ASSIGNMENTS_PARAMETER, loadAssignments);
	}

	public abstract void setOwnerUuid(UUID contractId);
	public abstract UUID getOwnerUuid();

}
