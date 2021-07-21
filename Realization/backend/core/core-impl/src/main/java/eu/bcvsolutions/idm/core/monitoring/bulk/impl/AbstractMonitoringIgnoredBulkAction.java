package eu.bcvsolutions.idm.core.monitoring.bulk.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.monitoring.api.domain.MonitoringGroupPermission;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.IdmMonitoringFilter;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.MonitoringIgnorableFilter;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringEvaluator;
import eu.bcvsolutions.idm.core.monitoring.api.service.MonitoringManager;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Ignore entity in monitoring.
 * 	
 * @author Radek Tomiška
 * @since 11.2.0
 */
public abstract class AbstractMonitoringIgnoredBulkAction<DTO extends AbstractDto, F extends MonitoringIgnorableFilter> extends AbstractBulkAction<DTO, F> {

	@Autowired private EntityStateManager entityStateManager;
	@Autowired private MonitoringManager monitoringManager;
	
	/**
	 * Return related monitoring evaluator, if monitoring has to be evaluated after bulk action ends.
	 * 
	 * @return related monitoring evaluator
	 */
	protected List<MonitoringEvaluator> getMonitoringEvaluators() {
		return Collections.emptyList();
	};
	
	@Override
	protected OperationResult processDto(DTO dto) {
		IdmEntityStateDto state = new IdmEntityStateDto();
		state.setResult(
				new OperationResultDto.Builder(OperationState.BLOCKED)
					.setModel(new DefaultResultModel(CoreResultCode.MONITORING_IGNORED))
					.build()
		);
		entityStateManager.saveState(dto, state);
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception exception) {
		if (exception != null 
				|| (result != null && OperationState.EXECUTED != result.getState())) {
			return super.end(result, exception);
		}
		//
		try {
			getMonitoringEvaluators()
				.stream()
				.map(this::findMonitoring)
				.filter(Objects::nonNull)
				.forEach(monitoringManager::execute);
			//
			return super.end(result, null);
		} catch (Exception ex) {
			return super.end(result, ex);
		}
	}
	
	/**
	 * Find already registered monitoring evaluator.
	 * 
	 * @return
	 */
	protected IdmMonitoringDto findMonitoring(MonitoringEvaluator monitoringEvaluator) {
		String evaluatorType = AutowireHelper.getTargetType(monitoringEvaluator);
		IdmMonitoringFilter filter = new IdmMonitoringFilter();
		filter.setEvaluatorType(evaluatorType);
		filter.setDisabled(Boolean.FALSE);
		//
		return monitoringManager
				.findMonitorings(filter, null)
				.stream()
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		//
		authorities.add(MonitoringGroupPermission.MONITORINGRESULT_READ);
		authorities.add(MonitoringGroupPermission.MONITORINGRESULT_UPDATE);
		//
		return authorities;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 9500;
	}
	
	@Override
	public NotificationLevel getLevel() {
		return null; // ~ default
	}
}
