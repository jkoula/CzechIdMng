package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.IdmMonitoringTypeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Monitoring manager
 * 
 * @author Vít Švanda
 * @since 10.3.0
 * @deprecated monitoring refactored from scratch in 11.2.0
 */
@Deprecated(since ="11.2.0")
public interface MonitoringManager {
	
	public IdmMonitoringTypeDto check(String monitoringType, BasePermission... permission);


}