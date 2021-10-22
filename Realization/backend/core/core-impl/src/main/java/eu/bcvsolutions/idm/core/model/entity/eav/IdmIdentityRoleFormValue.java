package eu.bcvsolutions.idm.core.model.entity.eav;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;

/**
 * Identity role extended attributes
 * 
 * @author Vít Švanda
 *
 */
@Entity
@Table(name = "idm_identity_role_form_value", indexes = {
		@Index(name = "idx_identity_role_form_a", columnList = "owner_id"),
		@Index(name = "idx_identity_role_form_a_def", columnList = "attribute_id"),
		@Index(name = "idx_identity_role_form_stxt", columnList = "short_text_value"),
		@Index(name = "idx_identity_role_form_uuid", columnList = "uuid_value") })
public class IdmIdentityRoleFormValue extends AbstractFormValue<IdmIdentityRole> {

	private static final long serialVersionUID = 1L;
	
	@Audited
	@ManyToOne(optional = false)
	@JoinColumn(name = "owner_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentityRole owner;
	
	public IdmIdentityRoleFormValue() {
	}
	
	public IdmIdentityRoleFormValue(IdmFormAttribute formAttribute) {
		super(formAttribute);
	}
	
	@Override
	public IdmIdentityRole getOwner() {
		return owner;
	}
	
	public void setOwner(IdmIdentityRole owner) {
		this.owner = owner;
	}

	/**
	 * @since 12.0.0
	 */
	@Override
	public String getOwnerId() {
		return this.getOwner().getIdentityContract().getIdentity().getId().toString();
	}

	/**
	 * @since 12.0.0
	 */
	@Override
	public String getOwnerCode() {
		return this.getOwner().getIdentityContract().getIdentity().getCode();
	}

	/**
	 * @since 12.0.0
	 */
	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getCanonicalName();
	}

	/**
	 * @since 12.0.0
	 */
	@Override
	public String getSubOwnerId() {
		return this.getOwner().getRole().getId().toString();
	}

	/**
	 * @since 12.0.0
	 */
	@Override
	public String getSubOwnerCode() {
		return this.getOwner().getRole().getCode();
	}

	/**
	 * @since 12.0.0
	 */
	@Override
	public String getSubOwnerType() {
		return IdmRole.class.getCanonicalName();
	}
}
