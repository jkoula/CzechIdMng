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
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;

/**
 * Configured monitoring evaluator.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Relation(collectionRelation = "monitorings")
public class IdmMonitoringDto extends AbstractDto implements Disableable, InstanceIdentifiable {

	private static final long serialVersionUID = 1L;
	//
	@NotNull
	private boolean disabled;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	@Min(Short.MIN_VALUE)
	@Max(Short.MAX_VALUE)
	private Short seq;
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	private String evaluatorType;
	private ConfigurationMap evaluatorProperties;
	@NotNull
	private String instanceId;
	private Long checkPeriod;
	private ZonedDateTime executeDate;
	
	public IdmMonitoringDto() {
	}

	public IdmMonitoringDto(UUID id) {
		super(id);
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Short getSeq() {
		return seq;
	}

	public void setSeq(Short seq) {
		this.seq = seq;
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
	 * Delay in seconds between new monitoring evaluation.
	 * - {@code null} - never, manually only
	 * - 0 - when instance is started only (e.g. static environment setting)
	 * - delay in seconds, between next run
	 * 
	 * @return delay in seconds
	 */
	public Long getCheckPeriod() {
		return checkPeriod;
	}
	
	/**
	 * Delay in seconds between new monitoring evaluation.
	 * - {@code null} - never, manually only
	 * - 0 - when instance is started only (e.g. static environment setting)
	 * - delay in seconds, between next run
	 * 
	 * @param checkPeriod delay in seconds
	 */
	public void setCheckPeriod(Long checkPeriod) {
		this.checkPeriod = checkPeriod;
	}
	
	public ZonedDateTime getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(ZonedDateTime executeDate) {
		this.executeDate = executeDate;
	}
}
