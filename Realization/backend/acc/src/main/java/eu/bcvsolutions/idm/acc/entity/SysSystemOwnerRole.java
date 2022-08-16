package eu.bcvsolutions.idm.acc.entity;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * System owner by role
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
@Entity
@Table(name = "idm_system_owner_role", indexes = {
		@Index(name = "idx_idm_system_owner_role_system", columnList = "system_id"),
		@Index(name = "idx_idm_system_owner_role_role", columnList = "owner_role_id"),
		@Index(name = "idx_idm_system_owner_role_ext_id", columnList = "external_id")})
public class SysSystemOwnerRole extends AbstractEntity implements ExternalIdentifiable {

	private static final long serialVersionUID = 6106304497345109366L;

	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "system_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private SysSystem system;

	@NotNull
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmRole ownerRole; // owner as role

	/**
	 * Role owner
	 *
	 * @return system entity
	 */
	public SysSystem getSystem() {
		return system;
	}

	/**
	 * Role owner
	 *
	 * @param system entity
	 */
	public void setSystem(SysSystem system) {
		this.system = system;
	}

	/**
	 * Guarantee as role
	 *
	 * @return owner as role entity
	 */
	public IdmRole getOwnerRole() {
		return ownerRole;
	}

	/**
	 * Owner as role
	 *
	 * @param ownerRole Role entity
	 */
	public void setOwnerRole(IdmRole ownerRole) {
		this.ownerRole = ownerRole;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

}
