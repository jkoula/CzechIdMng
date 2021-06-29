package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSyncConfigDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Synchronization configuration event.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class SyncConfigEvent extends CoreEvent<SysSyncConfigDto> {

	private static final long serialVersionUID = 1L;

	public enum SyncConfigEventType implements EventType {
		CREATE, UPDATE, DELETE;
	}

	public SyncConfigEvent(SyncConfigEventType operation, SysSyncConfigDto content) {
		super(operation, content);
	}

	public SyncConfigEvent(SyncConfigEventType operation, SysSyncConfigDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
