package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmRoleAssignmentManager {

    List<AbstractRoleAssignmentDto> findAllByIdentity(UUID id);
    <A extends AbstractRoleAssignmentDto> IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> getServiceForAssignment(A identityRole);

}
