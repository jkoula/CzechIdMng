package eu.bcvsolutions.idm.acc.rest.impl;

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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * System owner controller
 * 
 * @author Roman Kucera
 * @since 12.3.0
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + "/system-owners")
@Api(
		value = SysSystemOwnerController.TAG, 
		description = "Operations with identity system owners", 
		tags = { SysSystemOwnerController.TAG }, 
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemOwnerController extends AbstractReadWriteDtoController<SysSystemOwnerDto, SysSystemOwnerFilter> {
	
	protected static final String TAG = "System owners";
	
	@Autowired
	public SysSystemOwnerController(SysSystemOwnerService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@GetMapping
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@ApiOperation(
			value = "Search system owners (/search/quick alias)", 
			nickname = "searchSystemOwners", 
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/search/quick")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@ApiOperation(
			value = "Search system owners", 
			nickname = "searchQuickSystemOwners", 
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/search/autocomplete")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete system owners (selectbox usage)",
			nickname = "autocompleteSystemOwners",
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@GetMapping(value = "/search/count")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countSystemOwners",
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@GetMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@ApiOperation(
			value = "System owner detail",
			nickname = "getSystemOwner",
			response = SysSystemOwnerDto.class, 
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@PostMapping
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_CREATE + "')"
			+ " or hasAuthority('" + AccGroupPermission.SYSTEMOWNER_UPDATE + "')")
	@ApiOperation(
			value = "Create / update system owner",
			nickname = "postSystemOwner",
			response = SysSystemOwnerDto.class, 
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_CREATE, description = ""),
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody SysSystemOwnerDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@PutMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_UPDATE + "')")
	@ApiOperation(
			value = "Update system owner",
			nickname = "putSystemOwner",
			response = SysSystemOwnerDto.class, 
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody SysSystemOwnerDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@PatchMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_UPDATE + "')")
	@ApiOperation(
			value = "Update system owner",
			nickname = "patchSystemOwner",
			response = SysSystemOwnerDto.class, 
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@DeleteMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_DELETE + "')")
	@ApiOperation(
			value = "Delete system owner",
			nickname = "deleteSystemOwner",
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@GetMapping(value = "/{backendId}/permissions")
	@PreAuthorize("hasAuthority('" + AccGroupPermission.SYSTEMOWNER_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnSystemOwner",
			tags = { SysSystemOwnerController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.SYSTEMOWNER_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "System owner's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
}
