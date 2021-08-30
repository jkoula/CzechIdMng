package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemGroupSystemEvent.SystemGroupSystemEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * System group-system relation save processor.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Component("sysSystemGroupSystemSaveProcessor")
@Description("Save newly created or update exists system group-system relation.")
public class SystemGroupSystemSaveProcessor extends CoreEventProcessor<SysSystemGroupSystemDto> {

	private static final String PROCESSOR_NAME = "system-group-system-save-processor";

	@Autowired
	private SysSystemGroupSystemService systemGroupService;

	public SystemGroupSystemSaveProcessor() {
		super(SystemGroupSystemEventType.CREATE, SystemGroupSystemEventType.UPDATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemGroupSystemDto> process(EntityEvent<SysSystemGroupSystemDto> event) {
		SysSystemGroupSystemDto systemGroupDto = event.getContent();

		systemGroupDto = systemGroupService.saveInternal(systemGroupDto);
		event.setContent(systemGroupDto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

}
