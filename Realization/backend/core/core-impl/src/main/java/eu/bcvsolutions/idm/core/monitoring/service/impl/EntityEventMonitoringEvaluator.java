package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;

/**
 * Warning about errors in event queue.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(EntityEventMonitoringEvaluator.NAME)
@Description("Warning about errors in event queue.")
public class EntityEventMonitoringEvaluator extends AbstractDailyMonitoringEvaluator {
	
	public static final String NAME = "core-entity-event-monitoring-evaluator";
	//
	@Autowired private IdmEntityEventService entityEventService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		ResultModel resultModel;
		//
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setStates(Lists.newArrayList(OperationState.EXCEPTION));
		filter.setMonitoringIgnored(Boolean.FALSE);
		Long givenNumberOfDays = getParameterConverter().toLong(monitoring.getEvaluatorProperties(), PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			filter.setCreatedFrom(ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS).minusDays(givenNumberOfDays));
		}
		long count = entityEventService.count(filter);
		//
		if (count > 0) {
			resultModel = new DefaultResultModel(
					CoreResultCode.MONITORING_ENTITY_EVENT_ERROR,
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
