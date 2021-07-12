package eu.bcvsolutions.idm.core.monitoring.api.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Monitoring evaluator result.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Relation(collectionRelation = "monitoringResults")
public class IdmMonitoringResultDto  extends AbstractDto implements InstanceIdentifiable {
	
	private static final long serialVersionUID = 1L;
	//
	@NotNull
	@Embedded(dtoClass = IdmMonitoringDto.class)
	private UUID monitoring;
	private OperationResultDto result;
	private String ownerType;
	private UUID ownerId;
	private String value;
	private NotificationLevel level;
	@Min(Short.MIN_VALUE)
	@Max(Short.MAX_VALUE)
	private Short processedOrder;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String evaluatorType;
	private ConfigurationMap evaluatorProperties;
	@NotNull
	private String instanceId;
	private ZonedDateTime monitoringStarted;
	private ZonedDateTime monitoringEnded;
	private boolean lastResult;
	
	public IdmMonitoringResultDto() {
	}
	
	public IdmMonitoringResultDto(OperationResultDto result) {
		this.result = result;
	}
	
	public UUID getMonitoring() {
		return monitoring;
	}
	
	public void setMonitoring(UUID monitoring) {
		this.monitoring = monitoring;
	}
	
	public OperationResultDto getResult() {
		return result;
	}
	
	public void setResult(OperationResultDto result) {
		this.result = result;
	}
	
	public String getOwnerType() {
		return ownerType;
	}
	
	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
	
	public UUID getOwnerId() {
		return ownerId;
	}
	
	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public NotificationLevel getLevel() {
		return level;
	}
	
	public void setLevel(NotificationLevel level) {
		this.level = level;
	}
	
	public Short getProcessedOrder() {
		return processedOrder;
	}
	
	public void setProcessedOrder(Short processedOrder) {
		this.processedOrder = processedOrder;
	}
	
	public String getEvaluatorType() {
		return evaluatorType;
	}
	
	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}
	
	public void setEvaluatorProperties(ConfigurationMap evaluatorProperties) {
		this.evaluatorProperties = evaluatorProperties;
	}

	public ConfigurationMap getEvaluatorProperties() {
		if (evaluatorProperties == null) {
			evaluatorProperties = new ConfigurationMap();
		}
		return evaluatorProperties;
	}
	
	@Override
	public String getInstanceId() {
		return instanceId;
	}
	
	@Override
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	
	/**
	 * Monitoring result value.
	 * 
	 * @return result value - information purpose only
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Monitoring result value.
	 * 
	 * @param resultValue result value - information purpose only
	 */
	public void setValue(String value) {
		this.value = value;
	}

	public ZonedDateTime getMonitoringStarted() {
		return monitoringStarted;
	}

	public void setMonitoringStarted(ZonedDateTime monitoringStarted) {
		this.monitoringStarted = monitoringStarted;
	}

	public ZonedDateTime getMonitoringEnded() {
		return monitoringEnded;
	}

	public void setMonitoringEnded(ZonedDateTime monitoringEnded) {
		this.monitoringEnded = monitoringEnded;
	}
	
	/**
	 * Last result flag.
	 * 
	 * @return true - last result
	 * @since 11.2.0
	 */
	public boolean isLastResult() {
		return lastResult;
	}
	
	/**
	 * Last result flag.
	 * 
	 * @param lastResult true - last result
	 * @since 11.2.0
	 */
	public void setLastResult(boolean lastResult) {
		this.lastResult = lastResult;
	}
}
