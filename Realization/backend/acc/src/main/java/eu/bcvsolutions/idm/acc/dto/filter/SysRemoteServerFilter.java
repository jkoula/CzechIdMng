package eu.bcvsolutions.idm.acc.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ConfidentialContext;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for remote servers.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
public class SysRemoteServerFilter extends DataFilter {
	
	/**
	 * ~ Fill secret proxy string, if password is filled. 
	 * @deprecated use {@link ConfidentialContext#PARAMETER_ADD_SECRED_PROXY_STRING} instead
	 */
	@Deprecated(since = "11.3.0")
	public static final String PARAMETER_CONTAINS_PASSWORD = "containsPassword";
	
	public SysRemoteServerFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysRemoteServerFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysRemoteServerFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysConnectorServerDto.class, data, parameterConverter);
	}
	
	/**
	 * Password is filled info.
	 * 
	 * @param containsPassword include proxy chars in dto
	 */
	public void setContainsPassword(Boolean containsPassword) {
		set(PARAMETER_ADD_SECRED_PROXY_STRING, containsPassword);
	}

	/**
	 * Password is filled info.
	 * 
	 * @return true - include proxy chars in dto
	 */
	public Boolean getContainsPassword() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_ADD_SECRED_PROXY_STRING);
	}
}
