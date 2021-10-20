package eu.bcvsolutions.idm.core.security.rest.impl;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.domain.IdentityBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.dto.LoginRequestDto;
import eu.bcvsolutions.idm.core.security.api.dto.TwoFactorRequestDto;
import eu.bcvsolutions.idm.core.security.api.exception.CasTicketValidationException;
import eu.bcvsolutions.idm.core.security.api.exception.IdentityDisabledException;
import eu.bcvsolutions.idm.core.security.api.exception.IdentityNotFoundException;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.service.LoginService;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.api.service.TwoFactorAuthenticationManager;
import eu.bcvsolutions.idm.core.security.auth.filter.AuthenticationExceptionContext;
import eu.bcvsolutions.idm.core.security.service.impl.JwtAuthenticationMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Identity authentication.
 * 
 * @author Radek Tomiška 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@RestController
@RequestMapping(value = BaseController.BASE_PATH + LoginController.AUTH_PATH)
@Api(value = LoginController.TAG, description = "Authentication endpoint", tags = { LoginController.TAG })
public class LoginController implements BaseController {
	
	protected static final String TAG = "Authentication";
	public static final String AUTH_PATH = "/authentication";
	public static final String REMOTE_AUTH_PATH = "/remote-auth";
	public static final String CAS_LOGIN_REQUEST_PATH = "/cas-login-request";
	public static final String CAS_LOGIN_RESPONSE_PATH = "/cas-login-response";
	public static final String CAS_LOGOUT_REQUEST_PATH = "/cas-logout-request";
	public static final String CAS_LOGOUT_RESPONSE_PATH = "/cas-logout-response";
	//
	@Autowired private AuthenticationManager authenticationManager;
	@Autowired private TwoFactorAuthenticationManager twoFactorAuthenticationManager;
	@Autowired private LoginService loginService;
	@Autowired private LookupService lookupService;
	@Autowired private TokenManager tokenManager;
	@Autowired private JwtAuthenticationMapper jwtTokenMapper;
	@Autowired private AuthenticationExceptionContext ctx;
	@Autowired private CasConfiguration casConfiguration;
	@Autowired private ConfigurationService configurationService;
	@Autowired private SecurityService securityService;
	@Autowired @Lazy private RestTemplate restTemplate;
	
