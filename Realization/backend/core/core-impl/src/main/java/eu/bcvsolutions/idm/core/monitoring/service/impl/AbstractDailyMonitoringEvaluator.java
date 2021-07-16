package eu.bcvsolutions.idm.core.monitoring.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.context.annotation.Description;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.monitoring.api.service.AbstractMonitoringEvaluator;

/**
 * Warning about something happen during a day (configurable).
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Description("Warning about something happend durring a day (configurable).")
public abstract class AbstractDailyMonitoringEvaluator extends AbstractMonitoringEvaluator {
	
	public static final String PARAMETER_NUMBER_OF_DAYS = "numberOfDays"; // records not older than
	public static final int DEFAULT_NUMBER_OF_DAYS = 1;
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_NUMBER_OF_DAYS);
		//
		return parameters;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto numberOfDaysAttribute = new IdmFormAttributeDto(PARAMETER_NUMBER_OF_DAYS, PARAMETER_NUMBER_OF_DAYS, PersistentType.LONG);
		numberOfDaysAttribute.setDefaultValue(String.valueOf(DEFAULT_NUMBER_OF_DAYS));
		numberOfDaysAttribute.setMin(BigDecimal.ONE);
		//
		return Lists.newArrayList(numberOfDaysAttribute);
	}
}
