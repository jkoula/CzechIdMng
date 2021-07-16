package eu.bcvsolutions.idm.core.monitoring.rest;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.service.impl.DemoAdminMonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.service.impl.TestMonitoringEvaluator;

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
	
	@Test
	public void testFindByText() {
		IdmMonitoringDto monitoring = prepareDto();
		monitoring.setEvaluatorType(getHelper().createName());
		monitoring.setInstanceId("mock");
		IdmMonitoringDto monitoringOne = createDto(monitoring);
		//
		monitoring = prepareDto();
		monitoring.setEvaluatorType(TestMonitoringEvaluator.class.getCanonicalName());
		monitoring.setDescription(monitoringOne.getEvaluatorType());
		monitoring.setInstanceId("mock");
		IdmMonitoringDto monitoringTwo = createDto(monitoring);
		//
		monitoring = prepareDto();
		monitoring.setEvaluatorType(DemoAdminMonitoringEvaluator.class.getCanonicalName());
		monitoring.setDescription(monitoringOne.getEvaluatorType());
		monitoring.setInstanceId("mock");
		IdmMonitoringDto monitoringThree = createDto(monitoring);
		//
		createDto(); // other
		//
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setText(monitoringOne.getEvaluatorType());
		List<IdmMonitoringDto> results = find(filter);
		//
		Assert.assertEquals(3, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(monitoringOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(monitoringTwo.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(monitoringThree.getId())));
	}
}
