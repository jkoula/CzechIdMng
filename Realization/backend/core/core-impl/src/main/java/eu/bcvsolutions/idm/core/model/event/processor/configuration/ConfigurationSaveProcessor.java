package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * Persists configuration property.
 * Confidential properties are saved to confidential storage.
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
@Component(ConfigurationSaveProcessor.PROCESSOR_NAME)
@Description("Persists configuration property. Confidential properties are saved to confidential storage.")
public class ConfigurationSaveProcessor
		extends CoreEventProcessor<IdmConfigurationDto> 
		implements ConfigurationProcessor {
	
	public static final String PROCESSOR_NAME = "core-configuration-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigurationSaveProcessor.class);
	//
	@Autowired private IdmConfigurationService service;
	@Autowired private ConfidentialStorage confidentialStorage;
	
	public ConfigurationSaveProcessor() {
		super(ConfigurationEventType.CREATE, ConfigurationEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmConfigurationDto> process(EntityEvent<IdmConfigurationDto> event) {
		IdmConfigurationDto configuration = event.getContent();
		IdmConfigurationDto previousConfiguration = event.getOriginalSource();
		//
		// check confidential option
		if (GuardedString.shouldBeGuarded(configuration.getCode())) {
			configuration.setConfidential(true);
		}
		// save confidential properties to confidential storage
		String value = configuration.getValue();
		String previousValue = null;
		boolean confidential = configuration.isConfidential();
		if (confidential) {
			if (configuration.getId() != null) {
				previousValue = confidentialStorage.get(
						configuration.getId(), 
						getEntityClass(), 
						IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, 
						String.class
				);
			}
			// we need to know, if value was filled or changed (re saved)
			if (StringUtils.isEmpty(value) && previousValue == null) {
				// empty configuration property now and before too
				configuration.setValue(null);
			} else if (value == null && previousValue != null && previousConfiguration != null) {
				// filled before
				configuration.setValue(previousConfiguration.getValue());
			} else if (Objects.equals(value, previousValue)) {
				// filled with the same value
				configuration.setValue(previousConfiguration.getValue());
			} else if (StringUtils.isEmpty(value)) {
				// empty string => not filled
				configuration.setValue(null);
			} else {
				// new value given
				configuration.setValue(
						String.format("%s-%s", IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, UUID.randomUUID())
				);
			}
		}
		//
		configuration = service.saveInternal(configuration);
		//
		// save new value to confidential storage - empty string should be given for saving empty value. We are leaving previous value otherwise
		if (confidential && value != null && !Objects.equals(value, previousValue)) {
			if (StringUtils.isEmpty(value)) {
				confidentialStorage.delete(
						configuration.getId(), 
						service.getEntityClass(), 
						IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE
				);
				//
				LOG.debug("Configuration value [{}] deleted from confidential storage", configuration.getCode());
			} else {
				confidentialStorage.save(
						configuration.getId(), 
						service.getEntityClass(), 
						IdmConfigurationService.CONFIDENTIAL_PROPERTY_VALUE, 
						value
				);
				//
				LOG.debug("Configuration value [{}] is persisted in confidential storage", configuration.getCode());
			}
			
		}
		//
		event.setContent(configuration);
		//
		return new DefaultEventResult<>(event, this);
	}
}
