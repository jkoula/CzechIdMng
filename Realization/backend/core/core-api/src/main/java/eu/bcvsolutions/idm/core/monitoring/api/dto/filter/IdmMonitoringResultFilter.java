package eu.bcvsolutions.idm.core.monitoring.api.dto.filter;

import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.InstanceIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Filter for monitoring results.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class IdmMonitoringResultFilter extends DataFilter implements InstanceIdentifiableFilter {
	
	public static final String PARAMETER_MONITORING = "monitoring";
	public static final String PARAMETER_LEVEL = "level";
	public static final String PARAMETER_LAST_RESULT = "lastResult"; // false - all results, true - last results only
	
	public IdmMonitoringResultFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmMonitoringResultFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmMonitoringResultFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmMonitoringResultDto.class, data, parameterConverter);
	}
	
	public void setMonitoring(UUID monitoring) {
		set(PARAMETER_MONITORING, monitoring);
	}
	
	public UUID getMonitoring() {
		return getParameterConverter().toUuid(getData(), PARAMETER_MONITORING);
	}
	
	public List<NotificationLevel> getLevels() {
		return getParameterConverter().toEnums(getData(), PARAMETER_LEVEL, NotificationLevel.class);
	}
	
	public void setLevels(List<NotificationLevel> levels) {
		put(PARAMETER_LEVEL, levels);
	}
	
	/**
	 * Last result flag.
	 * 
	 * @return true - last result
	 * @since 11.2.0
	 */
	public boolean isLastResult() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_LAST_RESULT, false); // false - all results
	}

	/**
	 * Last result flag.
	 * 
	 * @param lastResult true - last result
	 * @since 11.2.0
	 */
	public void setLastResult(boolean lastResult) {
		set(PARAMETER_LAST_RESULT, lastResult);
	}

}
