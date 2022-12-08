package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleAssignmentRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface AccAccountRoleRepository extends IdmRoleAssignmentRepository<AccAccountRoleAssignment> {
    List<AccAccountRoleAssignment> findByAccAccount_Id(UUID id);


}
