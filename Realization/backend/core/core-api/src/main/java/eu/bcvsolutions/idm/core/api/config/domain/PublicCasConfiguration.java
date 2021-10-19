package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.List;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Cas configuration - public properties (required for FE).
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
public interface PublicCasConfiguration extends Configurable, ScriptEnabled {

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
		return ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getName();
	}
	
	@Override
	default boolean isSecured() {
		return false;
	}
	
	@Override
	default boolean isDefaultDisabled() {
		return true;
	}

	@Override
	default List<String> getPropertyNames() {
		return Configurable.super.getPropertyNames(); // add disabled support
	}
}