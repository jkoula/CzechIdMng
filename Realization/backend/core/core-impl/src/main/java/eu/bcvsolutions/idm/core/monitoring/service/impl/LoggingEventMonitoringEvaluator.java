package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.audit.dto.filter.IdmLoggingEventFilter;
import eu.bcvsolutions.idm.core.api.audit.service.IdmLoggingEventService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.LogType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;

/**
 * Warning about errors in logging events
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(LoggingEventMonitoringEvaluator.NAME)
@Description("Warning about errors in logging events.")
public class LoggingEventMonitoringEvaluator extends AbstractDailyMonitoringEvaluator {
	
	public static final String NAME = "core-logging-event-monitoring-evaluator";
	//
	@Autowired private IdmLoggingEventService loggingEventService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		ResultModel resultModel;
		//
		IdmLoggingEventFilter filter = new IdmLoggingEventFilter();
		filter.setLevelString(LogType.ERROR);
		Long givenNumberOfDays = getParameterConverter().toLong(monitoring.getEvaluatorProperties(), PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			filter.setFrom(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(givenNumberOfDays));
		}
		long count = loggingEventService.count(filter);
		//
		if (count > 0) {
			resultModel = new DefaultResultModel(
					CoreResultCode.MONITORING_LOGGING_EVENT_ERROR,
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
