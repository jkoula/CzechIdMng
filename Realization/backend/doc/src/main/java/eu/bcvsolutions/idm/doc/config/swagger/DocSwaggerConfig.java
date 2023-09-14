package eu.bcvsolutions.idm.doc.config.swagger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import eu.bcvsolutions.idm.core.api.config.swagger.AbstractSwaggerConfig;
import eu.bcvsolutions.idm.core.api.domain.ModuleDescriptor;
import eu.bcvsolutions.idm.doc.DocModuleDescriptor;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Doc module swagger configuration
 *
 * @author Jirka Koula
 */
@Configuration
@ConditionalOnProperty(prefix = "springfox.documentation.swagger", name = "enabled", matchIfMissing = true)
public class DocSwaggerConfig extends AbstractSwaggerConfig {

	@Autowired private DocModuleDescriptor moduleDescriptor;

	@Override
	protected ModuleDescriptor getModuleDescriptor() {
		return moduleDescriptor;
	}

	@Bean
	public Docket docApi() {
		return api("eu.bcvsolutions.idm.rest");
	}
}
