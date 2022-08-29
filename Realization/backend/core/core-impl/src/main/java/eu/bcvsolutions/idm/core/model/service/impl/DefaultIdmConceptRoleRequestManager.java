package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestManager;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service
public class DefaultIdmConceptRoleRequestManager implements IdmConceptRoleRequestManager {

    private final Map<Class<? extends AbstractConceptRoleRequestDto>, IdmGeneralConceptRoleRequestService> conceptServices;

    @Autowired
    public DefaultIdmConceptRoleRequestManager(List<IdmGeneralConceptRoleRequestService> conceptServices) {
        this.conceptServices = conceptServices.stream()
                .collect(Collectors.toMap(idmGeneralConceptRoleRequestService -> idmGeneralConceptRoleRequestService.getType(),
                idmGeneralConceptRoleRequestService -> idmGeneralConceptRoleRequestService));
    }

    @Override
    public <C extends AbstractConceptRoleRequestDto> void save(C concept) {
        getServiceForConcept(concept).save(concept);
    }

    @Override
    public <C extends AbstractConceptRoleRequestDto> IdmGeneralConceptRoleRequestService<AbstractRoleAssignmentDto, C, IdmBaseConceptRoleRequestFilter> getServiceForConcept(AbstractConceptRoleRequestDto concept) {
        return conceptServices.get(concept.getClass());
    }

    @Override
    public List<AbstractConceptRoleRequestDto> findAllByRoleRequest(UUID id, Pageable pageable, IdmBasePermission... permissions) {
        final List<AbstractConceptRoleRequestDto> result = new ArrayList<>();
        conceptServices.values()
                .forEach(idmGeneralConceptRoleRequestService ->
                        result.addAll(
                                idmGeneralConceptRoleRequestService.findAllByRoleRequest(id, pageable, permissions)
                        ));
        return result;
    }
}
