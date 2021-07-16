package eu.bcvsolutions.idm.core.monitoring.api.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;

/**
 * Registered monitoring evaluator - evaluate configured monitoring.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface MonitoringEvaluator extends Ordered, Configurable {
	
	@Override
	default String getConfigurableType() {
		return "monitoring-evaluator";
	}
	
	/**
	 *  bean name / unique identifier (spring bean name)
	 *  
	 * @return
	 */
	String getId();
	
	/**
	 * Returns configuration property names for this configurable object.
	 */
	@Override
	default List<String> getPropertyNames() {
		return new ArrayList<>();
	}

	/**
	 * Evaluate monitoring result by given configuration.
	 * 
	 * @param monitoring configured monitoring
	 * @return monitoring result
	 */
	IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring);
}
