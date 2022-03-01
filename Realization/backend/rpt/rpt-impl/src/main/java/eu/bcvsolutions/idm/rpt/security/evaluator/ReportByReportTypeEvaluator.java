package eu.bcvsolutions.idm.rpt.security.evaluator;

import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import eu.bcvsolutions.idm.rpt.entity.RptReport;
import eu.bcvsolutions.idm.rpt.entity.RptReport_;
import eu.bcvsolutions.idm.rpt.service.DefaultReportManager;

/**
 * Permission to reports by report executor type.
 * This evaluator works a little bit differently because it also explicitly
 * limit available reports in {@link DefaultReportManager}.
 *
 * @author Tomáš Doischer
 * @since 12.2.0
 *
 */
@Component(ReportByReportTypeEvaluator.EVALUATOR_NAME)
@Description("")
public class ReportByReportTypeEvaluator extends AbstractAuthorizationEvaluator<RptReport> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ReportByReportTypeEvaluator.class);
	public static final String PARAMETER_REPORT_TYPE = "report-type";
	public static final String EVALUATOR_NAME = "rpt-report-by-report-type-evaluator";
	
	@Autowired
	private RptReportService reportService;
	@Autowired
	private SecurityService securityService;
	
	@Override
	public Predicate getPredicate(Root<RptReport> root, CriteriaQuery<?> query, CriteriaBuilder builder, AuthorizationPolicy policy, BasePermission... permission) {
		if (!hasPermission(policy, permission)) {
			return null;
		}
		if (!securityService.isAuthenticated()) {
			return null;
		}
		return builder.equal(root.get(RptReport_.executorName), getReportName(policy));
	}
	
	@Override
	public Set<String> getPermissions(RptReport authorizable, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(authorizable, policy);
		String reportName = getReportName(policy);
		if (reportName == null) {
			return permissions;
		}
		
		RptReportFilter filter = new RptReportFilter();
		filter.setExecutorName(reportName);
		long reportCount = reportService.count(filter);
		
		if (reportCount > 0) {
			permissions.addAll(policy.getPermissions());
		}

		return permissions;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto reportTypeFormAttribute = new IdmFormAttributeDto(PARAMETER_REPORT_TYPE, 
				PARAMETER_REPORT_TYPE, 
				PersistentType.TEXT, 
				BaseFaceType.TEXTAREA);
		reportTypeFormAttribute.setRequired(true);
		return Lists.newArrayList(reportTypeFormAttribute);
	}

	@Override
	public String getName() {
		return EVALUATOR_NAME;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_REPORT_TYPE);
		return parameters;
	}
	
	private String getReportName(AuthorizationPolicy policy) {
		try {
			return policy.getEvaluatorProperties().getString(PARAMETER_REPORT_TYPE);
		} catch (ClassCastException ex) {
			LOG.warn("Wrong evaluator settings - skipping.", ex);
			return null;
		}
	}
}
