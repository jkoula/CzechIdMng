package eu.bcvsolutions.idm.acc.event.processor.module;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Creates schema form definitions for schemas which do not have any yet. Needed to override standard mapping. Does not support update which is not needed.
 * 
 * @author Tomáš Doischer
 *
 */
@Component(AccInitSchemaFormDefinitionProcessor.PROCESSOR_NAME)
@Description("Creates schema form definitions for schemas which do not have any yet.")
public class AccInitSchemaFormDefinitionProcessor extends AbstractInitApplicationProcessor {
	
	@Autowired
	private AccSchemaFormAttributeService schemaFormAttributeService;
	@Autowired
	private SysSchemaObjectClassService sysSchemaObjectClassService;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccInitSchemaFormDefinitionProcessor.class);
	public static final String PROCESSOR_NAME = "acc-init-schema-form-definition-processor";
	//
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) && isInitDataEnabled();
	}

	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		List<SysSchemaObjectClassDto> schemas = sysSchemaObjectClassService.find(new SysSchemaObjectClassFilter(), null).getContent();
		for (SysSchemaObjectClassDto schema : schemas) {
			IdmFormDefinitionDto formDefinition = schemaFormAttributeService.getSchemaFormDefinition(schema);
			if (formDefinition == null) {
				// create form definition and attributes 
				LOG.info("Creating form definition and attributes for object class [{}] with ID [{}].", schema.getObjectClassName(), schema.getId());
				schemaFormAttributeService.createSchemaFormAttributes(schema);
			}
		}
		
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after core user role is created
		return CoreEvent.DEFAULT_ORDER + 50;
	}
}
