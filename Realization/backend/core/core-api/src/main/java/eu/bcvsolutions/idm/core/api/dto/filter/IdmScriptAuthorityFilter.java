package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptAuthorityDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for  {@link IdmScriptAuthorityDto}.
 * Filtering by script.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class IdmScriptAuthorityFilter extends DataFilter {
	
	public static final String PARAMETER_SCRIPT_ID = "scriptId";
	
	public IdmScriptAuthorityFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public IdmScriptAuthorityFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }
    
    public IdmScriptAuthorityFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(IdmScriptAuthorityDto.class, data, parameterConverter);
    }

	public UUID getScriptId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SCRIPT_ID);
	}

	public void setScriptId(UUID scriptId) {
		set(PARAMETER_SCRIPT_ID, scriptId);
	}
}
