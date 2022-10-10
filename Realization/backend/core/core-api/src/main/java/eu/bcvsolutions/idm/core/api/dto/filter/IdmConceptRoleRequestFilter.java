package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filter for concept role request.
 *
 * @author svandav
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class IdmConceptRoleRequestFilter extends IdmBaseConceptRoleRequestFilter {

    public static final String IDENTITY_ROLE_ID_PARAMETER = "identityRoleId";
    public static final String IDENTITY_CONTRACT_ID_PARAMETER = "identityContractId";
    public static final String IDENTITY_IDENTITY_ROLE_IS_NULL_PARAMETER = "identityRoleIsNull";

    public IdmConceptRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmConceptRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmConceptRoleRequestDto.class, data);
	}

    @Override
    public void setRoleAssignmentUuid(UUID identityRoleId) {
        setIdentityRoleId(identityRoleId);
    }

    public UUID getIdentityRoleId() {
        return getParameterConverter().toUuid(getData(), IDENTITY_ROLE_ID_PARAMETER);
    }

    public void setIdentityRoleId(UUID identityRoleId) {
        set(IDENTITY_ROLE_ID_PARAMETER, identityRoleId);
    }

    public UUID getIdentityContractId() {
        return getParameterConverter().toUuid(getData(), IDENTITY_CONTRACT_ID_PARAMETER);
    }

    public void setIdentityContractId(UUID identityContractId) {
        set(IDENTITY_CONTRACT_ID_PARAMETER, identityContractId);
    }
    


	public boolean isIdentityRoleIsNull() {
        return getParameterConverter().toBoolean(getData(), IDENTITY_IDENTITY_ROLE_IS_NULL_PARAMETER, false);
	}

	public void setIdentityRoleIsNull(boolean identityRoleIsNull) {
		set(IDENTITY_IDENTITY_ROLE_IS_NULL_PARAMETER, identityRoleIsNull);
	}

}
