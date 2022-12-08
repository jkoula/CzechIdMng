package eu.bcvsolutions.idm.core.model.entity;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Request to role that has valid in future
 * 
 * This entity isn't audited.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Entity
@Table(name = "idm_identity_role_valid_req", indexes = {
		@Index(name = "idx_idm_identity_role_id", columnList = "identity_role_id")
})
public class IdmIdentityRoleValidRequest extends AbstractRoleAssignmentValidRequest {

	private static final long serialVersionUID = 4194613317799794221L;

	@NotNull
	@JsonBackReference
	@OneToOne(optional = false)
	@JoinColumn(name = "identity_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentityRole identityRole;

	public IdmIdentityRole getIdentityRole() {
		return identityRole;
	}

	public void setIdentityRole(IdmIdentityRole identityRole) {
		this.identityRole = identityRole;
	}

}
