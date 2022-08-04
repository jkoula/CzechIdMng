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

@Component("accSystemAttributeMappingUpdateProcessor")
@Description("Update strategy for attribute in roles where is overloaded")
public class SystemAttributeMappingUpdateProcessor extends CoreEventProcessor<SysSystemAttributeMappingDto> {

	private static final String PROCESSOR_NAME = "system-attribute-mapping-update-processor";

	private final SysSystemAttributeMappingService systemAttributeMappingService;

	@Autowired
	public SystemAttributeMappingUpdateProcessor(
			SysSystemAttributeMappingService systemAttributeMappingService) {
		super(SystemAttributeMappingEventType.UPDATE);

		Assert.notNull(systemAttributeMappingService, "Service is required.");

		this.systemAttributeMappingService = systemAttributeMappingService;
	}
	
	@Override
	public EventResult<SysSystemAttributeMappingDto> process(EntityEvent<SysSystemAttributeMappingDto> event) {
		//TODO run LRT with recalculation

		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 100;
	}
	
	@Override
	public boolean isDisableable() {
		return true;
	}
}
