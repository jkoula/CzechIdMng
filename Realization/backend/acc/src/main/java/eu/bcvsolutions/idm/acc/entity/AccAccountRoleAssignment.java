package eu.bcvsolutions.idm.acc.entity;

import eu.bcvsolutions.idm.core.api.domain.AuditSearchable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Entity
@Table(name = "acc_account_role_assignment", indexes = {
        @Index(name = "idx_acc_account_role_assign_ident_a", columnList = "account_id"),
        @Index(name = "idx_acc_account_role_assign_role", columnList = "role_id"),
        @Index(name = "idx_acc_account_role_assign_aut_r", columnList = "automatic_role_id"),
        @Index(name = "idx_acc_account_role_assign_ext_id", columnList = "external_id"),
        @Index(name = "idx_acc_account_role_assign_d_r_id", columnList = "direct_role_id"),
        @Index(name = "idx_acc_account_role_assign_comp_id", columnList = "role_composition_id")
})
public class AccAccountRoleAssignment extends AbstractRoleAssignment implements ValidableEntity, AuditSearchable, ExternalIdentifiable, FormableEntity {

    private static final long serialVersionUID = 1L;

    @Audited
    @Size(max = DefaultFieldLengths.NAME)
    @Column(name = "external_id", length = DefaultFieldLengths.NAME)
    private String externalId;

    // this cannot be abstracted in AbstractRoleAssignment, because of different column names in each subclass
    @Audited
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    private AccAccount accAccount;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public AccAccount getAccAccount() {
        return accAccount;
    }

    public void setAccAccount(AccAccount account) {
        this.accAccount = account;
    }

    @Override
    public String getOwnerId() {
        return getAccAccount().getId().toString();
    }

    @Override
    public String getOwnerCode() {
        return getAccAccount().getUid();
    }

    @Override
    public String getOwnerType() {
        return AccAccount.class.getName();
    }
}
