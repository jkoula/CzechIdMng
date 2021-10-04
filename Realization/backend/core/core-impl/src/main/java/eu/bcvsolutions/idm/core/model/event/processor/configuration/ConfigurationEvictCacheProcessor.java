package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;

/**
 * Clear configuration cache after configuration property is saved or deleted.
 * 
 * @author Radek Tomiška
 * @since 11.3.0
 */
@Component(ConfigurationEvictCacheProcessor.PROCESSOR_NAME)
@Description("Clear configuration cache after configuration property i saved or deleted.")
public class ConfigurationEvictCacheProcessor 
		extends CoreEventProcessor<IdmConfigurationDto>  
		implements ConfigurationProcessor {
	
	public static final String PROCESSOR_NAME = "core-configuration-evict-cache-processor";
	//
	@Autowired private IdmCacheManager cacheManager;

	public ConfigurationEvictCacheProcessor() {
		// CREATE included => default ~ empty configuration value can be cached (=> property is not saved in database).
		super(ConfigurationEventType.CREATE, ConfigurationEventType.UPDATE, ConfigurationEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmConfigurationDto> process(EntityEvent<IdmConfigurationDto> event) {
		IdmConfigurationDto configuration = event.getContent();
		String configurationCode = configuration.getCode();
		//
		// current configuration property
		cacheManager.evictValue(IdmConfigurationService.CACHE_NAME, configurationCode);
		//
		// previous configuration property, if code is changed
		IdmConfigurationDto previousConfiguration = event.getOriginalSource();
		if (previousConfiguration != null) {
			String previousConfigurationCode = previousConfiguration.getCode();
			if (!Objects.equals(configurationCode, previousConfigurationCode)) {
				cacheManager.evictValue(IdmConfigurationService.CACHE_NAME, previousConfigurationCode);
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 10;
	}
}
