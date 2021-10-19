package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Cas configuration - adds private properties, provide public configuration.
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
public interface CasConfiguration extends Configurable, ScriptEnabled {
	
	/**
	 * CAS server base url.
	 */
	String PROPERTY_URL = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.url";

	/**
	 * IdM service name configured as service on CAS server. 
	 * When service is configured, then login and logout redirect urls, should be defined directly in CAS service configuration.
	 * Default: service name for login / logout is created dynamically by BE server url.
	 */
	String PROPERTY_SERVICE = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.service";

	/**
	 * Login path on CAS server.
	 */
	String PROPERTY_LOGIN_PATH = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.login-path";
	String DEFAULT_LOGIN_PATH = "/login";

	/**
	 * Logout path on CAS server.
	 */
	String PROPERTY_LOGOUT_PATH = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.logout-path";
	String DEFAULT_LOGOUT_PATH = "/logout";
	
	/**
	 * Ticket can be given as request parameter (recommended, configured by default).
	 */
	String PROPERTY_PARAMETER_NAME = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.parameter-name";
	String DEFAULT_PARAMETER_NAME = "ticket";

	/**
	 * Ticket can be given as request header. Not configured by default.
	 */
	String PROPERTY_HEADER_NAME = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.header-name";
	String DEFAULT_HEADER_NAME = "";

	/**
	 * Ticket can be given as request header with constant prefix. Prefix will be removed from ticket value. Not configured by default.
	 */
	String PROPERTY_HEADER_PREFIX = ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX +
			"core.cas.header-prefix";
	String DEFAULT_HEADER_PREFIX = "";

	@Override
	default String getConfigurableType() {
		return "cas";
	}
	
	@Override
	default String getName() {
		return this.getConfigurableType();
	}
	
	@Override
	default String getConfigurationPrefix() {
		return ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getName();
	}
	
	@Override
	default boolean isSecured() {
		return true;
	}
	
	/**
	 * Returns public configuration.
	 * 
	 * @return
	 */
	PublicCasConfiguration getPublicConfiguration();

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // disabled is defined in public configuration
		properties.add(getPropertyName(PROPERTY_URL));
		properties.add(getPropertyName(PROPERTY_SERVICE));
		properties.add(getPropertyName(PROPERTY_LOGIN_PATH));
		properties.add(getPropertyName(PROPERTY_LOGOUT_PATH));
		properties.add(getPropertyName(PROPERTY_HEADER_NAME));
		properties.add(getPropertyName(PROPERTY_HEADER_PREFIX));
		properties.add(getPropertyName(PROPERTY_PARAMETER_NAME));
		return properties;
	}
	
	@Override
	default boolean isDisabled() {
		return getPublicConfiguration().isDisabled();
	}
	
	/**
	 * CAS server base url.
	 * 
	 * @return base url
	 */
	String getUrl();

	/**
	 * IdM service name - configured service on CAS server. 
	 * Default: service name for login / logout is created dynamically by BE server url.
	 * 
	 * @param request incoming request to get default value
	 * @param isLogin login / logout default service. Is the same, if is configured explicitelly.
	 * @return service name / url
	 */
	String getService(HttpServletRequest request, boolean isLogin);

	/**
	 * Login path on CAS server.
	 * Default: /login
	 * 
	 * @return url path
	 */
	String getLoginPath();

	/**
	 * Logout path on CAS server.
	 * Default: /logout
	 * 
	 * @return url path
	 */
	String getLogoutPath();
	
	/**
	 * Ticket can be given as request parameter (recommended, configured by default).
	 * Default: ticket
	 * 
	 * @return parameter name
	 */
	String getParameterName();

	/**
	 * Ticket can be given as request header.
	 * 
	 * @return request header name
	 */
	String getHeaderName();

	/**
	 * Ticket can be given as request header with constant prefix. Prefix will be removed from ticket value.
	 * 
	 * @return prefix
	 */
	String getHeaderPrefix();
}