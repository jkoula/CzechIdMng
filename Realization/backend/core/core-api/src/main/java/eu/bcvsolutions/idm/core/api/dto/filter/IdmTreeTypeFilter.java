package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for tree type.
 *
 * @author Radek Tomiška
 */
public class IdmTreeTypeFilter extends DataFilter implements ExternalIdentifiableFilter {

	public static final String PARAMETER_CODE = "code"; // PARAMETER_CODEABLE_IDENTIFIER can be used too

	public IdmTreeTypeFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmTreeTypeFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }
    
    public IdmTreeTypeFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(IdmTreeTypeDto.class, data, parameterConverter);
    }
    
    public String getCode() {
		return getParameterConverter().toString(getData(), PARAMETER_CODE);
	}

	public void setCode(String code) {
		set(PARAMETER_CODE, code);
	}
}
