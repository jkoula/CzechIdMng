package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Set;
import java.util.UUID;

/**
 * Filter for concept role request.
 *
 * @author svandav
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class IdmConceptRoleRequestFilter extends IdmBaseConceptRoleRequestFilter {
    private UUID identityRoleId;
    private UUID identityContractId;
    private Set<UUID> identityRoleIds;

    private boolean identityRoleIsNull = false;

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
        return identityRoleId;
    }

    public void setIdentityRoleId(UUID identityRoleId) {
        this.identityRoleId = identityRoleId;
    }

    public UUID getIdentityContractId() {
        return identityContractId;
    }

    public void setIdentityContractId(UUID identityContractId) {
        this.identityContractId = identityContractId;
    }
    
    public Set<UUID> getIdentityRoleIds() {
		return identityRoleIds;
	}

	public void setIdentityRoleIds(Set<UUID> identityRoleIds) {
		this.identityRoleIds = identityRoleIds;
	}

	public boolean isIdentityRoleIsNull() {
		return identityRoleIsNull;
	}

	public void setIdentityRoleIsNull(boolean identityRoleIsNull) {
		this.identityRoleIsNull = identityRoleIsNull;
	}

}
