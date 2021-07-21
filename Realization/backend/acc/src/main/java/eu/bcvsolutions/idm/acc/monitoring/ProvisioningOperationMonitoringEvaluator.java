package eu.bcvsolutions.idm.acc.monitoring;

import java.time.ZonedDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.service.impl.AbstractDailyMonitoringEvaluator;

/**
 * Warning about errors in provisioning queue.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component(ProvisioningOperationMonitoringEvaluator.NAME)
@Description("Warning about errors in provisioning queue.")
public class ProvisioningOperationMonitoringEvaluator extends AbstractDailyMonitoringEvaluator {
	
	public static final String NAME = "acc-provisioning-operation-monitoring-evaluator";
	//
	@Autowired private SysProvisioningOperationService provisioningOperationService;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		ResultModel resultModel;
		//
		SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
		filter.setResultState(OperationState.EXCEPTION);
		filter.setMonitoringIgnored(Boolean.FALSE);
		Long givenNumberOfDays = getParameterConverter().toLong(monitoring.getEvaluatorProperties(), PARAMETER_NUMBER_OF_DAYS);
		if (givenNumberOfDays != null) {
			filter.setCreatedFrom(ZonedDateTime.now().minusDays(givenNumberOfDays));
		}
		long count = provisioningOperationService.count(filter);
		//
		if (count > 0) {
			resultModel = new DefaultResultModel(
					AccResultCode.MONITORING_PROVISIONING_OPERATION_ERROR,
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
