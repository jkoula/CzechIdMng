package eu.bcvsolutions.idm.acc.dto.filter;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.CorrelationFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FormableFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class AccAccountRoleAssignmentFilter extends BaseRoleAssignmentFilter implements ExternalIdentifiableFilter, CorrelationFilter, FormableFilter {

    public static final String PARAMETER_ACCOUNT_ID = "accountId";

    public AccAccountRoleAssignmentFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public AccAccountRoleAssignmentFilter(MultiValueMap<String, Object> data) {
        super(AccAccountRoleAssignmentDto.class, data);
    }

    public void setAccountId(UUID accountId) {
        set(PARAMETER_ACCOUNT_ID,accountId);
    }

    public UUID getAccountId() {
        return getParameterConverter().toUuid(getData(), PARAMETER_ACCOUNT_ID);
    }

    @Override
    public void setOwnerId(UUID id) {
        setAccountId(id);
    }
}
