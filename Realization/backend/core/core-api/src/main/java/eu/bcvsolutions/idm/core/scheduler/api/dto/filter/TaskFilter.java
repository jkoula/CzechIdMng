package eu.bcvsolutions.idm.core.scheduler.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import eu.bcvsolutions.idm.core.scheduler.api.dto.Task;

/**
 * Scheduler task filter.
 *
 * @author Radek Tomiška
 * @since 10.2.0
 */
public class TaskFilter extends DataFilter {

	public static final String PARAMETER_INSTANCE_ID = IdmLongRunningTaskFilter.PARAMETER_INSTANCE_ID;
	public static final String PARAMETER_TASK_TYPE = IdmLongRunningTaskFilter.PARAMETER_TASK_TYPE;
	
    public TaskFilter() {
        this(new LinkedMultiValueMap<>());
    }

    public TaskFilter(MultiValueMap<String, Object> data) {
        this(data, null);
    }

    public TaskFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
        super(Task.class , data, parameterConverter);
    }
    
    /**
     * Filter by server instance identifier.
     * 
     * @return server instance identifier
     * @since 10.6.0
     */
    public String getInstanceId() {
		return getParameterConverter().toString(getData(), PARAMETER_INSTANCE_ID);
	}
	
    /**
     * Filter by server instance identifier.
     * 
     * @param instanceId server instance identifier
     * @since 10.6.0
     */
	public void setInstanceId(String instanceId) {
		set(PARAMETER_INSTANCE_ID, instanceId);
	}
	
	/**
	 * Scheduled task by task executor canonical name (equals).
	 * 
	 * @return canonical class name
	 * @since 11.1.0
	 */
	public String getTaskType() {
		return getParameterConverter().toString(getData(), PARAMETER_TASK_TYPE);
	}

	/**
	 * Scheduled task by task executor canonical name (equals).
	 * 
	 * @param taskType canonical class name
	 * @since 11.1.0
	 */
	public void setTaskType(String taskType) {
		set(PARAMETER_TASK_TYPE, taskType);
	}
}
