package eu.bcvsolutions.idm.core.monitoring.entity;

import java.time.ZonedDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Type;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;

/**
 * Monitoring evaluator result.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Entity
@Table(name = "idm_monitoring_result", indexes = {
		@Index(name = "idx_idm_monitoring_mon_id", columnList = "monitoring_id"),
		@Index(name = "idx_idm_monitoring_r_e_type", columnList = "evaluator_type"),
		@Index(name = "idx_idm_monitoring_r_inst", columnList = "instance_id")
})
public class IdmMonitoringResult extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;

	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "monitoring_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmMonitoring monitoring;
	
	@Embedded
	private OperationResult result;
	
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "owner_type", length = DefaultFieldLengths.NAME, nullable = true)
	private String ownerType;
	
	@Column(name = "owner_id", length = 16, nullable = true)
	private UUID ownerId;
	
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "value", nullable = true)
	private String value;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "level", length = DefaultFieldLengths.ENUMARATION)
	private NotificationLevel level;
	
	@Column(name = "processed_order")
	private Short processedOrder;
	
	@NotEmpty
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "evaluator_type", length = DefaultFieldLengths.NAME, nullable = false)
	private String evaluatorType;
	
	@Column(name = "evaluator_properties", length = Integer.MAX_VALUE)
	private ConfigurationMap evaluatorProperties;
	
	@NotNull
	@Column(name = "instance_id", length = DefaultFieldLengths.NAME, nullable = false)
	private String instanceId;
	
	@Column(name = "monitoring_started")
	private ZonedDateTime monitoringStarted;
	
	@Column(name = "monitoring_ended")
	private ZonedDateTime monitoringEnded;
	
	public IdmMonitoring getMonitoring() {
		return monitoring;
	}
	
	public void setMonitoring(IdmMonitoring monitoring) {
		this.monitoring = monitoring;
	}
	
	public OperationResult getResult() {
		return result;
	}
	
	public void setResult(OperationResult result) {
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

	public ConfigurationMap getEvaluatorProperties() {
		return evaluatorProperties;
	}

	public void setEvaluatorProperties(ConfigurationMap evaluatorProperties) {
		this.evaluatorProperties = evaluatorProperties;
	}

	public String getInstanceId() {
		return instanceId;
	}

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
}
