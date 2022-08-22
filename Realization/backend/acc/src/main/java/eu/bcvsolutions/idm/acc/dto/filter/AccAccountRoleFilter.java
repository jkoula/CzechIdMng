package eu.bcvsolutions.idm.acc.dto.filter;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class AccAccountRoleFilter extends BaseRoleAssignmentFilter {

    public AccAccountRoleFilter(Class<? extends BaseDto> dtoClass) {
        super(dtoClass);
    }

    public AccAccountRoleFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
        super(dtoClass, data);
    }

    public AccAccountRoleFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(dtoClass, data, parameterConverter);
    }

    public AccAccountRoleFilter(MultiValueMap<String, Object> data) {
        super(AccAccountRoleDto.class, data);
    }

    public AccAccountRoleFilter() {
        this(new LinkedMultiValueMap<>());
    }
}
