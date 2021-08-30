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
 * System group-system relation delete processor.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Component("sysSystemGroupSystemDeleteProcessor")
@Description("Delete system group-system and ensures referential integrity. Cannot be disabled.")
public class SystemGroupSystemDeleteProcessor extends CoreEventProcessor<SysSystemGroupSystemDto> {

	private static final String PROCESSOR_NAME = "system-group-system-delete-processor";

	@Autowired
	private SysSystemGroupSystemService systemGroupSystemService;

	public SystemGroupSystemDeleteProcessor() {
		super(SystemGroupSystemEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemGroupSystemDto> process(EntityEvent<SysSystemGroupSystemDto> event) {
		SysSystemGroupSystemDto dto = event.getContent();

		systemGroupSystemService.deleteInternal(dto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public boolean isDisableable() {
		// Cannot be disabled - referential integrity.
		return false;
	}
}
