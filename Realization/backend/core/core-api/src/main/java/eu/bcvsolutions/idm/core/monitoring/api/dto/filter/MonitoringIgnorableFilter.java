package eu.bcvsolutions.idm.core.monitoring.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseDataFilter;

/**
 * Filter for entities which are (not) ignored from monitoring.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
public interface MonitoringIgnorableFilter extends BaseDataFilter {

	String PARAMETER_MONITORING_IGNORED = "monitoringIgnored"; 

	/**
	 * Filter for entities which are (not) ignored from monitoring.
	 *
	 * @return true - entities ignored from monitoring / false - not ignored
	 */
	default Boolean getMonitoringIgnored() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_MONITORING_IGNORED);
	}

	/**
	 * Filter for filtering entities which are (not) ignored from monitoring.
	 *
	 * @param monitoringIgnored true - entities ignored from monitoring / false - not ignored
	 */
	default void setMonitoringIgnored(Boolean monitoringIgnored) {
		set(PARAMETER_MONITORING_IGNORED, monitoringIgnored);
	}
}
