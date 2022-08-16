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
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Intersection table between system and identity - owner of system
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
@Entity
@Table(name = "idm_system_owner", indexes = {
		@Index(name = "idx_idm_system_owner_system", columnList = "system_id"),
		@Index(name = "idx_idm_system_owner_gnt", columnList = "owner_id"),
		@Index(name = "idx_idm_system_owner_ext_id", columnList = "external_id")})
public class SysSystemOwner extends AbstractEntity implements ExternalIdentifiable {

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

	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentity owner; // owner as identity

	/**
	 * System
	 *
	 * @return System entity
	 */
	public SysSystem getSystem() {
		return system;
	}

	/**
	 * System
	 *
	 * @param system entity
	 */
	public void setSystem(SysSystem system) {
		this.system = system;
	}

	/**
	 * Owner as identity
	 *
	 * @return Owner as identity
	 */
	public IdmIdentity getOwner() {
		return owner;
	}

	/**
	 * Owner as identity
	 *
	 * @param owner identity entity
	 */
	public void setOwner(IdmIdentity owner) {
		this.owner = owner;
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
