package eu.bcvsolutions.idm.acc.event.processor.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.event.SystemOwnerEvent.SystemOwnerEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Deletes system owner by identity - ensures referential integrity.
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
@Component(SystemOwnerDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes system owner by identity - ensures referential integrity.")
public class SystemOwnerDeleteProcessor
		extends CoreEventProcessor<SysSystemOwnerDto>
		implements SystemOwnerProcessor {

	public static final String PROCESSOR_NAME = "acc-system-owner-delete-processor";

	@Autowired
	private SysSystemOwnerService service;

	public SystemOwnerDeleteProcessor() {
		super(SystemOwnerEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemOwnerDto> process(EntityEvent<SysSystemOwnerDto> event) {
		SysSystemOwnerDto entityEvent = event.getContent();
		//		
		service.deleteInternal(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
}