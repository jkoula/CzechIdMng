package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccountRole;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.envers.Audited;
import org.springframework.hateoas.core.Relation;

import javax.persistence.*;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Relation(collectionRelation = "accAccountConceptRoleRequests")
public class AccAccountConceptRoleRequestDto extends AbstractConceptRoleRequestDto {

    private static final long serialVersionUID = 1L;

    @Embedded(dtoClass = AccAccountDto.class)
    private UUID accAccount;

    @Embedded(dtoClass = AccAccountRoleDto.class)
    private UUID accountRole;

    public UUID getAccAccount() {
        return accAccount;
    }

    public void setAccAccount(UUID accAccount) {
        this.accAccount = accAccount;
    }

    public UUID getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(UUID accountRole) {
        this.accountRole = accountRole;
    }

}
