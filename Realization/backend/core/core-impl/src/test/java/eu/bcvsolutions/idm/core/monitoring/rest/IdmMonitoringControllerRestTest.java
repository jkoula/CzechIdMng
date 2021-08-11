package eu.bcvsolutions.idm.core.monitoring.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.service.impl.DemoAdminMonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.service.impl.H2DatabaseMonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.service.impl.TestMonitoringEvaluator;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Controller tests
 * - all filters.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmMonitoringControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmMonitoringDto> {

	@Autowired private IdmMonitoringController controller;
	@Autowired private IdmMonitoringService service;
	@Autowired private ConfigurationService configurationService;
	@Autowired private H2DatabaseMonitoringEvaluator h2DatabaseMonitoringEvaluator;
	
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
	
	@Test
	public void testExecute() throws Exception {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		//
		monitoring = service.save(monitoring);
		//
		getMockMvc().perform(put(String.format("%s/execute", getDetailUrl(monitoring.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNoContent());
		//
		getMockMvc().perform(put(String.format("%s/execute", getDetailUrl(UUID.randomUUID())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
}
