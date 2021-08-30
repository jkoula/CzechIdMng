package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.acc.event.SystemGroupEvent.SystemGroupEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * System group save processor.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Component("sysSystemGroupSaveProcessor")
@Description("Save newly created or update exists system group definition.")
public class SystemGroupSaveProcessor extends CoreEventProcessor<SysSystemGroupDto> {

	private static final String PROCESSOR_NAME = "system-group-save-processor";

	@Autowired
	private SysSystemGroupService systemGroupService;

	public SystemGroupSaveProcessor() {
		super(SystemGroupEventType.CREATE, SystemGroupEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemGroupDto> process(EntityEvent<SysSystemGroupDto> event) {
		SysSystemGroupDto systemGroupDto = event.getContent();

		systemGroupDto = systemGroupService.saveInternal(systemGroupDto);
		event.setContent(systemGroupDto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

}
