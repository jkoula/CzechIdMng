package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import org.springframework.hateoas.core.Relation;

import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Relation
public class AccAccountRoleAssignmentDto extends AbstractRoleAssignmentDto {

    private static final long serialVersionUID = 1L;
    private UUID account;

    public AccAccountRoleAssignmentDto(UUID identityRoleId) {
        super(identityRoleId);
    }

    public AccAccountRoleAssignmentDto() {
    }

    public UUID getAccount() {
        return account;
    }

    public void setAccount(UUID account) {
        this.account = account;
    }

    @Override
    public UUID getEntity() {
        return getAccount();
    }
}
