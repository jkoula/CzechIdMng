package eu.bcvsolutions.idm.doc.event.processor.module;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Description;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;
import eu.bcvsolutions.idm.doc.DocModuleDescriptor;

/**
 * Initialize Doc module.
 *
 * @author Jirka Koula
 *
 */
@Component(DocInitModuleProcessor.PROCESSOR_NAME)
@Description("Initialize Doc module.")
public class DocInitModuleProcessor extends AbstractInitApplicationProcessor {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocInitModuleProcessor.class);
	//
	public static final String PROCESSOR_NAME = "doc-init-module-processor";

	@Override
	public boolean conditional(EntityEvent<ModuleDescriptorDto> event) {
		return super.conditional(event) && isInitDataEnabled();
	}

	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		LOG.info("Module [{}] initialization", DocModuleDescriptor.MODULE_ID);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after admin identity is created
		return CoreEvent.DEFAULT_ORDER + 300;
	}
}
