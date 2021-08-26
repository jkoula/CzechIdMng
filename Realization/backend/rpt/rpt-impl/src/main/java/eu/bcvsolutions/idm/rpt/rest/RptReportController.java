package eu.bcvsolutions.idm.rpt.rest;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.utils.SpinalCase;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.filter.IdmAttachmentFilter;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.rpt.RptModuleDescriptor;
import eu.bcvsolutions.idm.rpt.api.domain.RptGroupPermission;
import eu.bcvsolutions.idm.rpt.api.dto.RptRenderedReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportDto;
import eu.bcvsolutions.idm.rpt.api.dto.RptReportExecutorDto;
import eu.bcvsolutions.idm.rpt.api.dto.filter.RptReportFilter;
import eu.bcvsolutions.idm.rpt.api.service.ReportManager;
import eu.bcvsolutions.idm.rpt.api.service.RptReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Report controller.
 * 
 * @author Radek Tomiška
 *
 */
@RestController
@Enabled(RptModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/" + RptModuleDescriptor.MODULE_ID + "/reports")
@Api(
		value = RptReportController.TAG,  
		tags = { RptReportController.TAG }, 
		description = "Reports",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class RptReportController extends AbstractReadWriteDtoController<RptReportDto, RptReportFilter>  {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RptReportController.class);
	protected static final String TAG = "Reports";
	//
	@Autowired private AttachmentManager attachmentManager;
	//
	private final ReportManager reportManager;
	
	@Autowired
	public RptReportController(
			RptReportService service,
			ReportManager reportManager) {
		super(service);
		//
		this.reportManager = reportManager;
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Search reports (/search/quick alias)", 
			nickname = "searchReports", 
			tags = { RptReportController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Search reports", 
			nickname = "searchQuickReports", 
			tags = { RptReportController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete reports (selectbox usage)", 
			nickname = "autocompleteReports", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countReports", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Report detail", 
			nickname = "getReport", 
			response = RptReportDto.class, 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_CREATE + "')")
	@ApiOperation(
			value = "Create report", 
			nickname = "createReport", 
			response = RptReportDto.class, 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "")})
				})
	public ResponseEntity<?> createReport(@Valid @RequestBody RptReportDto report) {
		checkAccess(report, IdmBasePermission.CREATE);
		//
		return new ResponseEntity<>(toResource(reportManager.generate(report)), HttpStatus.ACCEPTED);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_DELETE + "')")
	@ApiOperation(
			value = "Delete report", 
			nickname = "deleteReport", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')"
			+ " or hasAuthority('" + RptGroupPermission.REPORT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnReport", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = ""),
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = ""),
						@AuthorizationScope(scope = RptGroupPermission.REPORT_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	
	@ResponseBody
	@RequestMapping(value = "/{backendId}/render", method = RequestMethod.GET)
	public ResponseEntity<InputStreamResource> renderReport(
			@ApiParam(value = "Report's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "Renderer's identifier.", required = true)
			@RequestParam(required = true, name = "renderer") @NotNull String rendererName) {
		//
		RptReportDto report = getDto(backendId);
		if (report == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		try {
			InputStream is = null;
			MediaType contentType = null;
			String reportName = null;
			//
			// try to find already rendered report as persisted attachment
			IdmAttachmentFilter attachmentFilter = new IdmAttachmentFilter();
			attachmentFilter.setOwnerId(report.getId());
			attachmentFilter.setOwnerType(attachmentManager.getOwnerType(report));
			attachmentFilter.setAttachmentType(rendererName);
			List<IdmAttachmentDto> attachments = attachmentManager.find(attachmentFilter, PageRequest.of(0, 1)).getContent();
			if (!attachments.isEmpty()) {
				IdmAttachmentDto attachment = attachments.get(0);
				UUID attachmentId = attachment.getId();
				contentType = MediaType.valueOf(attachment.getMimetype());
				reportName = attachment.getName();
				//
				try {
					is = attachmentManager.getAttachmentData(attachmentId);
				} catch(Exception ex) {
					LOG.warn("Attachment [{}] is not valid. Report [{}] will be rendered again by renderer [{}].",
							attachmentId, report.getExecutorName(), rendererName);
				}
			} 
			if (is == null) {
				RptRenderedReportDto result = reportManager.render(report, rendererName);
				is = result.getRenderedReport();
				contentType = result.getRenderer().getFormat();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
				reportName = String.format(
						"\"%s-%s.%s\"",
						SpinalCase.format(report.getExecutorName()),
						report.getCreated().format(formatter),
						result.getRenderer().getExtension()
				);
			}
			//
			return ResponseEntity.ok()
					.contentLength(is.available())
					.contentType(contentType)
					.header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", reportName))
					.body(new InputStreamResource(is));
		} catch (Exception ex) {
			throw new ResultCodeException(CoreResultCode.INTERNAL_SERVER_ERROR, ex);
		}
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Process bulk action for report", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action for reports", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { RptReportController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	@ResponseBody
	@RequestMapping(path = "/search/supported", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + RptGroupPermission.REPORT_CREATE + "')")
	@ApiOperation(
			value = "Get supported reports", 
			nickname = "getSupportedReports", 
			tags={ RptReportController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = RptGroupPermission.REPORT_CREATE, description = "") })
				})
	public Resources<RptReportExecutorDto> find() {
		return new Resources<>(reportManager.getExecutors());
	}
	
	@Override
	protected RptReportFilter toFilter(MultiValueMap<String, Object> parameters) {
		RptReportFilter filter = new RptReportFilter(parameters);
		//
		filter.setFrom(getParameterConverter().toDateTime(parameters, "from"));
		filter.setTill(getParameterConverter().toDateTime(parameters, "till"));
		//
		return filter;
	}

}
