package eu.bcvsolutions.idm.configuration.repository.processor;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.configuration.entity.IdmConfiguration;
import eu.bcvsolutions.idm.security.domain.GuardedString;

@Component
public class IdmConfigurationProcessor implements ResourceProcessor<Resource<IdmConfiguration>> {
	
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdmConfigurationProcessor.class);
	
	@Override
	public Resource<IdmConfiguration> process(Resource<IdmConfiguration> resource) {
		// password etc. has to be guarded - can be used just in BE
		if(GuardedString.shouldBeGuarded(resource.getContent().getName())) {
			log.debug("Configuration value for property [{}] is guarded.", resource.getContent().getName());
			resource.getContent().setValue(GuardedString.SECRED_PROXY_STRING);
		}
		return resource;
	}
}
