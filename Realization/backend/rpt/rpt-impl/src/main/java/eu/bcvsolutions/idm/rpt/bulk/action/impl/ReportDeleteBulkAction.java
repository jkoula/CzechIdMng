package eu.bcvsolutions.idm.rpt.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.domain.RptGroupPermission;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;

/**
 * Delete given reports.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Enabled(RptModuleDescriptor.MODULE_ID)
@Component(ReportDeleteBulkAction.NAME)
@Description("Delete given reports.")
public class ReportDeleteBulkAction extends AbstractRemoveBulkAction<RptReportDto, RptReportFilter> {

	public static final String NAME = "rpt-report-delete-bulk-action";

	@Autowired private RptReportService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(RptGroupPermission.REPORT_DELETE);
	}

	@Override
	public ReadWriteDtoService<RptReportDto, RptReportFilter> getService() {
		return service;
	}
}
