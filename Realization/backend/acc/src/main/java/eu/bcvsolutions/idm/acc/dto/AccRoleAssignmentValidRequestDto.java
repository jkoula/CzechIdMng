package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;

import java.util.UUID;

/**
 * DTO for {@link IdmIdentityRoleValidRequest}
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */

public class AccRoleAssignmentValidRequestDto extends AbstractRoleValidRequestDto<AccAccountRoleAssignmentDto> {

    private static final long serialVersionUID = -4256009496017969313L;

    @Embedded(dtoClass = AccAccountRoleAssignmentDto.class)
    private UUID accountRoleAssignment;

    public UUID getAccountRoleAssignment() {
        return accountRoleAssignment;
    }

    public void setAccountRoleAssignment(UUID accountRoleAssignment) {
        this.accountRoleAssignment = accountRoleAssignment;
    }

    @Override
    public UUID getRoleAssignmentUuid() {
        return getAccountRoleAssignment();
    }
}
