package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.MonitoringEvaluatorDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent;
import eu.bcvsolutions.idm.core.monitoring.api.event.MonitoringEvent.MonitoringEventType;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringResultService;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Monitoring test.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultMonitoringManagerIntegrationTest extends AbstractIntegrationTest {

	@Autowired private ApplicationContext context;
	@Autowired private H2DatabaseMonitoringEvaluator h2DatabaseMonitoringEvaluator;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private IdmMonitoringResultService monitoringResultService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private IdmCacheManager cacheManager;
	//
	private DefaultMonitoringManager manager;
	
	@Before
	public void init() {
		manager = context.getAutowireCapableBeanFactory().createBean(DefaultMonitoringManager.class);
	}
	
	@Test
	public void testReferentiralIntegrity() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		//
		manager.scheduleExecute();
		//
		IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
		filter.setMonitoring(monitoring.getId());
		//
		List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
		Assert.assertEquals(1, results.size()); // executed only once => 0 check period
		//
		monitoringService.delete(monitoring);
		//
		results = monitoringResultService.find(filter, null).getContent();
		Assert.assertTrue(results.isEmpty());
	}
	
	@Test
	public void testSupportedEvaluators() {
		List<MonitoringEvaluatorDto> supportedEvaluators = manager.getSupportedEvaluators();
		//
		Assert.assertFalse(supportedEvaluators.isEmpty());
		Assert.assertTrue(supportedEvaluators.stream().anyMatch(e -> e.getName().equals(h2DatabaseMonitoringEvaluator.getName())));
	}
	
	@Test
	public void testRegisterMonitoringEvaluator() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		try {
			//
			manager.scheduleExecute();
			manager.scheduleExecute();
			manager.scheduleExecute();
			//
			IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
			filter.setMonitoring(monitoring.getId());
			//
			List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
			Assert.assertEquals(1, results.size()); // executed only once => 0 check period
			//
			List<IdmMonitoringResultDto> lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
			//
			manager.init();
			//
			lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());			
		} finally {
			monitoringService.delete(monitoring);
		}
	}
	
	@Test
	public void testRegisterMonitoringEvaluatorWithCheckPeriod() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(3600L); // ~ hour
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		try {
			//
			manager.scheduleExecute();
			manager.scheduleExecute();
			manager.scheduleExecute();
			//
			IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
			filter.setMonitoring(monitoring.getId());
			//
			List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
			Assert.assertEquals(1, results.size()); // executed only once => 0 check period
			//
			List<IdmMonitoringResultDto> lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
			//
			manager.init();
			//
			lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
			//
			// evict and load cache again
			cacheManager.evictCache(MonitoringManager.LAST_RESULT_CACHE_NAME);
			lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
		} finally {
			monitoringService.delete(monitoring);
		}
	}
	
	@Test
	public void testRegisterMonitoringEvaluatorWithoutCheckPeriod() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(null); // ~ 0
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		try {
			//
			manager.scheduleExecute();
			manager.scheduleExecute();
			manager.scheduleExecute();
			//
			IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
			filter.setMonitoring(monitoring.getId());
			//
			List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
			Assert.assertEquals(1, results.size()); // executed only once => 0 check period
			//
			List<IdmMonitoringResultDto> lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
			//
			manager.init();
			//
			lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
			//
			// evict and load cache again
			cacheManager.evictCache(MonitoringManager.LAST_RESULT_CACHE_NAME);
			lastResults = manager.getLastResults(filter, null).getContent();
			//
			Assert.assertEquals(1, lastResults.size());
			Assert.assertEquals(results.get(0).getId(), lastResults.get(0).getId());
		} finally {
			monitoringService.delete(monitoring);
		}
	}
	
	@Test
	public void testNotExecuteBeforeExecuteDate() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring.setExecuteDate(ZonedDateTime.now().plusHours(1));
		monitoring = monitoringService.save(monitoring);
		try {
			//
			manager.scheduleExecute();
			//
			IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
			filter.setMonitoring(monitoring.getId());
			//
			List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
			Assert.assertTrue(results.isEmpty()); // executed only once => 0 check period		
		} finally {
			monitoringService.delete(monitoring);
		}
	}
	
	@Test
	public void testNotExecuteDisabledMonitoring() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring.setDisabled(true);
		monitoring = monitoringService.save(monitoring);
		try {
			//
			manager.scheduleExecute();
			//
			IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
			filter.setMonitoring(monitoring.getId());
			//
			List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
			Assert.assertTrue(results.isEmpty()); // executed only once => 0 check period		
		} finally {
			monitoringService.delete(monitoring);
		}
	}
	
	@Test
	@Transactional
	public void testExecuteMonitoringAfterSave() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring.setDisabled(false);
		//
		MonitoringEvent event = new MonitoringEvent(MonitoringEventType.CREATE, monitoring);
		event.setPriority(PriorityType.HIGH);
		monitoring = monitoringService.publish(event).getContent();
		try {
			IdmMonitoringResultFilter filter = new IdmMonitoringResultFilter();
			filter.setMonitoring(monitoring.getId());
			List<IdmMonitoringResultDto> results = monitoringResultService.find(filter, null).getContent();
			Assert.assertFalse(results.isEmpty());		
		} finally {
			monitoringService.delete(monitoring);
		}
	}
}
