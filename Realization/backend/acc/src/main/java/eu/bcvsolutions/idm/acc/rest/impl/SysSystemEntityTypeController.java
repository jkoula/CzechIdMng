package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.PagedResources.PageMetadata;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SystemEntityTypeRegistrableDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-entity-types")
@Api(
		value = SysSystemEntityTypeController.TAG, 
		tags = SysSystemEntityTypeController.TAG, 
		description = "System entity types",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class SysSystemEntityTypeController implements BaseController {

	protected static final String TAG = "System entity types";
	
	@Autowired
	private SysSystemEntityTypeManager systemEntityTypeManager;
	
	@ResponseBody
	@GetMapping(value = "/search/supported")
	@ApiOperation(
			value = "Get supported system entity types", 
			nickname = "getSupportedSystemEntityTypes", 
			response = SystemEntityTypeRegistrableDto.class, 
			tags = { SysSystemEntityTypeController.TAG })
	public PagedResources<SystemEntityTypeRegistrableDto> getSupportedEntityTypes() {
		List<SystemEntityTypeRegistrableDto> systemEntityTypes = systemEntityTypeManager.getSupportedEntityTypes();
		return new PagedResources<>(systemEntityTypes,
				new PageMetadata(systemEntityTypes.size(), 0, systemEntityTypes.size(), 1));
	}

	@ResponseBody
	@GetMapping(value = "/{backendId}")
	@ApiOperation(
			value = "System entity type detail",
			nickname = "getSupportedSystemEntityType",
			response = SystemEntityTypeRegistrableDto.class,
			tags = { SysSystemEntityTypeController.TAG })
	public ResponseEntity<?> get(
			@ApiParam(value = "System entity type code.", required = true)
			@PathVariable @NotNull String backendId) {
		SystemEntityTypeRegistrableDto systemEntityType = systemEntityTypeManager.getSystemEntityDtoByCode(backendId);
		return new ResponseEntity<>(toResource(systemEntityType), HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping(value = "/{backendId}/{systemMappingId}")
	@ApiOperation(
			value = "System entity type detail by mapping",
			nickname = "getSupportedSystemEntityTypeByMapping",
			response = SystemEntityTypeRegistrableDto.class, 
			tags = { SysSystemEntityTypeController.TAG })
	public ResponseEntity<?> get(
			@ApiParam(value = "System entity type code.", required = true)
			@PathVariable @NotNull String backendId,
			@ApiParam(value = "System mapping id", required = true)
			@PathVariable @NotNull String systemMappingId) {
		SystemEntityTypeRegistrableDto systemEntityType = systemEntityTypeManager.getSystemEntityDtoByCode(backendId, systemMappingId);
		return new ResponseEntity<>(toResource(systemEntityType), HttpStatus.OK);
	}
	
	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	public ResourceSupport toResource(SystemEntityTypeRegistrableDto dto) {
		if (dto == null) { 
			return null;
		} 
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()).slash(dto.getId()).withSelfRel();
		Resource<SystemEntityTypeRegistrableDto> resourceSupport = new Resource<SystemEntityTypeRegistrableDto>(dto, selfLink);
		//
		return resourceSupport;
	}
}
