package eu.bcvsolutions.idm.acc.rest.impl;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/script-usage")
@Api(
		value = AccScriptUsageController.TAG,  
		tags = { AccScriptUsageController.TAG }, 
		description = "Groovy scripts administration",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class AccScriptUsageController implements BaseController  {

	protected static final String TAG = "Scripts";
	
	@Autowired
	private SysSystemAttributeMappingService service;
	//
	
	@ResponseBody
	@GetMapping(value = "/{backendId}")
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.SCRIPT_READ + "')")
	@ApiOperation(
			value = "Script usage in mapping", 
			nickname = "getScriptUsage", 
			tags = { AccScriptUsageController.TAG }, 
			authorizations = { 
				@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") }),
				@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
						@AuthorizationScope(scope = CoreGroupPermission.SCRIPT_READ, description = "") })
				})
	public List<SysSystemAttributeMappingDto> get(
			@ApiParam(value = "Script's uuid identifier or code.", required = true)
			@PathVariable @NotNull String backendId) {
		return service.getScriptUsage(backendId);
	}
}
