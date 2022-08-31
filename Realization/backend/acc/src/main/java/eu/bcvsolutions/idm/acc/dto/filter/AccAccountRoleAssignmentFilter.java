package eu.bcvsolutions.idm.acc.dto.filter;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class AccAccountRoleAssignmentFilter extends BaseRoleAssignmentFilter {

    public AccAccountRoleAssignmentFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public AccAccountRoleAssignmentFilter(MultiValueMap<String, Object> data) {
        super(AccAccountRoleAssignmentDto.class, data);
    }

}
