package eu.bcvsolutions.idm.core.model.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

/**
 * Concept for requested role.
 * @author svandav
 *
 */
@Entity
@Table(name = "idm_concept_role_request", indexes = {
		@Index(name = "idx_idm_conc_role_ident_c", columnList = "identity_contract_id"),
		@Index(name = "idx_idm_conc_role_c_p", columnList = "contract_position_id"),
		@Index(name = "idx_idm_conc_role_request", columnList = "request_role_id"),
		@Index(name = "idx_idm_conc_role_role", columnList = "role_id")
})
public class IdmConceptRoleRequest extends AbstractConceptRoleRequest {

	
	private static final long serialVersionUID = 1L;

	@Audited
	@ManyToOne(optional = true)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "identity_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentityContract identityContract;
	
	@Audited
	@Column(name = "contract_position_id", length = 16, nullable = true)
	private UUID contractPosition;

	@Audited
	@ManyToOne(optional = true)
	@NotFound(action = NotFoundAction.IGNORE)
	@JoinColumn(name = "identity_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentityRole identityRole;

	public IdmIdentityContract getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(IdmIdentityContract identityContract) {
		this.identityContract = identityContract;
	}

	public IdmIdentityRole getIdentityRole() {
		return identityRole;
	}

	public void setIdentityRole(IdmIdentityRole identityRole) {
		this.identityRole = identityRole;
	}

	/**
	 * Other contract position - identifier only
	 * 
	 * @return
	 * @since 9.6.0
	 */
	public UUID getContractPosition() {
		return contractPosition;
	}
	
	/**
	 * Other contract position - identifier only
	 * 
	 * @param contractPosition
	 * @since 9.6.0
	 */
	public void setContractPosition(UUID contractPosition) {
		this.contractPosition = contractPosition;
	}

}
