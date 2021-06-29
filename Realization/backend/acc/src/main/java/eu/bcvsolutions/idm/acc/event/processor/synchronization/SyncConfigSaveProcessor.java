package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.event.SyncConfigEvent.SyncConfigEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Persists synchronization configuration.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(SyncConfigSaveProcessor.PROCESSOR_NAME)
@Description("Persists synchronization configuration.")
public class SyncConfigSaveProcessor 
		extends CoreEventProcessor<AbstractSysSyncConfigDto>
		implements SyncConfigProcessor {

	public static final String PROCESSOR_NAME = "acc-sync-config-save-processor";
	//
	@Autowired private SysSyncConfigService service;
	
	public SyncConfigSaveProcessor() {
		super(SyncConfigEventType.UPDATE, SyncConfigEventType.CREATE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AbstractSysSyncConfigDto> process(EntityEvent<AbstractSysSyncConfigDto> event) {
		AbstractSysSyncConfigDto dto = event.getContent();
		dto = service.saveInternal(dto);
		//
		event.setContent(dto);
		return new DefaultEventResult<>(event, this);
	}
}