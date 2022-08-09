package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.core.Relation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Relation
public class AccAccountRoleDto extends AbstractRoleAssignmentDto {

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
    private AccAccount account;

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public AccAccount getAccount() {
        return account;
    }

    public void setAccount(AccAccount account) {
        this.account = account;
    }
}
