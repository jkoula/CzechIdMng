package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.util.List;

import javax.persistence.Table;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.BaseDtoService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringDto;
import eu.bcvsolutions.idm.core.monitoring.api.dto.IdmMonitoringResultDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.AbstractMonitoringEvaluator;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Warning about too many records in database table.
 *
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(DatabaseTableMonitoringEvaluator.NAME)
@Description("Warning about too many records in database table.")
public class DatabaseTableMonitoringEvaluator extends AbstractMonitoringEvaluator {
	
	public static final String NAME = "core-database-table-monitoring-evaluator";
	public static final String PARAMETER_THRESHOLD = "threshold";
	public static final long DEFAULT_TRESHOLD = 500000L;
	public static final String PARAMETER_READ_SERVICE_BEAN_NAME = "service-bean-name"; // read dto service bean name
	//
	@Autowired private ApplicationContext context;
	
	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public IdmMonitoringResultDto evaluate(IdmMonitoringDto monitoring) {
		String serviceName = getParameterConverter().toString(monitoring.getEvaluatorProperties(), PARAMETER_READ_SERVICE_BEAN_NAME);
		Object bean;
		try {
			bean = context.getBean(serviceName);
			if (bean == null || !(bean instanceof ReadDtoService<?, ?>)) {
				throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", serviceName));
			}
		} catch (BeansException ex) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", serviceName), ex);
		}
		//
		ReadDtoService<?, ?> readService = (ReadDtoService<?, ?>) bean;
		long treshold = getParameterConverter().toLong(monitoring.getEvaluatorProperties(), PARAMETER_THRESHOLD, DEFAULT_TRESHOLD);
		long count = readService.count(null);
		ResultModel resultModel = new DefaultResultModel(
				CoreResultCode.MONITORING_DATABASE_TABLE,
				ImmutableMap.of(
						"tableName", String.valueOf(getTableName(readService)),
						"dtoName", String.valueOf(getDtoName(readService)),
						"count", Long.toString(count)
				)
		);
		IdmMonitoringResultDto result = new IdmMonitoringResultDto();
		result.setValue(Long.toString(count));
		result.setResult(new OperationResultDto.Builder(OperationState.EXECUTED).setModel(resultModel).build());
		if (treshold < count) {
			result.setLevel(NotificationLevel.WARNING);
		} 
		//
		return result;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_THRESHOLD);
		parameters.add(PARAMETER_READ_SERVICE_BEAN_NAME);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto treshold = new IdmFormAttributeDto(PARAMETER_THRESHOLD, PARAMETER_THRESHOLD, PersistentType.LONG);
		treshold.setDefaultValue(Long.toString(DEFAULT_TRESHOLD));
		treshold.setRequired(true);
		//
		IdmFormAttributeDto serviceName = new IdmFormAttributeDto(PARAMETER_READ_SERVICE_BEAN_NAME, PARAMETER_READ_SERVICE_BEAN_NAME, PersistentType.TEXT);
		serviceName.setRequired(true);
		serviceName.setFaceType(BaseFaceType.READ_DTO_SERVICE_SELECT);
		//
		return Lists.newArrayList(
				treshold,
				serviceName
		);
	}
	
	/**
	 * Resolve table name from entity.
	 * 
	 * @param service base service
	 * @return table name or {@code null}, if e.g. WF tables are reused
	 */
	protected String getTableName(BaseDtoService<?> service) {
		Class<?> entityClass = service.getEntityClass();
		if (entityClass == null) {
			return null;
		}
		Table table = entityClass.getAnnotation(Table.class);
		if (table != null) {
			return table.name();
		}
		//
		return null;
	}
	
	/**
	 * Resolve controlled dto name.
	 * 
	 * @param service base service
	 * @return
	 */
	protected String getDtoName(BaseDtoService<?> service) {
		Class<?> dtoClass = service.getDtoClass();
		if (dtoClass == null) {
			return null;
		}
		//
		return dtoClass.getSimpleName();
	}
}
