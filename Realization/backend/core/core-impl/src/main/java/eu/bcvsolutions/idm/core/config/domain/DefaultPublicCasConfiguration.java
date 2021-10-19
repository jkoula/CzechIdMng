package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.PublicCasConfiguration;

/**
 * Cas configuration - public properties (required for FE).
 *
 * @author Radek Tomiška
 * @since 12.0.0
 */
@Component("publicCasConfiguration")
public class DefaultPublicCasConfiguration
		extends AbstractConfiguration
		implements PublicCasConfiguration {
	
	// enabled only - supported by super class
}