package eu.bcvsolutions.idm.core.eav.processor;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.event.FormDefinitionEvent.FormDefinitionEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormDefinitionProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Remove main definition is possible, only if another main definition exists.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(FormDefinitionValidateProcessor.PROCESSOR_NAME)
@Description("Remove main definition is possible, only if another main definition exists.")
public class FormDefinitionValidateProcessor
		extends CoreEventProcessor<IdmFormDefinitionDto> 
		implements FormDefinitionProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-definition-validate-processor";
	
	public FormDefinitionValidateProcessor() {
		super(FormDefinitionEventType.UPDATE, FormDefinitionEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmFormDefinitionDto> event) {
		if (!super.conditional(event)) {
			return false;
		}
		// Check if property for skip validation is sets to true.
		if (getBooleanProperty(FormService.SKIP_EAV_VALIDATION, event.getProperties())) {
			return false;
		}
		return true;
	}

	@Override
	public EventResult<IdmFormDefinitionDto> process(EntityEvent<IdmFormDefinitionDto> event) {
		IdmFormDefinitionDto dto = event.getContent();
		IdmFormDefinitionDto original = event.getOriginalSource();
		//
		if (event.hasType(FormDefinitionEventType.DELETE) && dto.isMain()) {
			throw new ResultCodeException(CoreResultCode.FORM_DEFINITION_DELETE_FAILED_MAIN_FORM, ImmutableMap.of("code", dto.getCode()));
		}
		if (original != null && original.isMain() && !dto.isMain()) {
			throw new ResultCodeException(CoreResultCode.FORM_DEFINITION_UPDATE_FAILED_MAIN_FORM, ImmutableMap.of("code", dto.getCode()));
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -50; // before save / delete
	}
}
