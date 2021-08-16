package eu.bcvsolutions.idm.core.eav.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent;
import eu.bcvsolutions.idm.core.eav.api.event.FormAttributeEvent.FormAttributeEventType;
import eu.bcvsolutions.idm.core.eav.api.event.FormDefinitionEvent.FormDefinitionEventType;
import eu.bcvsolutions.idm.core.eav.api.event.processor.FormDefinitionProcessor;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Deletes form definition - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
@Component(FormDefinitionDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes form definition from repository.")
public class FormDefinitionDeleteProcessor
		extends CoreEventProcessor<IdmFormDefinitionDto> 
		implements FormDefinitionProcessor {
	
	public static final String PROCESSOR_NAME = "core-form-definition-delete-processor";
	//
	@Autowired private IdmFormDefinitionService formDefinitionService;
	@Autowired private IdmFormAttributeService formAttributeService;
	
	public FormDefinitionDeleteProcessor() {
		super(FormDefinitionEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmFormDefinitionDto> process(EntityEvent<IdmFormDefinitionDto> event) {
		IdmFormDefinitionDto formDefinition = event.getContent();
		UUID id = formDefinition.getId();
		Assert.notNull(id, "Form definition identifier is required for delete.");
		//
		boolean forceDelete = getBooleanProperty(PROPERTY_FORCE_DELETE, event.getProperties());
		if (forceDelete) {
			//
			// delete all attributes in definition
			IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
			filter.setDefinitionId(id);
			formAttributeService.find(filter, null).forEach(formAttribute -> {
				Map<String, Serializable> properties = new HashMap<>();
				properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE); // force
				FormAttributeEvent formAttributeEvent = new FormAttributeEvent(FormAttributeEventType.DELETE, formAttribute, properties);
				//
				formAttributeService.publish(formAttributeEvent, event);
			});
		}
		//		
		formDefinitionService.deleteInternal(formDefinition);
		//
		return new DefaultEventResult<>(event, this);
	}
}