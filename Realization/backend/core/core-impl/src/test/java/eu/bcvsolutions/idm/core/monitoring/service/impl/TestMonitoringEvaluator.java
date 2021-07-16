package eu.bcvsolutions.idm.core.monitoring.service.impl;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.AbstractMonitoringEvaluator;

/**
 * Test evaluator.
 * 
 * @author Radek Tomiška
 */
@Component
public class TestMonitoringEvaluator extends AbstractMonitoringEvaluator {

	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		return null;
	}
	
	@Override
	public IdmFormInstanceDto getFormInstance(ConfigurationMap properties) {
		return new IdmFormInstanceDto(getFormDefinition());
	}

}
