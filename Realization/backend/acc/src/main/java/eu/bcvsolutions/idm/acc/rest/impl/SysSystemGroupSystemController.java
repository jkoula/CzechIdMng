package eu.bcvsolutions.idm.acc.rest.impl;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.rest.AbstractEventableDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import java.util.List;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
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

/**
 * System groups system - relation between a system and a group of systems.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-group-systems")
@Api(
		value = SysSystemGroupSystemController.TAG,
		description = "System groups system - relation between a system and a group of systems.",
		tags = {SysSystemGroupSystemController.TAG},
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemGroupSystemController extends AbstractEventableDtoController<SysSystemGroupSystemDto, SysSystemGroupSystemFilter> {

	protected static final String TAG = "System groups system - relation between a system and a group of systems.";

	@Autowired
	public SysSystemGroupSystemController(SysSystemGroupSystemService service) {
		super(service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@ApiOperation(
			value = "Search configured SystemGroupSystem relations (/search/quick alias)",
			nickname = "searchSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")})
			})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@ApiOperation(
			value = "Search configured SystemGroupSystem relations",
			nickname = "searchQuickSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")})
			})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete configured SystemGroupSystem relations (selectbox usage)",
			nickname = "autocompleteSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE, description = "")})
			})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter",
			nickname = "countSystemGroupSystems",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_COUNT, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_COUNT, description = "")})
			})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@ApiOperation(
			value = "System group-system detail",
			nickname = "getSystemGroupSystem",
			response = SysSystemGroupSystemDto.class,
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")})
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "System group-system's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE + "')")
	@ApiOperation(
			value = "Create / update configured SystemGroupSystem relation",
			nickname = "postSystemGroupSystem",
			response = SysSystemGroupSystemDto.class,
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_CREATE, description = ""),
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_CREATE, description = ""),
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE, description = "")})
			})
	public ResponseEntity<?> post(@Valid @RequestBody SysSystemGroupSystemDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE + "')")
	@ApiOperation(
			value = "Update configured SystemGroupSystem relation",
			nickname = "putSystemGroupSystem",
			response = SysSystemGroupSystemDto.class,
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_UPDATE, description = "")})
			})
	public ResponseEntity<?> put(
			@ApiParam(value = "System group-system's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId,
			@Valid @RequestBody SysSystemGroupSystemDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_DELETE + "')")
	@ApiOperation(
			value = "Delete configured SystemGroupSystem relation",
			nickname = "deleteSystemGroupSystem",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_DELETE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_DELETE, description = "")})
			})
	public ResponseEntity<?> delete(
			@ApiParam(value = "System group-system's uuid identifier or username.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record",
			nickname = "getPermissionsOnSystemGroupSystem",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = ""),
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = ""),
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_AUTOCOMPLETE, description = "")})
			})
	public Set<String> getPermissions(
			@ApiParam(value = "System group-system's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/bulk/actions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@ApiOperation(
			value = "Get available bulk actions",
			nickname = "availableBulkAction",
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")})
			})
	public List<IdmBulkActionDto> getAvailableBulkActions() {
		return super.getAvailableBulkActions();
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/action", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@ApiOperation(
			value = "Process bulk action",
			nickname = "bulkAction",
			response = IdmBulkActionDto.class,
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")})
			})
	public ResponseEntity<IdmBulkActionDto> bulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.bulkAction(bulkAction);
	}

	@Override
	@ResponseBody
	@RequestMapping(path = "/bulk/prevalidate", method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ + "')")
	@ApiOperation(
			value = "Prevalidate bulk action",
			nickname = "prevalidateBulkAction",
			response = IdmBulkActionDto.class,
			tags = {SysSystemGroupSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = AccGroupPermission.SYSTEM_GROUP_SYSTEM_READ, description = "")})
			})
	public ResponseEntity<ResultModels> prevalidateBulkAction(@Valid @RequestBody IdmBulkActionDto bulkAction) {
		return super.prevalidateBulkAction(bulkAction);
	}


	@Override
	protected SysSystemGroupSystemFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new SysSystemGroupSystemFilter(parameters, getParameterConverter());
	}
}
