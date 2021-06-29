package eu.bcvsolutions.idm.core.monitoring.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DisableableFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;

/**
 * Filter for configured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class IdmMonitoringFilter extends DataFilter implements DisableableFilter {
	
	public static final String PARAMETER_INSTANCE_ID = ConfigurationService.PROPERTY_INSTANCE_ID;
	
	public IdmMonitoringFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmMonitoringFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmMonitoringFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmMonitoringDto.class, data, parameterConverter);
	}
	
	/**
	 * Filter by server instance identifier.
	 *  
	 * @return server instance identifier
	 */
	public String getInstanceId() {
		return getParameterConverter().toString(getData(), PARAMETER_INSTANCE_ID);
	}
	
	/**
	 * Filter by server instance identifier.
	 * 
	 * @param instanceId server instance identifier
	 */
	public void setInstanceId(String instanceId) {
		set(PARAMETER_INSTANCE_ID, instanceId);
	}

}
