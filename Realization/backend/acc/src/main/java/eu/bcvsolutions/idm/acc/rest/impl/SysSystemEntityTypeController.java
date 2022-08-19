package eu.bcvsolutions.idm.acc.rest.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityTypeManager;
import eu.bcvsolutions.idm.acc.system.entity.SystemEntityTypeRegistrable;
import eu.bcvsolutions.idm.core.api.config.swagger.SwaggerConfig;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

@RestController
@Enabled(AccModuleDescriptor.MODULE_ID)
@RequestMapping(value = BaseDtoController.BASE_PATH + "/system-entity-type")
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
			value = "Remote server detail", 
			nickname = "getRemoteServer", 
			response = SysConnectorServerDto.class, 
			tags = { SysRemoteServerController.TAG }, 
			authorizations = {
					@Authorization(value = SwaggerConfig.AUTHENTICATION_BASIC, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")}),
					@Authorization(value = SwaggerConfig.AUTHENTICATION_CIDMST, scopes = { 
							@AuthorizationScope(scope = AccGroupPermission.REMOTESERVER_READ, description = "")})
					})
	public Resources<SystemEntityTypeRegistrable> getSupportedEntityTypes() {
		return new Resources<>(systemEntityTypeManager.getSupportedEntityTypes());
	}
}
