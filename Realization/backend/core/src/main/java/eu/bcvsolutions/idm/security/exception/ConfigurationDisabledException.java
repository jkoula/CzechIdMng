package eu.bcvsolutions.idm.security.exception;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.exception.CoreResultCode;
import eu.bcvsolutions.idm.core.exception.ResultCodeException;

public class ConfigurationDisabledException extends ResultCodeException  {

	private static final long serialVersionUID = 1L;
	private final String property;

	public ConfigurationDisabledException(String property) {
		super(CoreResultCode.CONFIGURATION_DISABLED, ImmutableMap.of("property", property));
		this.property = property;
	}

	public String getProperty() {
		return property;
	}

}
