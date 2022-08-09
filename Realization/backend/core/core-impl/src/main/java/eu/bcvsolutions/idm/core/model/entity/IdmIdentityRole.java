package eu.bcvsolutions.idm.core.model.entity;

import java.time.LocalDate;
import java.util.UUID;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.envers.Audited;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;

/**
 * Assigned identity role
 * - roles are related to identity's contract
 * 
 * @author Radek Tomiška
 *
 */
@Entity
@Table(name = "idm_identity_role", indexes = {
		@Index(name = "ux_idm_identity_role_cont_aut", columnList = "identity_contract_id,contract_position_id,automatic_role_id", unique = true),
		@Index(name = "idx_idm_identity_role_ident_c", columnList = "identity_contract_id"),
		@Index(name = "idx_idm_identity_role_con_pos", columnList = "contract_position_id"),
		@Index(name = "idx_idm_identity_role_role", columnList = "role_id"),
		@Index(name = "idx_idm_identity_role_aut_r", columnList = "automatic_role_id"),
		@Index(name = "idx_idm_identity_role_ext_id", columnList = "external_id"),
		@Index(name = "idx_idm_identity_role_d_r_id", columnList = "direct_role_id"),
		@Index(name = "idx_idm_identity_role_comp_id", columnList = "role_composition_id")
})
public class IdmIdentityRole extends AbstractRoleAssignment implements ValidableEntity, AuditSearchable, ExternalIdentifiable, FormableEntity {

	private static final long serialVersionUID = 9208706652291035265L;
	
	@Audited
	@Size(max = DefaultFieldLengths.NAME)
	@Column(name = "external_id", length = DefaultFieldLengths.NAME)
	private String externalId;

	// this cannot be abstracted in AbstractRoleAssignment, because of different column names in each subclass
	@Audited
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "identity_contract_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmIdentityContract identityContract;
	
	@Audited
	@ManyToOne(optional = true)
	@JoinColumn(name = "contract_position_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private IdmContractPosition contractPosition;

	public IdmIdentityRole() {
		super();
	}

	public IdmIdentityRole(UUID id) {
		super(id);
	}
	
	public IdmIdentityRole(IdmIdentityContract identityContract) {
		super(identityContract);
		//
		this.identityContract = identityContract;
 	}
	
	public IdmIdentityContract getIdentityContract() {
		return identityContract;
	}
	
	public void setIdentityContract(IdmIdentityContract identityContract) {
		this.identityContract = identityContract;
	}

	/**
	 * Check if this entity is valid from now
	 * @return
	 */
	public boolean isValid() {
		return EntityUtils.isValid(this);
	}

	@Override
	public String getOwnerId() {
		return this.getIdentityContract().getIdentity().getId().toString();
	}

	@Override
	public String getOwnerCode() {
		return this.getIdentityContract().getIdentity().getCode();
	}

	@Override
	public String getOwnerType() {
		return IdmIdentity.class.getName();
	}

	@Override
	public String getSubOwnerId() {
		return this.getRole().getId().toString();
	}

	@Override
	public String getSubOwnerCode() {
		return this.getRole().getCode();
	}

	@Override
	public String getSubOwnerType() {
		return IdmRole.class.getName();
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}

	public IdmContractPosition getContractPosition() {
		return contractPosition;
	}
	
	public void setContractPosition(IdmContractPosition contractPosition) {
		this.contractPosition = contractPosition;
	}
}
