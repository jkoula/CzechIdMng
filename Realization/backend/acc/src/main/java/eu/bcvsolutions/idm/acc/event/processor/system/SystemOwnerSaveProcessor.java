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
 * Persists system owner by identity.
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Component(SystemOwnerSaveProcessor.PROCESSOR_NAME)
@Description("Persists system owner by identity.")
public class SystemOwnerSaveProcessor
		extends CoreEventProcessor<SysSystemOwnerDto>
		implements SystemOwnerProcessor {

	public static final String PROCESSOR_NAME = "acc-system-owner-save-processor";

	@Autowired
	private SysSystemOwnerService service;

	public SystemOwnerSaveProcessor() {
		super(SystemOwnerEventType.UPDATE, SystemOwnerEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemOwnerDto> process(EntityEvent<SysSystemOwnerDto> event) {
		SysSystemOwnerDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
