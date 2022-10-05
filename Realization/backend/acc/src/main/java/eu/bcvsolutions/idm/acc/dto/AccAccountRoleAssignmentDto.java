package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import org.springframework.hateoas.core.Relation;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Relation(collectionRelation = "accountRoleAssignments")
public class AccAccountRoleAssignmentDto extends AbstractRoleAssignmentDto {

    private static final long serialVersionUID = 1L;
    @Embedded(dtoClass = AccAccountDto.class)
    private UUID accAccount;

    public AccAccountRoleAssignmentDto(UUID identityRoleId) {
        super(identityRoleId);
    }

    public AccAccountRoleAssignmentDto() {
    }

    public UUID getAccAccount() {
        return accAccount;
    }

    public void setAccAccount(UUID account) {
        this.accAccount = account;
    }

    @Override
    public UUID getEntity() {
        return getAccAccount();
    }
}
