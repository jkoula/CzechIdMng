package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmRoleAssignmentService<D extends AbstractRoleAssignmentDto, F extends BaseRoleAssignmentFilter> extends EventableDtoService<D, F>,
        AuthorizableService<D> {

    /**
     * Get form instance for given identity role
     *
     * @param dto
     * @return
     */
    IdmFormInstanceDto getRoleAttributeValues(D dto);

    /**
     * Validate form attributes for given identityRole
     *
     * @param identityRole
     * @return
     */
    List<InvalidFormAttributeDto> validateFormAttributes(D identityRole);

    @Transactional(readOnly = true)
    Page<D> findByAutomaticRole(UUID automaticRoleId, Pageable pageable);
}
