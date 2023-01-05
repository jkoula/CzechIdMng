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
 * Persists system owner by role.
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Component(SystemOwnerRoleSaveProcessor.PROCESSOR_NAME)
@Description("Persists system owner by role.")
public class SystemOwnerRoleSaveProcessor
		extends CoreEventProcessor<SysSystemOwnerRoleDto>
		implements SystemOwnerRoleProcessor {

	public static final String PROCESSOR_NAME = "acc-system-owner-role-save-processor";

	@Autowired
	private SysSystemOwnerRoleService service;

	public SystemOwnerRoleSaveProcessor() {
		super(SystemOwnerRoleEventType.UPDATE, SystemOwnerRoleEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemOwnerRoleDto> process(EntityEvent<SysSystemOwnerRoleDto> event) {
		SysSystemOwnerRoleDto entity = event.getContent();
		entity = service.saveInternal(entity);
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
}
