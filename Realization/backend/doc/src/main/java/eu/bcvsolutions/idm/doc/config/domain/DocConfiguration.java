package eu.bcvsolutions.idm.doc.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.Configurable;

/**
 * Doc configuration - interface
 *
 * @author Jirka Koula
 */
public interface DocConfiguration extends Configurable, ScriptEnabled {

	@Override
	default String getConfigurableType() {
		return "configuration";
	}

	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		return properties;
	}
}
