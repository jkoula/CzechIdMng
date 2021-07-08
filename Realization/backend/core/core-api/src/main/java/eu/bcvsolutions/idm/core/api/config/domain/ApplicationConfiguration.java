package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.AppModule;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Common application configuration.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface ApplicationConfiguration extends Configurable {
	
	String STAGE_PRODUCTION = "production";
	String STAGE_DEVELOPMENT = "development";
	String STAGE_TEST = "test";
	
	/**
	 * Application stage.
	 */
	String PROPERTY_STAGE = ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "app.stage";
	String DEFAULT_STAGE = STAGE_PRODUCTION;
	
	@Override
	default String getConfigurableType() {
		return AppModule.MODULE_ID;
	}
	
	@Override
	default String getName() {
		return AppModule.MODULE_ID;
	}
	
	@Override
	default String getConfigurationPrefix() {
		return ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX
				+ getName();
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return false;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		properties.add(getPropertyName(PROPERTY_STAGE));
		return properties;
	}
	
	/**
	 * Application stage.
	 * 
	 * @return configured application stage, or {@link #STAGE_PRODUCTION} as default
	 */
	String getStage();
	
	/**
	 * If application running in development stage.
	 * 
	 * @return true - development stage
	 */
	boolean isDevelopment();
	
	/**
	 * If application running in production stage. Production stage is used as default.
	 * 
	 * @return true - production stage
	 */
	boolean isProduction();
}
