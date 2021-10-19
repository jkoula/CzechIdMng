package eu.bcvsolutions.idm.core.security.auth.filter;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.validation.Assertion;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
// import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.CasTicketValidationException;
import eu.bcvsolutions.idm.core.security.api.exception.IdentityDisabledException;
import eu.bcvsolutions.idm.core.security.api.exception.IdentityNotFoundException;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.filter.AbstractAuthenticationFilter;
import eu.bcvsolutions.idm.core.security.api.service.CasValidationService;
import eu.bcvsolutions.idm.core.security.api.service.JwtAuthenticationService;

/**
 * Filter which will authenticate user against CAS.
 *
 * @author Radek Tomiška
 * @author Roman Kučera
 * @since 12.0.0
 */
@Order(50)
@Component("casAuthenticationFilter")
@Enabled(module = CoreModuleDescriptor.MODULE_ID)
public class CasAuthenticationFilter extends AbstractAuthenticationFilter {

	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(CasAuthenticationFilter.class);

	@Autowired private IdmIdentityService identityService;
	@Autowired private CasConfiguration casConfiguration;
	@Autowired private JwtAuthenticationService jwtAuthenticationService;
	@Autowired private AuthenticationExceptionContext ctx;
	@Autowired private CasValidationService validationService;

	@Override
	public boolean authorize(String token, HttpServletRequest request, HttpServletResponse response) {
		String casUrl = casConfiguration.getUrl();
		String service = casConfiguration.getService(request, true);
		//
		if (StringUtils.isBlank(casUrl)) {
			LOG.info("URL for CAS is not set in configuration [{}], CAS authentication will be skipped.",
					CasConfiguration.PROPERTY_URL);
			return false;
		}
		//
		try {
			if (StringUtils.isBlank(token)) {
				LOG.info("No token from CAS");
				return false;
			}

			Assertion assertion = validationService.validate(token, service, casUrl);

			if (assertion == null) {
				LOG.info("No principal name.");
				return false;
			}
			if (!assertion.isValid()) {
				LOG.debug("CAS Ticket [{}] validation failed.", token);
				//
				throw new CasTicketValidationException(MessageFormat.format(
						"CAS Ticket [{0}] validation failed.",
						token));
			}
			//
			String userName = assertion.getPrincipal().getName();
			LOG.debug("Username found [{}]", userName);
			//
			IdmIdentityDto identity = identityService.getByUsername(userName);

			if (identity == null) {
				throw new IdentityNotFoundException(MessageFormat.format(
						"Check identity can login: The identity "
								+ "[{0}] either doesn't exist or is deleted.",
						userName));
			}
			// identity is valid
			if (identity.isDisabled()) {
				throw new IdentityDisabledException(MessageFormat.format(
						"Check identity can login: The identity [{0}] is disabled.", 
						userName));
			}
			LoginDto loginDto = jwtAuthenticationService.createJwtAuthenticationAndAuthenticate(
					createLoginDto(userName),
					identity, 
					CoreModuleDescriptor.MODULE_ID
			);
			//
			LOG.debug("User [{}] successfully logged in.", loginDto.getUsername());
			return true;
		} catch (TwoFactorAuthenticationRequiredException ex) { // must change password exception is never thrown
			ctx.setCodeEx(ex);
			// publish additional authentication requirement
			throw ex;
		} catch (IdmAuthenticationException ex) {
			ctx.setAuthEx(ex);
			LOG.warn("Authentication exception raised during CAS authentication: [{}].", ex.getMessage(), ex);
		} catch (Exception ex) {
			LOG.error("Exception was raised during CAS authentication: [{}].", ex.getMessage(), ex);
		}
		//
		return false;
	}

	private LoginDto createLoginDto(String userName) {
		LoginDto ldto = new LoginDto();
		ldto.setUsername(userName);
		return ldto;
	}

	@Override
	public String getAuthorizationHeaderName() {
		return casConfiguration.getHeaderName();
	}
	
	@Override
	public String getTokenParameterName() {
		return casConfiguration.getParameterName();
	}

	@Override
	public String getAuthorizationHeaderPrefix() {
		return casConfiguration.getHeaderPrefix();
	}

	@Override
	public boolean isDisabled() {
		return false;
	}
	
	@Override
	public boolean isDefaultDisabled() {
		return false; // ~ can be disabled by both properties (filter related + public configuration, see above)
	}
}
