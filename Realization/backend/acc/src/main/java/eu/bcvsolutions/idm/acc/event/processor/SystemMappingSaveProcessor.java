package eu.bcvsolutions.idm.acc.event.processor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Processor for save {@link SysSystemMappingDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Roman Kucera
 *
 */

@Component("accSystemMappingSaveProcessor")
@Description("Save system mapping. Cannot be disabled.")
public class SystemMappingSaveProcessor extends CoreEventProcessor<SysSystemMappingDto> {

	private static final String PROCESSOR_NAME = "system-mapping-save-processor";
	
	private final SysSystemMappingService systemMappingService;

	@Autowired
	public SystemMappingSaveProcessor(
			SysSystemMappingService systemMappingService) {
		super(SystemMappingEventType.CREATE, SystemMappingEventType.UPDATE);
		//
		Assert.notNull(systemMappingService, "Service is required.");
		//
		this.systemMappingService = systemMappingService;

	}
	
	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto dto = event.getContent();
		dto = systemMappingService.saveInternal(dto);

		// Save id of mapping from event into connectedMapping Dto.
		SysSystemMappingDto originalSource = event.getOriginalSource();
		if (originalSource == null || dto.getConnectedSystemMappingId() != originalSource.getConnectedSystemMappingId()) {
			UUID connectedSystemMappingId = dto.getConnectedSystemMappingId();
			if (originalSource != null && originalSource.getConnectedSystemMappingId() != null) {
				SysSystemMappingDto connectedMappingDto = systemMappingService.get(originalSource.getConnectedSystemMappingId());
				if (connectedMappingDto.getConnectedSystemMappingId() != null) {
					connectedMappingDto.setConnectedSystemMappingId(null);
					systemMappingService.saveInternal(connectedMappingDto);
				}
			}
			if (connectedSystemMappingId != null) {
				SysSystemMappingDto connectedMappingDto = systemMappingService.get(connectedSystemMappingId);
				if (connectedMappingDto.getConnectedSystemMappingId() != dto.getConnectedSystemMappingId()) {
					connectedMappingDto.setConnectedSystemMappingId(dto.getId());
					systemMappingService.saveInternal(connectedMappingDto);
				}
			}
		}
		// update content
		event.setContent(dto);
		//
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
