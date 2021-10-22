package eu.bcvsolutions.idm.core.config.domain;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.CasConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.PublicCasConfiguration;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.security.rest.impl.LoginController;

/**
 * Cas configuration - adds private properties, provide public configuration.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
@Component("casConfiguration")
public class DefaultCasConfiguration
		extends AbstractConfiguration
		implements CasConfiguration {
	
	@Autowired private PublicCasConfiguration publicCasConfiguration;
	@Autowired private ApplicationConfiguration applicationConfiguration;
	
	@Override
	public PublicCasConfiguration getPublicConfiguration() {
		return publicCasConfiguration;
	}

	@Override
	public String getUrl() {
		String value = getConfigurationService().getValue(PROPERTY_URL);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return value;
	}

	@Override
	public String getService(HttpServletRequest request, boolean isLogin) {
		String value = getConfigurationService().getValue(PROPERTY_SERVICE);
		if (!StringUtils.isBlank(value)) {
			return value;
		}
		// service = login CAS response endpoint by default
		String backendUrl = applicationConfiguration.getBackendUrl(request);
		if (backendUrl == null) {
			backendUrl = ""; // ~ relative path will be returned as fallback
		}
		// append login / logout path 
		String backendUrlPath = String.format(
				"%s%s%s%s",
				backendUrl,
				BaseController.BASE_PATH, 
				LoginController.AUTH_PATH, 
				isLogin ? LoginController.CAS_LOGIN_RESPONSE_PATH : LoginController.CAS_LOGOUT_RESPONSE_PATH
		);
		//
		return backendUrlPath;
	}

	@Override
	public String getLoginPath() {
		return getConfigurationService().getValue(PROPERTY_LOGIN_PATH, DEFAULT_LOGIN_PATH);
	}

	@Override
	public String getLogoutPath() {
		return getConfigurationService().getValue(PROPERTY_LOGOUT_PATH, DEFAULT_LOGOUT_PATH);
	}

	@Override
	public String getHeaderName() {
		return getConfigurationService().getValue(PROPERTY_HEADER_NAME, DEFAULT_HEADER_NAME);
	}

	@Override
	public String getHeaderPrefix() {
		return getConfigurationService().getValue(PROPERTY_HEADER_PREFIX, DEFAULT_HEADER_PREFIX);
	}
	
	@Override
	public String getParameterName() {
		return getConfigurationService().getValue(PROPERTY_PARAMETER_NAME, DEFAULT_PARAMETER_NAME);
	}
}