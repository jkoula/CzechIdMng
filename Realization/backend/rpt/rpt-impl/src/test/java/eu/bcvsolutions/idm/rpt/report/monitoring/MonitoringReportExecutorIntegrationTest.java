package eu.bcvsolutions.idm.rpt.report.monitoring;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.monitoring.service.impl.H2DatabaseMonitoringEvaluator;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.dto.RptMonitoringResultDto;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Monitoring report tests.
 * 
 * @author Radek Tomiška
 *
 */
public class MonitoringReportExecutorIntegrationTest extends AbstractIntegrationTest {

	@Autowired private MonitoringReportExecutor reportExecutor;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private ObjectMapper mapper;
	@Autowired private MonitoringReportXlsxRenderer xlsxRenderer;
	@Autowired private H2DatabaseMonitoringEvaluator h2DatabaseMonitoringEvaluator;
	@Autowired private IdmMonitoringService monitoringService;
	@Autowired private ConfigurationService configurationService;
	@Autowired private MonitoringManager monitoringManager;
	
	@Before
	public void before() {
		// report checks authorization policies - we need to log in
		loginAsAdmin();
	}
	
	@After
	public void after() {
		super.logout();
	}
	
	@Test
	@Transactional
	public void testExecutor() throws IOException {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		monitoringManager.execute(monitoring);
		//
		// generate report
		RptReportDto report = reportExecutor.generate(new RptReportDto(UUID.randomUUID()));
		Assert.assertNotNull(report.getData());
		List<RptMonitoringResultDto> results = mapper.readValue(
				attachmentManager.getAttachmentData(report.getData()), 
				new TypeReference<List<RptMonitoringResultDto>>(){});
		//
		// test
		Assert.assertFalse(results.isEmpty());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getEvaluatorType().equals(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator))));
		//
		attachmentManager.deleteAttachments(report);
	}
	
	@Test
	@Transactional
	public void testRenderers() {
		IdmMonitoringDto monitoring = new IdmMonitoringDto();
		monitoring.setCheckPeriod(0L);
		monitoring.setEvaluatorType(AutowireHelper.getTargetType(h2DatabaseMonitoringEvaluator));
		monitoring.setInstanceId(configurationService.getInstanceId());
		monitoring = monitoringService.save(monitoring);
		monitoringManager.execute(monitoring);
		//
		RptReportDto report = new RptReportDto(UUID.randomUUID());
		report.setExecutorName(reportExecutor.getName());
		//
		// generate report
		report = reportExecutor.generate(report);
		//
		Assert.assertNotNull(xlsxRenderer.render(report));
	}
}
