package eu.bcvsolutions.idm.acc.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignmentValidRequest;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/**
 * Request to role that has valid in future
 * 
 * This entity isn't audited.
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */

@Entity
@Table(name = "acc_account_role_valid_req", indexes = {
		@Index(name = "idx_account_role_assignment_id", columnList = "account_role_assignment_id")
})
public class AccAccountRoleAssignmentValidRequest extends AbstractRoleAssignmentValidRequest {

	private static final long serialVersionUID = 1L;

	@NotNull
	@JsonBackReference
	@OneToOne(optional = false)
	@JoinColumn(name = "account_role_assignment_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private AccAccountRoleAssignment accountRoleAssignment;

	public AccAccountRoleAssignment getAccountRoleAssignment() {
		return accountRoleAssignment;
	}

	public void setAccountRoleAssignment(AccAccountRoleAssignment accountRoleAssignment) {
		this.accountRoleAssignment = accountRoleAssignment;
	}
}
