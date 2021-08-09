package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;

/**
 * Warning about errors in long running task queue.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(LongRunningTaskMonitoringEvaluator.NAME)
@Description("Warning about errors in long running task queue.")
public class LongRunningTaskMonitoringEvaluator extends AbstractDailyMonitoringEvaluator {
	
	public static final String NAME = "core-long-running-task-monitoring-evaluator";
	//
	@Autowired private IdmLongRunningTaskService longRunningTaskService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		ResultModel resultModel;
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setOperationState(OperationState.EXCEPTION);
		filter.setMonitoringIgnored(Boolean.FALSE);
		Long givenNumberOfDays = getParameterConverter().toLong(monitoring.getEvaluatorProperties(), PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			filter.setCreatedFrom(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(givenNumberOfDays));
		}
		long count = longRunningTaskService.count(filter);
		//
		if (count > 0) {
			resultModel = new DefaultResultModel(
					CoreResultCode.MONITORING_LONG_RUNNING_TASK_ERROR,
					ImmutableMap.of(
							"count", Long.toString(count)
					)
			);
		} else {
			resultModel = new DefaultResultModel(CoreResultCode.OK);
		}
		//
		result.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).setModel(resultModel).build());
		result.setValue(String.valueOf(count));
		//
		return result;
	}
}
