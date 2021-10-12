package eu.bcvsolutions.idm.core.rest.impl;

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.dto.DelegationTypeDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmUniversalSearchFilter;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.service.DelegationManager;
import eu.bcvsolutions.idm.core.api.service.IdmUniversalSearchService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import java.util.Set;
import java.util.stream.Collectors;
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
 * Controller for a universal search.
 * 
 * @since  11.3.0
 * @author Vít Švanda
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/universal-searches") 
@Api(
		value = IdmUniversalSearchController.TAG,  
		tags = { IdmUniversalSearchController.TAG }, 
		description = "Universal search",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class IdmUniversalSearchController extends AbstractReadDtoController<UniversalSearchDto, IdmUniversalSearchFilter> {

	protected static final String TAG = "Universal search";
	
	@Autowired
	private DelegationManager universalSearchManager;

	@Autowired
	public IdmUniversalSearchController(
			IdmUniversalSearchService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@ApiOperation(
			value = "Search universal searches (/search/quick alias)", 
			nickname = "searchUniversalSearches", 
			tags = { IdmUniversalSearchController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@ApiOperation(
			value = "Search universal searches", 
			nickname = "searchQuickDefinitions", 
			tags = { IdmUniversalSearchController.TAG }, 
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@ApiOperation(
			value = "Autocomplete universal searches (selectbox usage)", 
			nickname = "autocompleteUniversalSearches", 
			tags = { IdmUniversalSearchController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@ApiOperation(
			value = "Universal searche detail", 
			nickname = "getDefinition", 
			response = UniversalSearchDto.class, 
			tags = { IdmUniversalSearchController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Universal searche's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.DELEGATIONDEFINITION_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countUniversalSearches", 
			tags = { IdmUniversalSearchController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}
	
	/**
	 * Returns all registered universal search types.
	 *
	 * @return universal search types
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/search/supported")
	@ApiOperation(
			value = "Get all supported universal search types",
			nickname = "getSupportedDelegationTypes",
			tags = {IdmUniversalSearchController.TAG},
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
			})
	public Resources<DelegationTypeDto> getSupportedTypes() {
		return new Resources<>(universalSearchManager.getSupportedTypes()
				.stream()
				.map(universalSearchType -> universalSearchManager.convertDelegationTypeToDto(universalSearchType))
				.collect(Collectors.toList())
		);
	}
	

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnDefinition", 
			tags = { IdmUniversalSearchController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST)
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Universal searche's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}

	@Override
	protected IdmUniversalSearchFilter toFilter(MultiValueMap<String, Object> parameters) {
		IdmUniversalSearchFilter filter = new IdmUniversalSearchFilter(parameters, getParameterConverter());
		return filter;
	}

}
