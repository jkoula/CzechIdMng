package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmRoleAssignmentManager extends AdaptableService<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>,
        MultiResourceProvider<IdmRequestIdentityRoleDto, IdmRequestIdentityRoleFilter, IdmRequestIdentityRoleDto>{

    List<AbstractRoleAssignmentDto> findAllByIdentity(UUID id);
    <A extends AbstractRoleAssignmentDto> IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> getServiceForAssignment(A identityRole);

    Page<AbstractRoleAssignmentDto> getAllByIdentity(UUID identity, IdmBasePermission[] permissions);

    List<AbstractRoleAssignmentDto> find(IdmRequestIdentityRoleFilter identityRoleFilter, Pageable pageable,
            BiConsumer<AbstractRoleAssignmentDto, IdmRoleAssignmentService<AbstractRoleAssignmentDto, BaseRoleAssignmentFilter>> consumer);

}
