package eu.bcvsolutions.idm.core.config.domain;

import org.apache.commons.lang3.StringUtils;

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
}
