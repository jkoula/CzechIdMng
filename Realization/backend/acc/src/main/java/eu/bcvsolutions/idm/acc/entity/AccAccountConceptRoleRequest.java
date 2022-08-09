package eu.bcvsolutions.idm.acc.entity;

import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Entity
@Table(name = "acc_account_concept_role_request", indexes = {
        @Index(name = "idx_idm_conc_role_account_id", columnList = "account_id"),
        @Index(name = "idx_idm_conc_role_c_p", columnList = "contract_position_id"),
        @Index(name = "idx_idm_conc_role_request", columnList = "request_role_id"),
        @Index(name = "idx_idm_conc_role_role", columnList = "role_id")
})
public class AccAccountConceptRoleRequest extends AbstractConceptRoleRequest {

    private static final long serialVersionUID = 1L;

    @Audited
    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private AccAccount identityContract;

    @Audited
    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "identity_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private AccAccountRole accountRole;

    public AccAccount getIdentityContract() {
        return identityContract;
    }

    public void setIdentityContract(AccAccount identityContract) {
        this.identityContract = identityContract;
    }

    public AccAccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccAccountRole accountRole) {
        this.accountRole = accountRole;
    }
}
