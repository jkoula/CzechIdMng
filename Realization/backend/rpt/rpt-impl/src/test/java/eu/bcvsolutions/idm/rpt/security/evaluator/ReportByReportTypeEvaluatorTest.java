package eu.bcvsolutions.idm.rpt.security.evaluator;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.api.domain.RptGroupPermission;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.entity.RptReport;
import eu.bcvsolutions.idm.rpt.report.identity.IdentityReportExecutor;
import eu.bcvsolutions.idm.test.api.AbstractEvaluatorIntegrationTest;

public class ReportByReportTypeEvaluatorTest extends AbstractEvaluatorIntegrationTest {
	
	@Autowired
	private ReportManager manager;
	
	@Test
	public void getAvailableReportsTest() {
		IdmIdentityDto permissionTester = getHelper().createIdentity();
		try {			
			getHelper().login(permissionTester.getUsername(), permissionTester.getPassword());
			List<RptReportExecutorDto> executors = manager.getExecutors();
			// all reports should be available (this is independent of permissions)
			assertNotEquals(1, executors.size());
		} finally {
			logout();
		}
		//
		IdmRoleDto role = getHelper().createRole();
		getHelper().createIdentityRole(permissionTester, role);
		ConfigurationMap evaluatorProperties = new ConfigurationMap();
		evaluatorProperties.put(ReportByReportTypeEvaluator.PARAMETER_REPORT_TYPE,
				IdentityReportExecutor.REPORT_NAME);
		getHelper().createAuthorizationPolicy(
				role.getId(),
				RptGroupPermission.REPORT,
				RptReport.class,
				ReportByReportTypeEvaluator.class,
				evaluatorProperties,
				IdmBasePermission.CREATE);
		//
		try {			
			getHelper().login(permissionTester.getUsername(), permissionTester.getPassword());
			List<RptReportExecutorDto> executors = manager.getExecutors();
			// only one report should be available
			assertEquals(1, executors.size());
		} finally {
			logout();
		}
	}
}
