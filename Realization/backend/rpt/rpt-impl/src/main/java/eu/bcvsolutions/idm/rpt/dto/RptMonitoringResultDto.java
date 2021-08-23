package eu.bcvsolutions.idm.rpt.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Monitoring evaluator result in report.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Relation(collectionRelation = "monitoringResults")
public class RptMonitoringResultDto extends AbstractDto implements InstanceIdentifiable {
	
	private static final long serialVersionUID = 1L;
	//
	@NotNull
	private String resultMessage;
	private String ownerType;
	private UUID ownerId;
	private String value;
	private NotificationLevel level;
	private String evaluatorType;
	private String evaluatorDescription;
	private String instanceId;
	
	public RptMonitoringResultDto() {
	}
	
	public RptMonitoringResultDto(Auditable auditable) {
		super(auditable);
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
	
	public String getEvaluatorType() {
		return evaluatorType;
	}
	
	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
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

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

	public String getEvaluatorDescription() {
		return evaluatorDescription;
	}

	public void setEvaluatorDescription(String evaluatorDescription) {
		this.evaluatorDescription = evaluatorDescription;
	}
}
