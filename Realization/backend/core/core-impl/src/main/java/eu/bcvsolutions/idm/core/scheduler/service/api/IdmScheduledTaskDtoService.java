package eu.bcvsolutions.idm.core.scheduler.service.api;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.IdmScheduledTaskFilter;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Scheduled tasks service.
 * 
 * @author Jan Helbich
 *
 */
public interface IdmScheduledTaskDtoService
	extends ReadWriteDtoService<IdmScheduledTaskDto, IdmScheduledTaskFilter>, 
	AuthorizableService<IdmScheduledTaskDto, IdmScheduledTaskFilter> {

	/**
	 * Finds scheduled tasks by relative quartz task name.
	 * @param taskName
	 * @return
	 */
	IdmScheduledTaskDto findByQuartzTaskName(String taskName);
	
	/**
	 * Finds scheduled tasks by assigned long running task.
	 * @param lrtId
	 * @return
	 */
	IdmScheduledTaskDto findByLongRunningTaskId(UUID lrtId);

}