	@ResponseBody
	@ApiOperation(
			value = "Login an get the CIDMST token", 
			notes= "Login an get the CIDMST token. Use returned token attribute value as \"CIDMST\" http header in next requests.",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@RequestMapping(method = RequestMethod.POST)
	public Resource<LoginDto> login(
			@ApiParam(value = "Identity credentials.", required = true)
			@Valid @RequestBody(required = true) LoginRequestDto loginDto) {
		if (loginDto == null || loginDto.getUsername() == null || loginDto.getPassword() == null){
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Username and password must be filled");
		}
		LoginDto authenticate = authenticationManager.authenticate(new LoginDto(loginDto));
		if (!casConfiguration.isDisabled() && !securityService.isAdmin()) {
			authenticationManager.logout();
			//
			throw new ResultCodeException(CoreResultCode.CAS_IDM_LOGIN_ADMIN_ONLY);
		}
		//
		return new Resource<LoginDto>(authenticate);
	}
	
	/**
	 * Two factor login.
	 * 
	 * @param twoFactorDto
	 * @return
	 * @since 10.7.0
	 */
	@ResponseBody
	@ApiOperation(
			value = "Login - additional two factor authentication", 
			notes= "Additional two factor authentication with TOTP verification code.",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@RequestMapping(path = "/two-factor", method = RequestMethod.POST)
	public Resource<LoginDto> twoFactor(
			@ApiParam(value = "Token and verification code.", required = true)
			@Valid @RequestBody(required = true) TwoFactorRequestDto twoFactorDto) {
		if (twoFactorDto == null 
				|| twoFactorDto.getVerificationCode() == null
				|| twoFactorDto.getToken() == null) {
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Verification code must be filled");
		}
		//
		LoginDto loginDto = new LoginDto();
		loginDto.setPassword(twoFactorDto.getVerificationCode());
		loginDto.setToken(twoFactorDto.getToken().asString());
		//
		return new Resource<LoginDto>(twoFactorAuthenticationManager.authenticate(loginDto));
	}
	
	@ApiOperation(
			value = "Login with remote token", 
			notes= "Login with remote token an get the CIDMST token. Remote token can be obtained by external authentication system (e.g. OpenAM, OAuth).",
			response = LoginDto.class,
			tags = { LoginController.TAG })
	@RequestMapping(path = REMOTE_AUTH_PATH, method = RequestMethod.GET)
	public Resource<LoginDto> loginWithRemoteToken() {
		return new Resource<LoginDto>(loginService.loginAuthenticatedUser());
	}
	
	/**
	 * Switch user.
	 * 
	 * 
	 * @param username target user
	 * @return new login dto
	 * @since 10.5.0
	 */
	@ResponseBody
	@ApiOperation(
			value = "Login as other user", 
			notes= "Login as other user (switch user).",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@PreAuthorize("hasAuthority('" + CoreGroupPermission.IDENTITY_SWITCHUSER + "')")
	@RequestMapping(path = "/switch-user", method = RequestMethod.PUT)
	public Resource<LoginDto> switchUser(
			@ApiParam(value = "Switch to user by given username.", required = true)
			@RequestParam @NotNull String username) {
		// change logged token authorities
		IdmIdentityDto identity = lookupService.lookupDto(IdmIdentityDto.class, username);
		if (identity == null) {
			throw new EntityNotFoundException(IdmIdentity.class, username);
		}
		return new Resource<LoginDto>(loginService.switchUser(identity, IdentityBasePermission.SWITCHUSER));
	}
	
	/**
	 * Switch user - logout. Available for all logged identities (without authority check).
	 * 
	 * @param username target user
	 * @return new login dto
	 * @since 10.5.0
	 */
	@ResponseBody
	@ApiOperation(
			value = "Logout after login as other user", 
			notes= "Logout after login as other user (switch user logout).",
			response = LoginDto.class,
			tags = { LoginController.TAG } )
	@RequestMapping(path = "/switch-user", method = RequestMethod.DELETE)
	public Resource<LoginDto> switchUserLogout() {
		return new Resource<LoginDto>(loginService.switchUserLogout());
	}
	
	/**
	 * Redirect to CAS server.
	 * 
	 * @param nativeRequest
	 * @return redirect
	 * @since 12.0.0
	 */
	@RequestMapping(path = CAS_LOGIN_REQUEST_PATH, method = RequestMethod.GET)
	public ResponseEntity<Void> casLoginRequest(HttpServletRequest nativeRequest) {
		//
		// check correct setting - redirect to FE error page otherwise
		String casUrl = casConfiguration.getUrl();
		StringBuilder frontendUrl = new StringBuilder(configurationService.getFrontendUrl(CAS_LOGIN_RESPONSE_PATH));
		if (StringUtils.isBlank(casUrl)) {
			frontendUrl.append("?status-code=");
			frontendUrl.append(CoreResultCode.CAS_LOGIN_SERVER_URL_NOT_CONFIGURED.getCode().toLowerCase());
			//
			return ResponseEntity
					.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, frontendUrl.toString())
					.build();
		}
		//
		String casLoginUrl = String.format("%s%s?service=%s", 
				casUrl, 
				casConfiguration.getLoginPath(),
				casConfiguration.getService(nativeRequest, true));
		// check CAS server is available
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(casLoginUrl, String.class);
			if (HttpStatus.OK != response.getStatusCode()) {
				frontendUrl.append("?status-code=");
				frontendUrl.append(CoreResultCode.CAS_LOGIN_SERVER_NOT_AVAILABLE.getCode().toLowerCase());
				return ResponseEntity
						.status(HttpStatus.FOUND)
						.header(HttpHeaders.LOCATION, frontendUrl.toString())
						.build();
			}
		} catch (Exception ex) {
			frontendUrl.append("?status-code=");
			frontendUrl.append(CoreResultCode.CAS_LOGIN_SERVER_NOT_AVAILABLE.getCode().toLowerCase());
			return ResponseEntity
					.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, frontendUrl.toString())
					.build();
		}
		//
		// redirect to CAS
		return ResponseEntity
				.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, casLoginUrl)
				.build();
	}
	
	/**
	 * Redirect to FE, after CAS authentication.
	 * 
	 * @return redirect to FE
	 * @since 12.0.0
	 */
	@RequestMapping(path = CAS_LOGIN_RESPONSE_PATH, method = RequestMethod.GET)
	public ResponseEntity<Void> casLoginResponse() {
		// process ticket + add token into url parameter
		IdmTokenDto currentToken = tokenManager.getCurrentToken();
		StringBuilder url = new StringBuilder(configurationService.getFrontendUrl(CAS_LOGIN_RESPONSE_PATH));
		// set token into url - ok
		if (currentToken != null) {
			IdmJwtAuthentication authentication = jwtTokenMapper.fromDto(currentToken);
			url.append('?');
			url.append(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME.toLowerCase());
			url.append('=');
			url.append(jwtTokenMapper.writeToken(authentication));
		} else if (ctx != null) {
			// not - ok => resolve exception
			ResultCodeException resultCodeException = ctx.getCodeEx();
			if (resultCodeException == null) {
				// resolve concrete exception
				url.append("?status-code=");
				if (ctx.getAuthEx() instanceof IdentityNotFoundException) {
					url.append(CoreResultCode.AUTH_FAILED.getCode().toLowerCase()); // same as from standard login
				} else if (ctx.getAuthEx() instanceof IdentityDisabledException) {
					url.append(CoreResultCode.AUTH_FAILED.getCode().toLowerCase()); // same as from standard login
				} else if (ctx.getAuthEx() instanceof CasTicketValidationException) {
					url.append(CoreResultCode.CAS_TICKET_VALIDATION_FAILED.getCode().toLowerCase());
				} else {
					url.append(CoreResultCode.LOG_IN_FAILED.getCode().toLowerCase()); // common error - login failed
				}
			} else if (resultCodeException instanceof TwoFactorAuthenticationRequiredException) {
				// handle two factor login
				url.append('?');
				url.append(JwtAuthenticationMapper.AUTHENTICATION_TOKEN_NAME.toLowerCase());
				url.append('=');
				url.append(((TwoFactorAuthenticationRequiredException) resultCodeException).getToken());
			} else {
				// concrete status code form result code exception
				url.append("?status-code=");
				url.append(resultCodeException.getError().getError().getStatusEnum().toLowerCase());
			}	
		}
		//
		return ResponseEntity
			.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, url.toString())
			.build();
	}
	
	/**
	 * Redirect to CAS server.
	 * 
	 * @param nativeRequest
	 * @return redirect
	 * @since 12.0.0
	 */
	@RequestMapping(path = CAS_LOGOUT_REQUEST_PATH, method = RequestMethod.GET)
	public ResponseEntity<Void> casLogoutRequest(HttpServletRequest nativeRequest) {
		String casUrl = casConfiguration.getUrl();
		StringBuilder frontendUrl = new StringBuilder(configurationService.getFrontendUrl(CAS_LOGOUT_RESPONSE_PATH));
		//
		// check correct setting - redirect to FE error page otherwise
		if (StringUtils.isBlank(casUrl)) {
			frontendUrl.append("?status-code=");
			frontendUrl.append(CoreResultCode.CAS_LOGOUT_SERVER_URL_NOT_CONFIGURED.getCode().toLowerCase());
			return ResponseEntity
					.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, frontendUrl.toString())
					.build();
		}
		// check CAS server is available
		String casLogoutUrl = String.format("%s%s?service=%s", 
				casUrl, 
				casConfiguration.getLogoutPath(),
				casConfiguration.getService(nativeRequest, false));
		try {
			ResponseEntity<String> response = restTemplate.getForEntity(casLogoutUrl, String.class);
			if (HttpStatus.OK != response.getStatusCode()) {
				frontendUrl.append("?status-code=");
				frontendUrl.append(CoreResultCode.CAS_LOGOUT_SERVER_NOT_AVAILABLE.getCode().toLowerCase());
				return ResponseEntity
						.status(HttpStatus.FOUND)
						.header(HttpHeaders.LOCATION, frontendUrl.toString())
						.build();
			}
		} catch (Exception ex) {
			frontendUrl.append("?status-code=");
			frontendUrl.append(CoreResultCode.CAS_LOGOUT_SERVER_NOT_AVAILABLE.getCode().toLowerCase());
			return ResponseEntity
					.status(HttpStatus.FOUND)
					.header(HttpHeaders.LOCATION, frontendUrl.toString())
					.build();
		}
		//
		// redirect to CAS
		return ResponseEntity
				.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, casLogoutUrl)
				.build();
	}
	
	/**
	 * Redirect to FE, after CAS authentication.
	 * 
	 * @return redirect to FE
	 * @since 12.0.0
	 */
	@RequestMapping(path = CAS_LOGOUT_RESPONSE_PATH, method = RequestMethod.GET)
	public ResponseEntity<Void> casLogoutResponse() {
		StringBuilder url = new StringBuilder(configurationService.getFrontendUrl(CAS_LOGOUT_RESPONSE_PATH));
		//
		return ResponseEntity
			.status(HttpStatus.FOUND)
			.header(HttpHeaders.LOCATION, url.toString())
			.build();
	}
	
	protected void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
}
