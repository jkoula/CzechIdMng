package eu.bcvsolutions.idm.acc.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Set;
import java.util.UUID;

/**
 * Filter for concept role request.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class AccAccountConceptRoleRequestFilter extends IdmBaseConceptRoleRequestFilter {
    private UUID accountuuid;

    private UUID accountRole;
    private Set<UUID> accountRoleUuids;

    private boolean accountRoleIsNull = false;

    public AccAccountConceptRoleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public AccAccountConceptRoleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmConceptRoleRequestDto.class, data);
	}

    public UUID getAccountuuid() {
        return accountuuid;
    }

    public void setAccountuuid(UUID accountuuid) {
        this.accountuuid = accountuuid;
    }

    public Set<UUID> getAccountRoleUuids() {
        return accountRoleUuids;
    }

    public void setAccountRoleUuids(Set<UUID> accountRoleUuids) {
        this.accountRoleUuids = accountRoleUuids;
    }

    public boolean isAccountRoleIsNull() {
        return accountRoleIsNull;
    }

    public void setAccountRoleIsNull(boolean accountRoleIsNull) {
        this.accountRoleIsNull = accountRoleIsNull;
    }

    public UUID getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(UUID accountRole) {
        this.accountRole = accountRole;
    }
}
