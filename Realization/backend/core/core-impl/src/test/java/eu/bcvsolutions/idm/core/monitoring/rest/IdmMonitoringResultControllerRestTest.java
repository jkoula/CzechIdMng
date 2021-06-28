package eu.bcvsolutions.idm.core.monitoring.rest;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;

/**
 * Controller tests
 * - all filters.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmMonitoringResultControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmMonitoringResultDto> {

	@Autowired private IdmMonitoringResultController controller;
	@Autowired private IdmMonitoringService monitoringService;
	
	@Override
	protected AbstractReadWriteDtoController<IdmMonitoringResultDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmMonitoringResultDto prepareDto() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setEvaluatorType("mock");
		monitoring.setInstanceId("mock");
		monitoring = monitoringService.save(monitoring);
		//
		IdmMonitoringResultDto dto = new IdmMonitoringResultDto();
		dto.setMonitoring(monitoring.getId());
		dto.setEvaluatorType("mock");
		dto.setInstanceId("mock");
		dto.setResult(new OperationResultDto(OperationState.BLOCKED));
		//
		return dto;
	}
}
