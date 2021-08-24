package eu.bcvsolutions.idm.rpt.bulk.action.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.rpt.entity.RptReport;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete report integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class ReportDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private RptReportService service;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void processBulkActionByIds() {
		List<RptReportDto> reports = createReports(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(RptReport.class, ReportDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(reports);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void processBulkActionByFilter() {
		List<RptReportDto> reports = createReports(5);
		
		RptReportFilter filter = new RptReportFilter();
		filter.setId(reports.get(2).getId());

		List<RptReportDto> checkReports = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkReports.size());

		IdmBulkActionDto bulkAction = findBulkAction(RptReport.class, ReportDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(reports.get(2)));
		Assert.assertNotNull(service.get(reports.get(1)));
		Assert.assertNotNull(service.get(reports.get(3)));
	}
	
	private List<RptReportDto> createReports(int count) {
		List<RptReportDto> results = new ArrayList<>();
		
		for (int i = 0; i < count; i++) {
			RptReportDto dto = new RptReportDto();
			dto.setName(getHelper().createName());
			dto.setExecutorName(getHelper().createName());
			//
			results.add(service.save(dto));
		}
		
		return results;
	}
}
