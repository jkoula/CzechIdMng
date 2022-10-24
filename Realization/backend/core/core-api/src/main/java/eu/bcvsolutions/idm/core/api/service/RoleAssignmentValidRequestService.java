package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;

import java.util.List;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface RoleAssignmentValidRequestService<T extends AbstractRoleValidRequestDto<?>> {

    /**
     * Method find all {@link IdmIdentityRoleValidRequestDto} that can be process from now = role is valid form today.
     * @return
     */
    List<T> findAllValid();

    void publishOrIncrease(T validRequestDto);
}
