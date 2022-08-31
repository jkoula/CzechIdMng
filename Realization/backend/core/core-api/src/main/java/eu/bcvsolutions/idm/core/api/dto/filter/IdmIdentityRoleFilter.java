package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;

/**
 * Filter for identity role.
 *
 * @author svandav
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmIdentityRoleFilter extends BaseRoleAssignmentFilter implements ExternalIdentifiableFilter, CorrelationFilter, FormableFilter {

	public static final String PARAMETER_IDENTITY_CONTRACT_ID = "identityContractId";
	public static final String PARAMETER_CONTRACT_POSITION_ID = "contractPositionId";

	public IdmIdentityRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmIdentityRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmIdentityRoleDto.class, data);
	}

	public List<UUID> getIdentities() {
		return getParameterConverter().toUuids(getData(), PARAMETER_IDENTITY_ID);
	}
    
    public void setIdentities(List<UUID> identities) {
    	put(PARAMETER_IDENTITY_ID, identities);
	}

	public UUID getIdentityContractId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_IDENTITY_CONTRACT_ID);
	}

	public void setIdentityContractId(UUID identityContractId) {
		set(PARAMETER_IDENTITY_CONTRACT_ID, identityContractId);
	}

	public UUID getContractPositionId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_CONTRACT_POSITION_ID);
	}
	
	public void setContractPositionId(UUID contractPositionId) {
		set(PARAMETER_CONTRACT_POSITION_ID, contractPositionId);
	}

}
