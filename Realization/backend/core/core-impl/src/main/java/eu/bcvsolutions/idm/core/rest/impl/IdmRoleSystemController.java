package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleSystemFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import java.util.Set;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * IdM role-system controller - (we need to work with role-system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/core/role-systems")
@Api(
		value = IdmRoleSystemController.TAG,
		description = "IdM role-system controller - (we need to work with role-system also in the core module)",
		tags = {IdmRoleSystemController.TAG},
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmRoleSystemController extends AbstractReadDtoController<IdmRoleSystemDto, IdmRoleSystemFilter> {

	protected static final String TAG = "IdM role-system controller - (we need to work with role-system also in the core module).";
	
	@Autowired
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public IdmRoleSystemController(IdmRoleSystemService service) {
		super((ReadDtoService) service);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search system roles (/search/quick alias)",
			nickname = "searchSystemRoles",
			tags = {IdmRoleSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
			})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "Search system roles",
			nickname = "searchQuickSystemRoles",
			tags = {IdmRoleSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
			})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete system roles (selectbox usage)",
			nickname = "autocompleteSystemRoles",
			tags = {IdmRoleSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_AUTOCOMPLETE, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_AUTOCOMPLETE, description = "")})
			})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

//	@ResponseBody
//	@RequestMapping(value= "/search/can-be-requested", method = RequestMethod.GET)
//	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_CANBEREQUESTED + "')")
//	@ApiOperation(
//			value = "Find assigned roles, which can be requested", 
//			nickname = "findCanBeRequestedSystemRoles", 
//			tags = { IdmRoleSystemController.TAG }, 
//			authorizations = { 
//				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
//						@AuthorizationScope(scope = CoreGroupPermission.ROLE_CANBEREQUESTED, description = "") }),
//				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
//						@AuthorizationScope(scope = CoreGroupPermission.ROLE_CANBEREQUESTED, description = "") })
//				})
//	public Resources<?> findCanBeRequested(
//			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
//			@PageableDefault Pageable pageable) {
//		return toResources(find(toFilter(parameters), pageable, RoleBasePermission.CANBEREQUESTED), getDtoClass());
//	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter",
			nickname = "countSystemRoles",
			tags = {IdmRoleSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_COUNT, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_COUNT, description = "")})
			})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "System role detail",
			nickname = "getSystemRole",
			response = IdmRoleSystemDto.class,
			tags = {IdmRoleSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
			})
	public ResponseEntity<?> get(
			@ApiParam(value = "System role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record",
			nickname = "getPermissionsOnRoleSystem",
			tags = {IdmRoleSystemController.TAG},
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_READ, description = "")})
			})
	public Set<String> getPermissions(
			@ApiParam(value = "System role's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
