package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.model.service.util.MultiSourcePagedResource;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service
public class DefaultIdmRoleAssignmentManager extends AbstractAdaptableMultiService<
        IdmRequestIdentityRoleDto,
        IdmRequestIdentityRoleFilter,
        IdmRequestIdentityRoleDto> implements IdmRoleAssignmentManager {

    protected final Map<Class<IdmRequestIdentityRoleDto>, IdmRoleAssignmentService> assignmentServices;
    private final List<AdaptableService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>> adaptableServices;


    @Autowired
    public DefaultIdmRoleAssignmentManager(List<IdmRoleAssignmentService> assignmentServices,
            List<AdaptableService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>> adaptableServices, ModelMapper modelMapper) {
        super(modelMapper, adaptableServices);

        this.adaptableServices = adaptableServices;
        this.assignmentServices = assignmentServices.stream().collect(Collectors.toMap(idmRoleAssignmentService -> idmRoleAssignmentService.getType(),
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

    @Override
    public IdmRequestIdentityRoleDto checkAccess(IdmRequestIdentityRoleDto dto, BasePermission... permission) {
        return null;
    }

    @Override
    public boolean supports(Class<?> delimiter) {
        return false;
    }

    @Override
    public Class<IdmRequestIdentityRoleFilter> getFilterClass() {
        return IdmRequestIdentityRoleFilter.class;
    }

    @Override
    protected MultiSourcePagedResource<? extends BaseDto, ? extends BaseFilter, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto> getMultiResource() {
        return new MultiSourcePagedResource<>(adaptableServices, modelMapper);
    }

    @Override
    public Class<IdmRequestIdentityRoleDto> getDtoClass() {
        return IdmRequestIdentityRoleDto.class;
    }

    @Override
    public<F2 extends BaseFilter> DtoAdapter<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleDto> getAdapter(F2 originalFilter) {
        return input -> input;
    }
}
