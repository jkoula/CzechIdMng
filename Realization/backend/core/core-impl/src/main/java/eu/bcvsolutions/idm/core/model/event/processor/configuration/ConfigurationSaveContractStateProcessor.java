package eu.bcvsolutions.idm.core.model.event.processor.configuration;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ConfigurationProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.event.ConfigurationEvent.ConfigurationEventType;

/**
 * Validate contract state configuration property.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(ConfigurationSaveContractStateProcessor.PROCESSOR_NAME)
@Description("Validate contract state configuration property.")
public class ConfigurationSaveContractStateProcessor
		extends CoreEventProcessor<IdmConfigurationDto> 
		implements ConfigurationProcessor {
	
	public static final String PROCESSOR_NAME = "core-configuration-save-contract-state-processor";
	
	public ConfigurationSaveContractStateProcessor() {
		super(ConfigurationEventType.UPDATE, ConfigurationEventType.CREATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmConfigurationDto> event) {
		return super.conditional(event)
				&& PrivateIdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_STATE
					.equals(event.getContent().getCode());
	}

	@Override
	public EventResult<IdmConfigurationDto> process(EntityEvent<IdmConfigurationDto> event) {
		IdmConfigurationDto configuration = event.getContent();
		String contractState = configuration.getValue();
		if (StringUtils.isEmpty(contractState)) {
			return new DefaultEventResult<>(event, this);
		}
		//
		try {
			ContractState.valueOf(contractState.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			throw new ResultCodeException(
					CoreResultCode.BAD_VALUE,
					ImmutableMap.of("value", contractState),
					ex
			);
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -100;
	}
}
