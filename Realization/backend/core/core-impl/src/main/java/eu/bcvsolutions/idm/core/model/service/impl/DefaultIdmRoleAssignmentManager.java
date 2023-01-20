package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.api.utils.ReflectionUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.model.service.util.MultiSourcePagedResource;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service("roleAssignmentManager")
public class DefaultIdmRoleAssignmentManager extends AbstractAdaptableMultiService<
        IdmRequestIdentityRoleDto,
        IdmRequestIdentityRoleFilter,
        IdmRequestIdentityRoleDto> implements IdmRoleAssignmentManager {

    protected final Map<Class<? extends AbstractRoleAssignmentDto>, IdmRoleAssignmentService> assignmentServices;


    @Autowired
    public DefaultIdmRoleAssignmentManager(List<IdmRoleAssignmentService<? extends AbstractRoleAssignmentDto, ?>> assignmentServices, ModelMapper modelMapper) {
        super(modelMapper, assignmentServices);

        this.assignmentServices = assignmentServices.stream().collect(
                Collectors.toMap(IdmRoleAssignmentService::getType,
                s ->  s, ReflectionUtils::resolveMultipleServices));
    }

    @Transactional(readOnly = true)
    @Override
    public List<AbstractRoleAssignmentDto> findAllByIdentity(UUID identityUuid) {
        return assignmentServices.values().stream()
                .flatMap(idmRoleAssignmentService -> (Stream<AbstractRoleAssignmentDto>) idmRoleAssignmentService.findAllByIdentity(identityUuid).stream())
                .collect(Collectors.toList());
    }

    @Override
    public <A extends AbstractRoleAssignmentDto> IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> getServiceForAssignment(A identityRole) {
        return assignmentServices.get(identityRole.getClass());
    }

    @Override
    public Page<AbstractRoleAssignmentDto> getAllByIdentity(UUID identity, IdmBasePermission[] permissions) {
        final List<AbstractRoleAssignmentDto> result = assignmentServices.values().stream().flatMap(idmRoleAssignmentService -> {
            final BaseRoleAssignmentFilter filter = idmRoleAssignmentService.getFilter();
            filter.setIdentityId(identity);
            return (Stream<AbstractRoleAssignmentDto>) idmRoleAssignmentService.find(filter, null, permissions).stream();
        }).collect(Collectors.toList());
        return new PageImpl<>(result, Pageable.unpaged(), result.size());
    }

    @Override
    public List<AbstractRoleAssignmentDto> find(IdmRequestIdentityRoleFilter identityRoleFilter, Pageable pageable,
            BiConsumer<AbstractRoleAssignmentDto, IdmRoleAssignmentService<AbstractRoleAssignmentDto, BaseRoleAssignmentFilter>> consumer) {
        return assignmentServices.values().stream().flatMap(service -> {
            final BaseRoleAssignmentFilter filter = ReflectionUtils.translateFilter(identityRoleFilter, service.getFilter().getClass());
            final Page<AbstractRoleAssignmentDto> page = service.find(filter, pageable);
            //
            page.forEach(o -> consumer.accept(o, service));
            return page.stream();
        }).collect(Collectors.toList());
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
    public MultiSourcePagedResource<IdmRequestIdentityRoleDto,IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto> getMultiResource() {
        List<AdaptableService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>> adaptableServices = new ArrayList<>();
        assignmentServices.values().forEach(adaptableServices::add);
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
