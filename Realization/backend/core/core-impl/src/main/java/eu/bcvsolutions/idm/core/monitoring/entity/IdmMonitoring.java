package eu.bcvsolutions.idm.core.monitoring.entity;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;

/**
 * Monitoring evaluator configuration.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Entity
@Audited
@Table(name = "idm_monitoring", indexes = {
		@Index(name = "idx_idm_monitoring_e_type", columnList = "evaluator_type"),
		@Index(name = "idx_idm_monitoring_inst", columnList = "instance_id")
})
public class IdmMonitoring extends AbstractEntity implements Disableable, InstanceIdentifiable {
	
	private static final long serialVersionUID = 1L;
	
	@NotNull
	@Column(name = "disabled", nullable = false)
	private boolean disabled;
	
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	@Column(name = "description", length = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	@Min(Short.MIN_VALUE)
	@Max(Short.MAX_VALUE)
	@Column(name = "seq")
	private Short seq;

	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "evaluator_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String evaluatorType;
	
	@Column(name = "evaluator_properties", length = Integer.MAX_VALUE)
	private ConfigurationMap evaluatorProperties;
	
	@NotNull
	@Column(name = "instance_id", length = DefaultFieldLengths.NAME, nullable = false)
	private String instanceId;
	
	@Min(0L)
	@Max(Long.MAX_VALUE)
	@Column(name = "check_period")
	private Long checkPeriod;
	
	@Column(name = "execute_date")
	private ZonedDateTime executeDate;

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

	public ConfigurationMap getEvaluatorProperties() {
		return evaluatorProperties;
	}

	public void setEvaluatorProperties(ConfigurationMap evaluatorProperties) {
		this.evaluatorProperties = evaluatorProperties;
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
