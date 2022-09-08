package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class BaseRoleAssignmentFilter extends DataFilter {
    public static final String PARAMETER_DIRECT_ROLE = "directRole"; //if its direct role (true) or not (false - depends on some filled direct role)
    public static final String PARAMETER_DIRECT_ROLE_ID = "directRoleId";
    public static final String PARAMETER_ROLE_COMPOSITION_ID = "roleCompositionId";
    public static final String PARAMETER_ROLE_ID = "roleId"; // list - OR
    public static final String PARAMETER_ROLE_TEXT = "roleText"; // role text ~ quick ~ like
    public static final String PARAMETER_ROLE_ENVIRONMENT = "roleEnvironment";  // list - OR
    public static final String PARAMETER_ROLE_CATALOGUE_ID = "roleCatalogueId";
    public static final String PARAMETER_VALID = "valid"; // valid identity roles with valid contract
    public static final String PARAMETER_AUTOMATIC_ROLE = "automaticRole"; // true / false
    public static final String PARAMETER_AUTOMATIC_ROLE_ID = "automaticRoleId";
    public static final String PARAMETER_ROLE_SYSTEM_ID = "roleSystemId";
    public static final String PARAMETER_IDENTITY_ID = "identityId"; // list - OR

    public BaseRoleAssignmentFilter(Class<? extends BaseDto> dtoClass) {
        super(dtoClass);
    }

    public BaseRoleAssignmentFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
        super(dtoClass, data);
    }

    public BaseRoleAssignmentFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(dtoClass, data, parameterConverter);
    }


    public UUID getRoleId() {
    	return getParameterConverter().toUuid(getData(), PARAMETER_ROLE_ID);
	}

    public void setRoleId(UUID roleId) {
    	set(PARAMETER_ROLE_ID, roleId);
	}

    public List<UUID> getRoles() {
		return getParameterConverter().toUuids(getData(), PARAMETER_ROLE_ID);
	}

    public void setRoles(List<UUID> roles) {
    	put(PARAMETER_ROLE_ID, roles);
	}

    public String getRoleEnvironment() {
    	return getParameterConverter().toString(getData(), PARAMETER_ROLE_ENVIRONMENT);
	}

    public void setRoleEnvironment(String roleEnvironment) {
    	set(PARAMETER_ROLE_ENVIRONMENT, roleEnvironment);
	}

    public List<String> getRoleEnvironments() {
		return getParameterConverter().toStrings(getData(), PARAMETER_ROLE_ENVIRONMENT);
	}

    public void setRoleEnvironments(List<String> roleEnvironments) {
    	put(PARAMETER_ROLE_ENVIRONMENT, roleEnvironments);
	}

    public UUID getRoleCatalogueId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_ROLE_CATALOGUE_ID);
	}

    public void setRoleCatalogueId(UUID roleCatalogueId) {
		set(PARAMETER_ROLE_CATALOGUE_ID, roleCatalogueId);
	}

    public Boolean getValid() {
    	return getParameterConverter().toBoolean(getData(), PARAMETER_VALID);
	}

    public void setValid(Boolean valid) {
		set(PARAMETER_VALID, valid);
	}

    public Boolean getAutomaticRole() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_AUTOMATIC_ROLE);
	}

    public void setAutomaticRole(Boolean automaticRole) {
		set(PARAMETER_AUTOMATIC_ROLE, automaticRole);
	}

    public UUID getAutomaticRoleId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_AUTOMATIC_ROLE_ID);
	}

    public void setAutomaticRoleId(UUID automaticRoleId) {
		set(PARAMETER_AUTOMATIC_ROLE_ID, automaticRoleId);
	}

    public Boolean getDirectRole() {
        return getParameterConverter().toBoolean(getData(), PARAMETER_DIRECT_ROLE);
    }

    public void setDirectRole(Boolean directRole) {
        set(PARAMETER_DIRECT_ROLE, directRole);
    }

    public UUID getDirectRoleId() {
        return getParameterConverter().toUuid(getData(), PARAMETER_DIRECT_ROLE_ID);
    }

    public void setDirectRoleId(UUID directRoleId) {
        set(PARAMETER_DIRECT_ROLE_ID, directRoleId);
    }

    public UUID getRoleCompositionId() {
        return getParameterConverter().toUuid(getData(), PARAMETER_ROLE_COMPOSITION_ID);
    }

    public void setRoleCompositionId(UUID roleCompositionId) {
        set(PARAMETER_ROLE_COMPOSITION_ID, roleCompositionId);
    }

    public UUID getRoleSystemId() {
        return getParameterConverter().toUuid(data, PARAMETER_ROLE_SYSTEM_ID);
    }

    public void setRoleSystemId(UUID contractPositionId) {
        set(PARAMETER_ROLE_SYSTEM_ID, contractPositionId);
    }

    /**
     * Role text ~ quick ~ like.
     *
     * @return role text
     * @since 11.2.0
     */
    public String getRoleText() {
        return getParameterConverter().toString(getData(), PARAMETER_ROLE_TEXT);
    }

    /**
     * Role text ~ quick ~ like.
     *
     * @param roleText role text
     * @since 11.2.0
     */
    public void setRoleText(String roleText) {
        set(PARAMETER_ROLE_TEXT, roleText);
    }

    public UUID getIdentityId() {
    	return getParameterConverter().toUuid(getData(), BaseRoleAssignmentFilter.PARAMETER_IDENTITY_ID);
    }

    public void setIdentityId(UUID identityId) {
    	set(BaseRoleAssignmentFilter.PARAMETER_IDENTITY_ID, identityId);
    }

    public abstract void setOwnerId(UUID id);
}
