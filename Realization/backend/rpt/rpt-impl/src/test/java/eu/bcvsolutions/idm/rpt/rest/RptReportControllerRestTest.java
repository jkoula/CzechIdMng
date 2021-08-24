package eu.bcvsolutions.idm.rpt.rest;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;

/**
 * Controller test.
 * 
 * @author Radek Tomiška
 */
public class RptReportControllerRestTest extends AbstractReadWriteDtoControllerRestTest<RptReportDto> {

	@Autowired private RptReportController controller;
	
	@Override
	protected AbstractReadWriteDtoController<RptReportDto, ?> getController() {
		return controller;
	}

	@Override
	protected RptReportDto prepareDto() {
		RptReportDto report = new RptReportDto();
		report.setName(getHelper().createName());
		report.setExecutorName(getHelper().createName());
		//
		return report;
	}

	@Override
	protected boolean isReadOnly() {
		return true;
	}
	
	@Override
	protected boolean supportsFormValues() {
		return false;
	}
}
