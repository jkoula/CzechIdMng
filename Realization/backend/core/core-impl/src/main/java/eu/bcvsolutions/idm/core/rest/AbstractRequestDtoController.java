package eu.bcvsolutions.idm.core.rest;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.exception.CoreException;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.RequestManager;
import eu.bcvsolutions.idm.core.rest.impl.IdmRequestController;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;

/**
 * CRUD operations for requests
 * 
 * @author svandav
 */
public abstract class AbstractRequestDtoController<DTO extends Requestable, F extends BaseFilter>
		extends AbstractReadWriteDtoController<DTO, F> {

	@Autowired
	private RequestManager requestManager;

	public AbstractRequestDtoController(ReadWriteDtoService<DTO, F> entityService) {
		super(entityService);
	}

	/**
	 * Post DTO and convert to response
	 * 
	 * @param dto
	 * @param requestId
	 * @return
	 */
	@ApiOperation(value = "Create / update record", authorizations = {
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	})
	public ResponseEntity<?> post(@ApiParam(value = "Request ID", required = true) String requestId, //
			@ApiParam(value = "Record (dto).", required = true) DTO dto) { //
		Requestable resultDto = requestManager.post(requestId, dto);
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}

	/**
	 * Update DTO by given backendId and convert to response
	 * 
	 * @param requestId
	 * @param backendId
	 * @param dto
	 * @return
	 */
	@ApiOperation(value = "Update record", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) })
	public ResponseEntity<?> put( //
			@ApiParam(value = "Request ID", required = true) String requestId, //
			@ApiParam(value = "Record's uuid identifier or unique code", required = true) String backendId, //
			@ApiParam(value = "Record (dto).", required = true) DTO dto) { //
		DTO updateDto = getDto(backendId);
		if (updateDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}

		Requestable resultDto = requestManager.post(requestId, dto);
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Deletes DTO by given id
	 * 
	 * @param requestId
	 * @param backendId
	 * @return
	 */
	@ApiOperation(value = "Delete record", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) })
	public ResponseEntity<?> delete(@ApiParam(value = "Request ID", required = true) String requestId, //
			@ApiParam(value = "Record's uuid identifier or unique code.", required = true) String backendId) { //
		DTO dto = getDto(backendId);
		if (dto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		Requestable resultDto = requestManager.delete(requestId, dto);
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	/**
	 * Returns response DTO by given backendId
	 * 
	 * @param backendId
	 * @param requestId
	 * @return
	 */
	@ApiOperation(value = "Read record", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	})
	public ResponseEntity<?> get(@PathVariable @NotNull String requestId,
			@ApiParam(value = "Record's uuid identifier or unique code, if record supports Codeable interface.", required = true) //
			@PathVariable @NotNull String backendId) { //

		DTO dto = getDto(backendId);
		if (dto == null) {
			try {
				dto = getService().getDtoClass().newInstance();
				dto.setId(UUID.fromString(backendId));
			} catch (InstantiationException | IllegalAccessException e) {
				throw new CoreException(e);
			}
		}
		Requestable resultDto = requestManager.get(requestId, dto);
		if (resultDto == null) {
			throw new EntityNotFoundException(getService().getEntityClass(), backendId);
		}
		@SuppressWarnings("unchecked")
		ResourceSupport resource = toResource(requestId, (DTO) resultDto);
		if (resource == null) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
		}
		//
		return new ResponseEntity<>(resource, HttpStatus.OK);
	}

	@ApiOperation(value = "Create request for DTO", authorizations = {
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	})
	public ResponseEntity<?> createRequest(@ApiParam(value = "Record (dto).", required = true) DTO dto) {
		IdmRequestDto request = requestManager.createRequest(dto);
		Link selfLink = ControllerLinkBuilder.linkTo(IdmRequestController.class).slash(request.getId()).withSelfRel();
		Resource<IdmRequestDto> resource = new Resource<IdmRequestDto>(request, selfLink);
		return new ResponseEntity<>(resource, HttpStatus.CREATED);
	}

	/**
	 * Quick search - parameters will be transformed to filter object
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
	 * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "Search records (/search/quick alias)", authorizations = { //
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	}) //
	@ApiImplicitParams({ //
			@ApiImplicitParam(name = "page", dataType = "string", paramType = "query", //
					value = "Results page you want to retrieve (0..N)"), //
			@ApiImplicitParam(name = "size", dataType = "string", paramType = "query", //
					value = "Number of records per page."), //
			@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", //
					value = "Sorting criteria in the format: property(,asc|desc). " + //
							"Default sort order is ascending. " + //
							"Multiple sort criteria are supported.") //
	})
	public Resources<?> find( //
			@ApiParam(value = "Request ID", required = true) String requestId, //
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, //
			@PageableDefault Pageable pageable) { //
		@SuppressWarnings("unchecked")
		Page<DTO> page = (Page<DTO>) requestManager.find(getDtoClass(), requestId, toFilter(parameters), pageable,
				IdmBasePermission.READ);

		return toResources(page, getDtoClass());
	}

	/**
	 * All endpoints will support find quick method.
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
	 * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "Search records", authorizations = { @Authorization(SwaggerConfig.AUTHENTICATION_BASIC),
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) })
	@ApiImplicitParams({
			@ApiImplicitParam(name = "page", dataType = "string", paramType = "query", value = "Results page you want to retrieve (0..N)"),
			@ApiImplicitParam(name = "size", dataType = "string", paramType = "query", value = "Number of records per page."),
			@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", value = "Sorting criteria in the format: property(,asc|desc). "
					+ "Default sort order is ascending. " + "Multiple sort criteria are supported.") })
	public Resources<?> findQuick(@ApiParam(value = "Request ID", required = true) String requestId,
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return find(requestId, parameters, pageable);
	}

	/**
	 * Quick search for autocomplete (read data to select box etc.) - parameters
	 * will be transformed to filter object
	 * 
	 * @param parameters
	 * @param pageable
	 * @return
	 * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "Autocomplete records (selectbox usage)", authorizations = { //
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	}) //
	@ApiImplicitParams({ //
			@ApiImplicitParam(name = "page", dataType = "string", paramType = "query", //
					value = "Results page you want to retrieve (0..N)"), //
			@ApiImplicitParam(name = "size", dataType = "string", paramType = "query", //
					value = "Number of records per page."), //
			@ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query", //
					value = "Sorting criteria in the format: property(,asc|desc). " + //
							"Default sort order is ascending. " + //
							"Multiple sort criteria are supported.") }) //
	public Resources<?> autocomplete( //
			@ApiParam(value = "Request ID", required = true) String requestId, //
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, //
			@PageableDefault Pageable pageable) { //
		@SuppressWarnings("unchecked")
		Page<DTO> page = (Page<DTO>) requestManager.find(getDtoClass(), requestId, toFilter(parameters), pageable,
				IdmBasePermission.AUTOCOMPLETE);
		return toResources(page, getDtoClass());
	}

	// TODO: Support of count !
	/**
	 * The number of entities that match the filter - parameters will be transformed
	 * to filter object
	 * 
	 * @param parameters
	 * @return
	 * @see #toFilter(MultiValueMap)
	 */
	@ApiOperation(value = "The number of entities that match the filter", authorizations = {
			@Authorization(SwaggerConfig.AUTHENTICATION_BASIC), //
			@Authorization(SwaggerConfig.AUTHENTICATION_CIDMST) //
	}) //
	public long count(@ApiParam(value = "Request ID", required = true) String requestId, //
			@RequestParam(required = false) //
			MultiValueMap<String, Object> parameters) { //
		return count(toFilter(parameters), IdmBasePermission.COUNT);
	}

	/**
	 * Converts DTO to ResourceSupport
	 * 
	 * @param dto
	 * @return
	 */
	protected ResourceSupport toResource(String requestId, DTO dto) {
		if (dto == null) {
			return null;
		}
		Link selfLink = ControllerLinkBuilder.linkTo(this.getClass()) //
				.slash(requestId) //
				.slash(this.getRequestSubPath()) //
				.slash(dto.getId()).withSelfRel(); //
		Resource<DTO> resourceSupport = new Resource<DTO>(dto, selfLink);
		return resourceSupport;
	}

	public abstract String getRequestSubPath();
}
