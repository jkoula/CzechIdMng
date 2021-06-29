package eu.bcvsolutions.idm.acc.event.processor.synchronization;

import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Synchronization config processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface SyncConfigProcessor extends EntityEventProcessor<AbstractSysSyncConfigDto> {
	
}
