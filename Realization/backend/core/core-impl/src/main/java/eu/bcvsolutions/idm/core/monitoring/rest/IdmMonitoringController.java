package eu.bcvsolutions.idm.core.monitoring.rest;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.MonitoringEvaluatorDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.IdmMonitoringService;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * Configgured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/monitorings")
@Api(
		value = IdmMonitoringController.TAG, 
		description = "Operations with configured monitoring evaluators", 
		tags = { IdmMonitoringController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmMonitoringController extends AbstractEventableDtoController<IdmMonitoringDto, IdmMonitoringFilter> {
	
	protected static final String TAG = "Configured monitoring evaluators";
	
	@Autowired private MonitoringManager monitoringManager;
	
	@Autowired
	public IdmMonitoringController(IdmMonitoringService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Search configured monitoring evaluators (/search/quick alias)", 
			nickname = "searchMonitorings", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Search configured monitoring evaluators", 
			nickname = "searchQuickMonitorings", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete configured monitoring evaluators (selectbox usage)", 
			nickname = "autocompleteMonitorings", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countMonitorings", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Monitoring detail", 
			nickname = "getMonitoring", 
			response = IdmMonitoringDto.class, 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_CREATE + "')"
			+ " or hasAuthority('" + MonitoringGroupPermission.MONITORING_UPDATE + "')")
	@ApiOperation(
			value = "Create / update configured monitoring evaluator", 
			nickname = "postMonitoring", 
			response = IdmMonitoringDto.class, 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_CREATE, description = ""),
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_CREATE, description = ""),
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody IdmMonitoringDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_UPDATE + "')")
	@ApiOperation(
			value = "Update configured monitoring evaluator", 
			nickname = "putMonitoring", 
			response = IdmMonitoringDto.class, 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody IdmMonitoringDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_UPDATE + "')")
	@ApiOperation(
			value = "Update configured monitoring evaluator", 
			nickname = "patchMonitoring", 
			response = IdmMonitoringDto.class, 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_DELETE + "')")
	@ApiOperation(
			value = "Delete configured monitoring evaluator", 
			nickname = "deleteMonitoring", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Monitoring's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')"
			+ " or hasAuthority('" + MonitoringGroupPermission.MONITORING_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnMonitoring", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = ""),
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = ""),
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Monitoring's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions", 
			nickname = "availableBulkAction", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "") })
				})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Process bulk action", 
			nickname = "bulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "")})
				})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action", 
			nickname = "prevalidateBulkAction", 
			response = IdmBulkActionDto.class, 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "")})
				})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}
	
	/**
	 * Returns all registered evaluators
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@PreAuthorize("hasAuthority('" + MonitoringGroupPermission.MONITORING_AUTOCOMPLETE + "') "
			+ "or hasAuthority('" + MonitoringGroupPermission.MONITORING_READ + "')")
	@ApiOperation(
			value = "Get all supported evaluators", 
			nickname = "getSupportedMonitoringEvaluators", 
			tags = { IdmMonitoringController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_AUTOCOMPLETE, description = ""),
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_AUTOCOMPLETE, description = ""),
						@AuthorizationScope(scope = MonitoringGroupPermission.MONITORING_READ, description = "")})
				})
	public Resources<MonitoringEvaluatorDto> getSupportedEvaluators() {
		return new Resources<>(monitoringManager.getSupportedEvaluators());
	}

	@Override
	protected IdmMonitoringFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new IdmMonitoringFilter(parameters, getParameterConverter());
	}
}
