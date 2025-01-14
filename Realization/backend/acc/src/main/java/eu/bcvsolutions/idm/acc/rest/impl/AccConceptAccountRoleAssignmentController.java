package eu.bcvsolutions.idm.acc.rest.impl;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.rest.impl.IdmFormDefinitionController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest;
import eu.bcvsolutions.idm.core.rest.impl.IdmIdentityRoleController;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/" + AccModuleDescriptor.MODULE_ID + "/concept-role-requests")
@Api(value = AccConceptAccountRoleAssignmentController.TAG, description = "Operations with account role assignment concepts", tags = {
		AccConceptAccountRoleAssignmentController.TAG }, produces = BaseController.APPLICATION_HAL_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccConceptAccountRoleAssignmentController
		extends AbstractReadWriteDtoController<AccAccountConceptRoleRequestDto, AccAccountConceptRoleRequestFilter> {

	protected static final String TAG = "Role Request - account role concepts";
	private final SecurityService securityService;
	private final IdmRoleRequestService roleRequestService;
	private final AccAccountConceptRoleRequestService service;
	@Autowired
	private IdmFormDefinitionController formDefinitionController;

	@Autowired
	public AccConceptAccountRoleAssignmentController(AccAccountConceptRoleRequestService service, SecurityService securityService,
			IdmRoleRequestService roleRequestService) {
		super(service);
		//
		Assert.notNull(securityService, "Service is required.");
		Assert.notNull(roleRequestService, "Service is required.");
		Assert.notNull(service, "Service is required.");
		//
		this.securityService = securityService;
		this.roleRequestService = roleRequestService;
		this.service = service;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search concept role requests (/search/quick alias)", nickname = "searchConceptRoleRequests", tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }) })
	public Resources<?> find(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Search concept role requests", nickname = "searchQuickConceptRoleRequests", tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_ADMIN, description = "") }) })
	public Resources<?> findQuick(@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}

	@Override
	public Page<AccAccountConceptRoleRequestDto> find(AccAccountConceptRoleRequestFilter filter, Pageable pageable,
			BasePermission permission) {
		// check access
		if (!securityService.hasAnyAuthority(CoreGroupPermission.ROLE_REQUEST_ADMIN)) {
			if (filter == null || filter.getRoleRequestId() == null) {
				throw new ForbiddenEntityException((Serializable)null, CoreGroupPermission.ROLEREQUEST, IdmBasePermission.ADMIN);
			}
			IdmRoleRequestDto roleRequest = roleRequestService.get(filter.getRoleRequestId(), permission);
			if (roleRequest == null) {
				// return empty result (find method doesn't throw 404)
				return new PageImpl<>(new ArrayList<>());
			}
		}
		Page<AccAccountConceptRoleRequestDto> results = super.find(filter, pageable, permission);
		return results;
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Concept detail", nickname = "getConceptRoleRequest", response = IdmConceptRoleRequestDto.class, tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public ResponseEntity<?> get(
			@ApiParam(value = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	public AccAccountConceptRoleRequestDto getDto(Serializable backendId) {
		AccAccountConceptRoleRequestDto concept = super.getDto(backendId);
		if (concept == null) {
			return null;
		}
		IdmFormInstanceDto formInstanceDto = service.getRoleAttributeValues(concept, true);
		if (formInstanceDto != null) {
			concept.getEavs().clear();
			concept.getEavs().add(formInstanceDto);
		}
		return concept;
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_CREATE + "')" + " or hasAuthority('"
			+ CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Create / update concept", nickname = "postConceptRoleRequest", response = AccAccountConceptRoleRequestDto.class, tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_CREATE, description = ""),
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> post(@RequestBody @NotNull AccAccountConceptRoleRequestDto dto) {
		// Check if exist same concept for same role and operation type. If yes, we delete it before save new.
		if (dto != null //
				&& dto.getRoleRequest() != null //
				&& dto.getAccountRole() != null //
				&& dto.getOperation() != null //
				&& ConceptRoleRequestOperation.ADD != dto.getOperation() //
				&& dto.getAccount() != null) {
			AccAccountConceptRoleRequestFilter filter = new AccAccountConceptRoleRequestFilter();
			filter.setAccountRole(dto.getAccountRole());
			filter.setRoleRequestId(dto.getRoleRequest());
			filter.setOperation(dto.getOperation());
			filter.setAccountuuid(dto.getAccount());
			List<AccAccountConceptRoleRequestDto> duplicates = getService().find(filter, PageRequest.of(0, 1)).getContent();
			duplicates.forEach(duplicate -> {
				// Delete duplicated concept first
				getService().delete(duplicate);
			});
		}

		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@ApiOperation(value = "Update concept", nickname = "putConceptRoleRequest", response = AccAccountConceptRoleRequestDto.class, tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public ResponseEntity<?> put(
			@ApiParam(value = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId,
			@RequestBody @NotNull AccAccountConceptRoleRequestDto dto) {
		return super.put(backendId, dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_DELETE + "')")
	@ApiOperation(value = "Delete concept", nickname = "delete ConceptRoleRequest", tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_DELETE, description = "") }) })
	public ResponseEntity<?> delete(
			@ApiParam(value = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "What logged identity can do with given record", nickname = "getPermissionsOnConceptRoleRequest", tags = {
			AccConceptAccountRoleAssignmentController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public Set<String> getPermissions(
			@ApiParam(value = "Concept's uuid identifier.", required = true) @PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	/**
	 * Returns form definition to given entity.
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-definitions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Concept extended attributes form definitions", nickname = "getIdentityRoleFormDefinitions", tags = {
			IdmIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public ResponseEntity<?> getFormDefinitions(
			@ApiParam(value = "Role's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId) {
		AccAccountConceptRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		// Search definition by definition in role
		IdmRoleDto roleDto = DtoUtils.getEmbedded(dto, AbstractConceptRoleRequest_.role, IdmRoleDto.class);
		if (roleDto != null && roleDto.getIdentityRoleAttributeDefinition() != null) {
			return formDefinitionController.getDefinitions(roleDto.getIdentityRoleAttributeDefinition());
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	/**
	 * Returns entity's filled form values
	 * 
	 * @param backendId
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/{backendId}/form-values", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_READ + "')")
	@ApiOperation(value = "Concept form definition - read values", nickname = "getRoleFormValues", tags = {
			IdmIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_READ, description = "") }) })
	public Resource<?> getFormValues(
			@ApiParam(value = "Concept's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode) {
		AccAccountConceptRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(AccAccountConceptRoleRequestDto.class,
				definitionCode);
		//
		return formDefinitionController.getFormValues(dto, formDefinition);
	}

	/**
	 * Saves entity's form values
	 * 
	 * @param backendId
	 * @param formValues
	 * @return
	 */
	@ResponseBody
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.ROLE_REQUEST_UPDATE + "')")
	@RequestMapping(value = "/{backendId}/form-values", method = { RequestMethod.POST, RequestMethod.PATCH })
	@ApiOperation(value = "Concept form definition - save values", nickname = "postIdentityRoleFormValues", tags = {
			IdmIdentityRoleController.TAG }, authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = {
							@AuthorizationScope(scope = CoreGroupPermission.ROLE_REQUEST_UPDATE, description = "") }) })
	public Resource<?> saveFormValues(
			@ApiParam(value = "Concept's uuid identifier or code.", required = true) @PathVariable @NotNull String backendId,
			@ApiParam(value = "Code of form definition (default will be used if no code is given).", required = false, defaultValue = FormService.DEFAULT_DEFINITION_CODE) @RequestParam(name = "definitionCode", required = false) String definitionCode,
			@RequestBody @Valid List<IdmFormValueDto> formValues) {
		AccAccountConceptRoleRequestDto dto = getDto(backendId);
		if (dto == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", backendId));
		}
		checkAccess(dto, IdmBasePermission.UPDATE);
		//
		IdmFormDefinitionDto formDefinition = formDefinitionController.getDefinition(IdmConceptRoleRequest.class,
				definitionCode);
		//
		return formDefinitionController.saveFormValues(dto, formDefinition, formValues);
	}

	@Override
	protected AccAccountConceptRoleRequestFilter toFilter(MultiValueMap<String, Object> parameters) {
		AccAccountConceptRoleRequestFilter filter = new AccAccountConceptRoleRequestFilter(parameters);
		
		filter.setRoleRequestId(getParameterConverter().toUuid(parameters, "roleRequestId"));
		filter.setState(getParameterConverter().toEnum(parameters, "state", RoleRequestState.class));
		return filter;
	}
}
