package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmRoleAssignmentService<D extends AbstractRoleAssignmentDto, F extends BaseRoleAssignmentFilter> extends EventableDtoService<D, F>,
        AuthorizableService<D>, AdaptableService<D, F, IdmRequestIdentityRoleDto>, ReadDtoService<D, F>{

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

    /**
     * Returns assigned roles by given automatic role.
     *
     * @param roleTreeNodeId
     * @return
     */
    abstract Page<D> findByAutomaticRole(UUID roleTreeNodeId, Pageable pageable);

    /**
     * Returns all roles with date lower than given expiration date.
     *
     * @param expirationDate valid till < expirationDate
     * @param pageable add sort if needed
     * @return all expired roles
     * @see #findDirectExpiredRoles(LocalDate, Pageable)
     */
    Page<D> findExpiredRoles(LocalDate expirationDate, Pageable pageable);

    /**
     * Returns all direct roles with date lower than given expiration date. Automatic roles are included, sub roles not.
     *
     * @param expirationDate valid till < expirationDate
     * @param pageable add sort if needed
     * @return expired roles without sub roles
     * @since 10.2.0
     */
    Page<D> findDirectExpiredRoles(LocalDate expirationDate, Pageable pageable);

    /**
     * Returns all direct roles with date lower than given expiration date. Automatic roles are included, sub roles not.
     *
     * @param expirationDate valid till < expirationDate
     * @return expired role identifiers (without sub roles)
     * @since 10.6.5, 10.7.2
     */
    List<UUID> findDirectExpiredRoleIds(LocalDate expirationDate);

    /**
     * Check if {@link IdmIdentityRoleDto} <b>ONE</b> is duplicit against {@link IdmIdentityRoleDto} <b>TWO</b>.</br></br>
     * Method check these states:</br>
     * - If {@link IdmIdentityRoleDto} has same {@link IdmRoleDto}</br>
     * - If {@link IdmIdentityRoleDto} has same {@link IdmIdentityContractDto}</br>
     * - If both roles are automatically added (in this case is return always false)</br>
     * - If role <b>ONE</b> is duplicity with validity to role <b>TWO</b>. When are both roles manually added is also check if
     * role <b>TWO</b> is duplicity with validity to role <b>ONE</b>
     * - If {@link IdmIdentityRoleDto} has same definition and values (this can be skipped by parameter @param <b>skipSubdefinition</b>)</br>
     * </br>
     * <b>Beware,</b> for check subdefinition is needed that given identity role has filled <b>_eavs</b> attribute with form instance. Form
     * definition with values is not get by database.
     *
     * @param one
     * @param two
     * @param skipSubdefinition
     * @return true if {@link IdmIdentityRoleDto} are same or similar. Otherwise false if {@link IdmIdentityRoleDto} are different
     * @since 9.5.0
     * @see <a href="https://wiki.czechidm.com/devel/documentation/roles/dev/identity-role-deduplication">Documentation link</a> for more information
     */
    D getDuplicated(D one, D two, Boolean skipSubdefinition);

    /**
     * Returns true, if given identity-role is automatic or business role.
     */
    boolean isRoleAutomaticOrComposition(D identityRole);

    Class<D> getType();

    Collection<D> findAllByIdentity(UUID id);

    void unassignAllSubRoles(UUID identityRoleId, EntityEvent<D> parentEvent);

    AbstractRoleAssignmentEvent<D> getEventForAssignment(D assignment, AbstractRoleAssignmentEvent.RoleAssignmentEventType update, String... flags);
    F getFilter();
}
