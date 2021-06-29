package eu.bcvsolutions.idm.core.model.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.model.event.MonitoringEvent;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Monitoring manager
 * 
 * @author Vít Švanda
 * 
 * @since 10.3.0
 * @deprecated monitoring refactored from scratch in 11.2.0
 */
@Deprecated(since = "11.1.0")
@Service("deprecatedMonitoringManager")
public class DefaultMonitoringManager implements MonitoringManager {
	
	@Autowired
	public EntityEventManager entityEventManager;
	
	@Override
	public IdmMonitoringTypeDto check(String monitoringType, BasePermission... permission) {
		
		IdmMonitoringTypeDto monitoringTypeDto = new IdmMonitoringTypeDto();
		monitoringTypeDto.setType(monitoringType);
		
		MonitoringEvent event = new MonitoringEvent(MonitoringEvent.MonitoringEventType.CHECK, monitoringTypeDto);
		
		return entityEventManager.process(event).getContent();
	} 
	
	
}
