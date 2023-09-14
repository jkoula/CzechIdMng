package eu.bcvsolutions.idm.doc.config.domain;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;

/**
 * Doc configuration - implementation
 *
 * @author Jirka Koula
 *
 */
@Component("docConfiguration")
public class DefaultDocConfiguration
		extends AbstractConfiguration
		implements DocConfiguration {
}
