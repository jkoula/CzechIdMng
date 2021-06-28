package eu.bcvsolutions.idm.core.monitoring.api.service;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Configured monitoring evaluators.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface IdmMonitoringService extends 
		EventableDtoService<IdmMonitoringDto, IdmMonitoringFilter>,
		AuthorizableService<IdmMonitoringDto>,
		ScriptEnabled {
}
