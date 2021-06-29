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
 * Delete synchronization configuration - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(SyncConfigDeleteProcessor.PROCESSOR_NAME)
@Description("Delete synchronization configuration - ensures referential integrity.")
public class SyncConfigDeleteProcessor 
		extends CoreEventProcessor<AbstractSysSyncConfigDto> 
		implements SyncConfigProcessor {

	public static final String PROCESSOR_NAME = "acc-sync-config-delete-processor";
	//
	@Autowired private SysSyncConfigService service;

	public SyncConfigDeleteProcessor() {
		super(SyncConfigEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AbstractSysSyncConfigDto> process(EntityEvent<AbstractSysSyncConfigDto> event) {
		AbstractSysSyncConfigDto syncConfig = event.getContent();
		//
		service.deleteInternal(syncConfig);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
