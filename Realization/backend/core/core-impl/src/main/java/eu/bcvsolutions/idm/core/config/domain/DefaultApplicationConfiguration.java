package eu.bcvsolutions.idm.core.config.domain;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;

/**
 * Common application configuration.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class DefaultApplicationConfiguration extends AbstractConfiguration implements ApplicationConfiguration {	
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultApplicationConfiguration.class);
	//
	private String backendUrl = null;
	
	@Override
	public String getStage() {
		String stage = getConfigurationService().getValue(PROPERTY_STAGE);
		if (StringUtils.isBlank(stage)) {
			LOG.debug("Stage by property [{}] is not configured, return default [{}]", PROPERTY_STAGE, DEFAULT_STAGE);
			return DEFAULT_STAGE;
		}
		//
		return stage;
	}
	
	@Override
	public boolean isDevelopment() {
		return STAGE_DEVELOPMENT.equalsIgnoreCase(getStage());
	}
	
	@Override
	public boolean isProduction() {
		return STAGE_PRODUCTION.equalsIgnoreCase(getStage());
	}
	
	@Override
	public String getFrontendUrl() {
		return getConfigurationService().getFrontendUrl("");
	}
	
	@Override
	public String getBackendUrl(HttpServletRequest request) {
		String configuredBackendUrl = getConfigurationService().getValue(PROPERTY_BACKEND_URL);
		if (StringUtils.isNotBlank(configuredBackendUrl)) {
			return configuredBackendUrl;
		}
		// from cache
		if (StringUtils.isNotBlank(backendUrl)) {
			return backendUrl;
		}
		// try to resolve from given request
		if (request == null) {
			return null;
		}
		//
		backendUrl = ServletUriComponentsBuilder
				.fromRequest(request)
				.replacePath(request.getContextPath())
				.replaceQuery(null)
				.build()
				.toUriString();
		//
		return backendUrl;
	}
}
