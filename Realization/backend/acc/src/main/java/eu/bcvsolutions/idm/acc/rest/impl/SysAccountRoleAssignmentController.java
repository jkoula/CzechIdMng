package eu.bcvsolutions.idm.acc.rest.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.security.api.domain.RoleBasePermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
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

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Account role assignment controller
 * - read only for now: @Enabled by some public configuration property
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/" + AccModuleDescriptor.MODULE_ID + "/account-role-assignments")
@Api(
		value = SysAccountRoleAssignmentController.TAG,
		description = "Operations with account role assignments",
		tags = { SysAccountRoleAssignmentController.TAG },
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysAccountRoleAssignmentController extends AbstractReadWriteDtoController<AccAccountRoleAssignmentDto, AccAccountRoleAssignmentFilter> {

	protected static final String TAG = "Account role assignments";
	//
	@Autowired private IdmFormDefinitionController formDefinitionController;
	@Autowired private IdmRoleService roleService;
	@Autowired private FormService formService;

	@Autowired
	public SysAccountRoleAssignmentController(AccAccountRoleAssignmentService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@ApiOperation(
			value = "Search account role assignments (/search/quick alias)",
			nickname = "searchAccountRoleAssignments",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@ApiOperation(
			value = "Search account role assignment",
			nickname = "searchQuickAccountRoleAssignments",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete account role assignments (selectbox usage)",
			nickname = "autocompleteAccountRoleAssignments",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@ResponseBody
	@RequestMapping(value= "/search/can-be-requested", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_CANBEREQUESTED + "')")
	@ApiOperation(
			value = "Find assigned roles, which can be requested", 
			nickname = "findCanBeRequestedAccountRoleAssignments",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_CANBEREQUESTED, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_CANBEREQUESTED, description = "") })
				})
	public Resources<?> findCanBeRequested(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return toResources(find(toFilter(parameters), pageable, RoleBasePermission.CANBEREQUESTED), getDtoClass());
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countAccountRoleAssignments",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@ApiOperation(
			value = "Account role assignment detail",
			nickname = "getAccountRoleAssignment",
			response = AccAccountRoleAssignmentDto.class,
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Account role assignment's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	//@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnAccountRoleAssignment",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") })
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Account role assignment's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	
	/**
	 * Returns form definition to given role.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@ApiOperation(
			value = "Account role assignment extended attributes form definitions",
			nickname = "getAccountRoleAssignmentFormDefinitions",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") })
				})
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Role's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		
		AccAccountRoleAssignmentDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		// Search definition by definition in role
		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, AbstractRoleAssignment_.role, IdmRoleDto.class);
		if (roleDto != null && roleDto.getIdentityRoleAttributeDefinition() != null) {
			IdmFormDefinitionDto definition = roleService.getFormAttributeSubdefinition(roleDto);
			return formDefinitionController.toResources(Lists.newArrayList(definition));
		}
		
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	/**
	 * Returns entity's filled form values
	 * 
	 * In this case is code of definition ignored, we will load only definition by given role and sub-definition.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ + "')")
	@ApiOperation(
			value = "Account role assignment form definition - read values",
			nickname = "getAccountRoleFormValues",
			tags = { SysAccountRoleAssignmentController.TAG },
			authorizations = { 
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = AccGroupPermission.ACCOUNTROLEASSIGNMENT_READ, description = "") })
				})
	public Resource<?> getFormValues(
			@ApiParam(value = "Account role assignment's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId, 
			@ApiParam(value = "Code of form definition (default will be used if no code is given)."
					+ " In this case is code of definition ignored, we will load only definition by given role and sub-definition.",
					required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE)
			@RequestParam(name = "definitionCode", required = false) String definitionCode) {
		AccAccountRoleAssignmentDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, AbstractRoleAssignment_.role, IdmRoleDto.class);
		IdmFormDefinitionDto definition = roleService.getFormAttributeSubdefinition(roleDto);
		//
		return new Resource<>(formService.getFormInstance(dto, definition));
	}
	
	@Override
	protected AccAccountRoleAssignmentFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccAccountRoleAssignmentFilter filter = new  AccAccountRoleAssignmentFilter(parameters);
		// TODO: resolve codeable parameters automatically ...
		filter.setIdentityId(getParameterConverter().toEntityUuid(parameters, BaseRoleAssignmentFilter.PARAMETER_IDENTITY_ID, IdmIdentityDto.class));
		filter.setRoleId(getParameterConverter().toEntityUuid(parameters, BaseRoleAssignmentFilter.PARAMETER_ROLE_ID, IdmRoleDto.class));
		//
		return filter;
	}
}
