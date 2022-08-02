package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.event.SystemAttributeMappingEvent.SystemAttributeMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Processor for save {@link SysSystemAttributeMappingDto}
 * 
 * @author Roman Kucera
 *
 */

@Component("accSystemAttributeMappingSaveProcessor")
@Description("Save system attribute mapping. Cannot be disabled.")
public class SystemAttributeMappingSaveProcessor extends CoreEventProcessor<SysSystemAttributeMappingDto> {

	private static final String PROCESSOR_NAME = "system-attribute-mapping-save-processor";

	private final SysSystemAttributeMappingService systemAttributeMappingService;

	@Autowired
	public SystemAttributeMappingSaveProcessor(
			SysSystemAttributeMappingService systemAttributeMappingService) {
		super(SystemAttributeMappingEventType.CREATE, SystemAttributeMappingEventType.UPDATE);

		Assert.notNull(systemAttributeMappingService, "Service is required.");

		this.systemAttributeMappingService = systemAttributeMappingService;
	}
	
	@Override
	public EventResult<SysSystemAttributeMappingDto> process(EntityEvent<SysSystemAttributeMappingDto> event) {
		SysSystemAttributeMappingDto result = systemAttributeMappingService.saveInternal(event.getContent());
		// update content
		event.setContent(result);
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
