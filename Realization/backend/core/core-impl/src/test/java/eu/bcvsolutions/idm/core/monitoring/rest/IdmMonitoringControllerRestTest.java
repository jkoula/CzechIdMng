package eu.bcvsolutions.idm.core.monitoring.rest;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;

/**
 * Controller tests
 * - all filters.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmMonitoringControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmMonitoringDto> {

	@Autowired private IdmMonitoringController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmMonitoringDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmMonitoringDto prepareDto() {
		IdmMonitoringDto dto = new IdmMonitoringDto();
		dto.setEvaluatorType("mock");
		dto.setInstanceId("mock");
		//
		return dto;
	}
}
