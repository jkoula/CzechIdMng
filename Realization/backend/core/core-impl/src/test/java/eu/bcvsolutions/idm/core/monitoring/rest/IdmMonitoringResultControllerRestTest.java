package eu.bcvsolutions.idm.core.monitoring.rest;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.collections.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.service.impl.H2DatabaseMonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.service.impl.TestMonitoringEvaluator;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.TestHelper;

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
	@Autowired private IdmMonitoringResultService monitoringResultService;
	@Autowired private H2DatabaseMonitoringEvaluator h2DatabaseMonitoringEvaluator;
	@Autowired private ConfigurationService configurationService;
	@Autowired private MonitoringManager monitoringManager;
	@Autowired private IdmCacheManager cacheManager;
	
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
	
	@Test
	public void testFindByText() {
		IdmMonitoringResultDto result = prepareDto();
		IdmMonitoringDto monitoringOne = new IdmMonitoringDto();
		monitoringOne.setEvaluatorType(getHelper().createName());
		monitoringOne.setInstanceId("mock");
		monitoringOne = monitoringService.save(monitoringOne);
		result.setMonitoring(monitoringOne.getId());
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		result.setOwnerId(owner.getId());
		result.setOwnerType(IdmIdentity.class.getCanonicalName());
		IdmMonitoringResultDto resultOne = createDto(result);
		//
		result = prepareDto();
		IdmMonitoringDto monitoringTwo = new IdmMonitoringDto();
		monitoringTwo.setEvaluatorType(TestMonitoringEvaluator.class.getCanonicalName());
		monitoringTwo.setDescription(monitoringOne.getEvaluatorType());
		monitoringTwo.setInstanceId("mock");
		monitoringTwo = monitoringService.save(monitoringTwo);
		result.setMonitoring(monitoringTwo.getId());
		result.setEvaluatorType(TestMonitoringEvaluator.class.getCanonicalName());
		IdmMonitoringResultDto resultTwo = createDto(result);
		//
		createDto(); // other
		//
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
		filter.setText(monitoringOne.getEvaluatorType());
		List<IdmMonitoringResultDto> results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(resultOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(resultTwo.getId())));
	}
	
	@Test
	public void testFindByLevel() {
		String instanceId = getHelper().createName();
		IdmMonitoringResultDto result = prepareDto();
		result.setInstanceId(instanceId);
		result.setLevel(NotificationLevel.ERROR);
		IdmMonitoringResultDto resultOne = createDto(result);
		result = prepareDto();
		result.setInstanceId(instanceId);
		result.setLevel(NotificationLevel.WARNING);
		IdmMonitoringResultDto resultTwo = createDto(result);
		result = prepareDto();
		result.setInstanceId(instanceId);
		result.setLevel(NotificationLevel.INFO);
		createDto(result); // other
		//
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
		filter.setInstanceId(instanceId);
		filter.setLevels(Lists.newArrayList(NotificationLevel.ERROR, NotificationLevel.WARNING));
		List<IdmMonitoringResultDto> results = find(filter);
		//
		Assert.assertEquals(2, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(resultOne.getId())));
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(resultTwo.getId())));
	}
	
	@Test
	public void testFindByLastResult() {
		String instanceId = getHelper().createName();
		IdmMonitoringResultDto result = prepareDto();
		result.setInstanceId(instanceId);
		result.setLastResult(true);
		IdmMonitoringResultDto resultOne = createDto(result);
		result = prepareDto();
		result.setInstanceId(instanceId);
		createDto(result); // other
		//
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
		filter.setInstanceId(instanceId);
		filter.setLastResult(true);
		List<IdmMonitoringResultDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(resultOne.getId())));
	}
	
	@Test
	public void testFindLastResults() {
		// evaluate monitoring
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(3600L); // ~ hour
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		//
		monitoringManager.execute(monitoring);
		monitoringManager.execute(monitoring);
		monitoringManager.execute(monitoring);
		//
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
		filter.setMonitoring(monitoring.getId());
		// workaround: result model cannot be deserialized automatically from json
		monitoringResultService.find(filter, null).forEach(result -> {
			result.setResult(new OperationResultDto(OperationState.EXECUTED));
			monitoringResultService.save(result);
		});
		cacheManager.evictCache(MonitoringManager.LAST_RESULT_CACHE_NAME);
		//
		filter.setLastResult(true);
		List<IdmMonitoringResultDto> lastResults = find(filter);
		List<IdmMonitoringResultDto> results = find("last-results", filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(1, lastResults.size());
		Assert.assertEquals(lastResults.get(0).getId(), results.get(0).getId());
	}
	
	@Test
	public void testExecute() throws Exception {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring =  monitoringService.save(monitoring);
		//
		IdmMonitoringResultDto dto = new IdmMonitoringResultDto();
		dto.setMonitoring(monitoring.getId());
		dto.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		dto.setInstanceId(configurationService.getInstanceId());
		dto.setResult(new OperationResultDto(OperationState.BLOCKED));
		dto = monitoringResultService.save(dto);
		//
		String response = getMockMvc().perform(put(String.format("%s/execute", getDetailUrl(dto.getId())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isCreated())
				.andReturn()
                .getResponse()
                .getContentAsString();
		Assert.assertNotNull(response);
		//
		getMockMvc().perform(put(String.format("%s/execute", getDetailUrl(UUID.randomUUID())))
        		.with(authentication(getAdminAuthentication()))
                .contentType(TestHelper.HAL_CONTENT_TYPE))
				.andExpect(status().isNotFound());
	}
}
