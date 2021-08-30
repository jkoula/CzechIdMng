package eu.bcvsolutions.idm.acc.event.processor;

import com.tc.util.Assert;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.event.SystemGroupEvent.SystemGroupEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupService;
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
 * System group delete processor.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Component("sysSystemGroupDeleteProcessor")
@Description("Delete system group and ensures referential integrity. Cannot be disabled.")
public class SystemGroupDeleteProcessor extends CoreEventProcessor<SysSystemGroupDto> {

	private static final String PROCESSOR_NAME = "system-group-delete-processor";

	@Autowired
	private SysSystemGroupService systemGroupService;
	@Autowired
	private SysSystemGroupSystemService systemGroupSystemService;

	public SystemGroupDeleteProcessor() {
		super(SystemGroupEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemGroupDto> process(EntityEvent<SysSystemGroupDto> event) {
		SysSystemGroupDto dto = event.getContent();
		Assert.assertNotNull(dto.getId(), "Id cannot be null for delete!");

		// Delete all connections with systems
		SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
		systemGroupSystemFilter.setSystemGroupId(dto.getId());
		systemGroupSystemService.find(systemGroupSystemFilter, null).forEach(systemGroupSystem -> {
			systemGroupSystemService.delete(systemGroupSystem);
		});

		systemGroupService.deleteInternal(dto);

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
