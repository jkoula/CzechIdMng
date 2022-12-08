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
        @Index(name = "idx_acc_conc_role_account_id", columnList = "account_id"),
        @Index(name = "idx_acc_conc_role_request", columnList = "request_role_id"),
        @Index(name = "idx_acc_conc_role_role", columnList = "role_id")
})
public class AccAccountConceptRoleRequest extends AbstractConceptRoleRequest {

    private static final long serialVersionUID = 1L;

    @Audited
    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private AccAccount accAccount;

    @Audited
    @ManyToOne(optional = true)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(name = "identity_role_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private AccAccountRoleAssignment accountRole;

    public AccAccountRoleAssignment getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccAccountRoleAssignment accountRole) {
        this.accountRole = accountRole;
    }

    public AccAccount getAccAccount() {
        return accAccount;
    }

    public void setAccAccount(AccAccount accAccount) {
        this.accAccount = accAccount;
    }
}
