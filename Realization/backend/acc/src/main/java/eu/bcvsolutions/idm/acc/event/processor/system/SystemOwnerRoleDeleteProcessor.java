package eu.bcvsolutions.idm.acc.event.processor.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.event.SystemOwnerRoleEvent.SystemOwnerRoleEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerRoleService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Deletes system owner by role - ensures referential integrity.
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
@Component(SystemOwnerRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes system owner by role from repository.")
public class SystemOwnerRoleDeleteProcessor
		extends CoreEventProcessor<SysSystemOwnerRoleDto>
		implements SystemOwnerRoleProcessor {

	public static final String PROCESSOR_NAME = "acc-system-owner-role-delete-processor";

	@Autowired
	private SysSystemOwnerRoleService service;

	public SystemOwnerRoleDeleteProcessor() {
		super(SystemOwnerRoleEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemOwnerRoleDto> process(EntityEvent<SysSystemOwnerRoleDto> event) {
		SysSystemOwnerRoleDto entityEvent = event.getContent();
		//		
		service.deleteInternal(entityEvent);
		//
		return new DefaultEventResult<>(event, this);
	}
}