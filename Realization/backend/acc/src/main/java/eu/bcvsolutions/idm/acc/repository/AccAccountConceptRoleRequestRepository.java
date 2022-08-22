package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountRole;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleAssignmentRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface AccAccountConceptRoleRequestRepository extends AbstractEntityRepository<AccAccountConceptRoleRequest> {

}
