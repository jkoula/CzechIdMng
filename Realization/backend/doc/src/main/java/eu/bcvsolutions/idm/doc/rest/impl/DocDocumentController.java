package eu.bcvsolutions.idm.doc.rest.impl;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.Resources;
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

import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.doc.DocModuleDescriptor;
import eu.bcvsolutions.idm.doc.domain.DocGroupPermission;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * RESTful document endpoint
 * 
 * @author Jirka Koula
 *
 */
@RestController
@Enabled(DocModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseController.BASE_PATH + "/doc")
@Api(
		value = DocDocumentController.TAG,
		description = "Documents",
		tags = { DocDocumentController.TAG })
public class DocDocumentController extends AbstractReadWriteDtoController<DocDocumentDto, DocDocumentFilter> {

	protected static final String TAG = "Documents";

	@Autowired
	public DocDocumentController(DocDocumentService service) {
		super(service);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_READ + "')")
	@ApiOperation(
			value = "Search documents (/search/quick alias)",
			nickname = "searchDocuments",
			tags = { DocDocumentController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = "") })
				})
	public Resources<?> find(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.find(parameters, pageable);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/search/quick", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_READ + "')")
	@ApiOperation(
			value = "Search documents",
			nickname = "searchQuickDocuments",
			tags = { DocDocumentController.TAG },
			authorizations = {
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = "") })
				})
	public Resources<?> findQuick(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters,
			@PageableDefault Pageable pageable) {
		return super.findQuick(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/autocomplete", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "Autocomplete documents (selectbox usage)",
			nickname = "autocompleteDocuments",
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_AUTOCOMPLETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_AUTOCOMPLETE, description = "") })
				})
	public Resources<?> autocomplete(
			@RequestParam(required = false) MultiValueMap<String, Object> parameters, 
			@PageableDefault Pageable pageable) {
		return super.autocomplete(parameters, pageable);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/search/count", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_COUNT + "')")
	@ApiOperation(
			value = "The number of entities that match the filter", 
			nickname = "countDocuments",
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_COUNT, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_COUNT, description = "") })
				})
	public long count(@RequestParam(required = false) MultiValueMap<String, Object> parameters) {
		return super.count(parameters);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_READ + "')")
	@ApiOperation(
			value = "Document detail",
			nickname = "getDocument",
			response = DocDocumentDto.class,
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = "") })
				})
	public ResponseEntity<?> get(
			@ApiParam(value = "Documents's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.get(backendId);
	}

	@Override
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_CREATE + "') or hasAuthority('" + DocGroupPermission.DOCUMENT_UPDATE + "')")
	@ApiOperation(
			value = "Create / update document",
			nickname = "postDocument",
			response = DocDocumentDto.class,
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_CREATE, description = ""),
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_UPDATE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_CREATE, description = ""),
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_UPDATE, description = "")})
				})
	public ResponseEntity<?> post(@Valid @RequestBody DocDocumentDto dto) {
		return super.post(dto);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PUT)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_UPDATE + "')")
	@ApiOperation(
			value = "Update document",
			nickname = "putDocument",
			response = DocDocumentDto.class,
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_UPDATE, description = "") })
				})
	public ResponseEntity<?> put(
			@ApiParam(value = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId, 
			@Valid @RequestBody DocDocumentDto dto) {
		return super.put(backendId, dto);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.PATCH)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_UPDATE + "')")
	@ApiOperation(
			value = "Update document",
			nickname = "patchDocument",
			response = DocDocumentDto.class,
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_UPDATE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_UPDATE, description = "") })
				})
	public ResponseEntity<?> patch(
			@ApiParam(value = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId,
			HttpServletRequest nativeRequest)
			throws HttpMessageNotReadableException {
		return super.patch(backendId, nativeRequest);
	}

	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}", method = RequestMethod.DELETE)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_DELETE + "')")
	@ApiOperation(
			value = "Delete document",
			nickname = "deleteDocument",
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_DELETE, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_DELETE, description = "") })
				})
	public ResponseEntity<?> delete(
			@ApiParam(value = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.delete(backendId);
	}
	
	@Override
	@ResponseBody
	@RequestMapping(value = "/{backendId}/permissions", method = RequestMethod.GET)
	@PreAuthorize("hasAuthority('" + DocGroupPermission.DOCUMENT_READ + "')"
			+ " or hasAuthority('" + DocGroupPermission.DOCUMENT_AUTOCOMPLETE + "')")
	@ApiOperation(
			value = "What logged identity can do with given record", 
			nickname = "getPermissionsOnDocument",
			tags = { DocDocumentController.TAG },
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = ""),
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_AUTOCOMPLETE, description = "")}),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_READ, description = ""),
						@AuthorizationScope(scope = DocGroupPermission.DOCUMENT_AUTOCOMPLETE, description = "")})
				})
	public Set<String> getPermissions(
			@ApiParam(value = "Document's uuid identifier.", required = true)
			@PathVariable @NotNull String backendId) {
		return super.getPermissions(backendId);
	}
	
	@Override
	protected DocDocumentFilter toFilter(MultiValueMap<String, Object> parameters) {
		return new DocDocumentFilter(parameters);
	}
}
