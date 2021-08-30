package eu.bcvsolutions.idm.acc.entity;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

/**
 * System groups system - relation between system and group of systems.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Entity
@Table(name = "sys_system_group_system", indexes = {	
		@Index(name = "idx_sys_group_system_group_id", columnList = "system_group_id"),
		@Index(name = "idx_sys_group_system_id", columnList = "system_id") })
public class SysSystemGroupSystem extends AbstractEntity {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_group_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystemGroup systemGroup;
	
	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem system;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "merge_attribute_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystemAttributeMapping mergeAttribute;

	public SysSystemGroup getSystemGroup() {
		return systemGroup;
	}

	public void setSystemGroup(SysSystemGroup systemGroup) {
		this.systemGroup = systemGroup;
	}

	public SysSystem getSystem() {
		return system;
	}

	public void setSystem(SysSystem system) {
		this.system = system;
	}

	public SysSystemAttributeMapping getMergeAttribute() {
		return mergeAttribute;
	}

	public void setMergeAttribute(SysSystemAttributeMapping mergeAttribute) {
		this.mergeAttribute = mergeAttribute;
	}
}
