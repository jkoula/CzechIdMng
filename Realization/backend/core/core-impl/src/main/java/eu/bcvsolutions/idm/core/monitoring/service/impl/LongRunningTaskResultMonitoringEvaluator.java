package eu.bcvsolutions.idm.core.monitoring.service.impl;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Monitoring failed {@link eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto}. For monitoring of the failed
 * {@link eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto} see {@link LongRunningTaskMonitoringEvaluator}
 *
 * @since 12.1.0
 * @author Peter Štrunc <peter.strunc@bcvsolutions.eu>
 */
@Component(LongRunningTaskResultMonitoringEvaluator.NAME)
@Description("Warning about errors in long running task items.")
public class LongRunningTaskResultMonitoringEvaluator extends AbstractDailyMonitoringEvaluator{

    public static final String NAME = "core-long-running-task-result-monitoring-evaluator";
    //
    @Autowired
    private IdmProcessedTaskItemService processedTaskItemService;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
        IdmMonitoringResultDto result = new IdmMonitoringResultDto();
        ResultModel resultModel;
        //
        IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
        filter.setOperationState(OperationState.EXCEPTION);
        filter.setTaskMonitoringIgnored(false);

        Long givenNumberOfDays = getParameterConverter().toLong(monitoring.getEvaluatorProperties(), PARAMETER_NUMBER_OF_DAYS);
        if (givenNumberOfDays != null) {
            filter.setCreatedFrom(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(givenNumberOfDays));
        }
        long count = processedTaskItemService.count(filter);
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
