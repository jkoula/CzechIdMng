package eu.bcvsolutions.idm.core.monitoring.api.service;

import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringResultFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Monitoring results.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public interface IdmMonitoringResultService extends 
		EventableDtoService<IdmMonitoringResultDto, IdmMonitoringResultFilter>,
		AuthorizableService<IdmMonitoringResultDto>,
		ScriptEnabled {
}
