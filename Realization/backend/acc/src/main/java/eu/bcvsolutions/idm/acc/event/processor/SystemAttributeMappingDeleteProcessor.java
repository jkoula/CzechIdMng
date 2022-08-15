package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Processor for delete {@link SysSystemAttributeMappingDto}
 * 
 * @author Roman Kucera
 *
 */
@Component("accSystemAttributeMappingDeleteProcessor")
@Description("Remove attribute mapping. Cannot be disabled.")
public class SystemAttributeMappingDeleteProcessor extends CoreEventProcessor<SysSystemAttributeMappingDto> {

	private static final String PROCESSOR_NAME = "system-attribute-mapping-delete-processor";

	private final SysSystemAttributeMappingService systemAttributeMappingService;

	@Autowired
	public SystemAttributeMappingDeleteProcessor(
			SysSystemAttributeMappingService systemAttributeMappingService) {
		super(SystemMappingEventType.DELETE);
		//
		Assert.notNull(systemAttributeMappingService, "Service is required.");
		//
		this.systemAttributeMappingService = systemAttributeMappingService;
	}

	@Override
	public EventResult<SysSystemAttributeMappingDto> process(EntityEvent<SysSystemAttributeMappingDto> event) {
		systemAttributeMappingService.deleteInternal(event.getContent());
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
