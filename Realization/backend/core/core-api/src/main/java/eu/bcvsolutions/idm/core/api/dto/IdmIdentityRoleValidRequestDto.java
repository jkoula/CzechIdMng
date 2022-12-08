package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

import java.util.UUID;

/**
 * DTO for {@link IdmIdentityRoleValidRequest}
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public class IdmIdentityRoleValidRequestDto extends AbstractRoleValidRequestDto<IdmIdentityRoleDto> {

    private static final long serialVersionUID = -4256009496017969313L;

    @Embedded(dtoClass = IdmIdentityRoleDto.class)
    private UUID identityRole;

    public UUID getIdentityRole() {
        return identityRole;
    }

    public void setIdentityRole(UUID identityRole) {
        this.identityRole = identityRole;
    }

    @Override
    public UUID getRoleAssignmentUuid() {
        return getIdentityRole();
    }
}
