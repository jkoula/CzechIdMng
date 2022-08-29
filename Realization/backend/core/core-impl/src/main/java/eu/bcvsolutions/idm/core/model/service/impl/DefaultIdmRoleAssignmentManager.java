package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.config.datasource.CoreEntityManager;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service
public class DefaultIdmRoleAssignmentManager implements IdmRoleAssignmentManager {

    @CoreEntityManager
    @Autowired
    EntityManager entityManager;

    private final Map<Class<? extends AbstractRoleAssignmentDto>, IdmRoleAssignmentService> assignmentServices;

    @Autowired
    public DefaultIdmRoleAssignmentManager(List<IdmRoleAssignmentService> assignmentServices) {
        this.assignmentServices = assignmentServices.stream()
                .collect(Collectors.toMap(idmRoleAssignmentService -> idmRoleAssignmentService.getType(),
                        idmRoleAssignmentService -> idmRoleAssignmentService));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AbstractRoleAssignmentDto> findAllByIdentity(UUID id) {
        final List<AbstractRoleAssignmentDto> result = new ArrayList<>();
        assignmentServices.values().forEach(idmRoleAssignmentService -> result.addAll(idmRoleAssignmentService.findAllByIdentity(id)));
        return result;
    }

    @Override
    public <A extends AbstractRoleAssignmentDto> IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> getServiceForAssignment(A identityRole) {
        return assignmentServices.get(identityRole.getClass());
    }
}
