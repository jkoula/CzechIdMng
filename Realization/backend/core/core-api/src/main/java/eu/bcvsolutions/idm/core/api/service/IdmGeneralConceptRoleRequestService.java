package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.adapter.AdaptableService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for concept role request
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface IdmGeneralConceptRoleRequestService<
        A extends AbstractRoleAssignmentDto,
        D extends AbstractConceptRoleRequestDto,
        F extends IdmBaseConceptRoleRequestFilter> extends ReadWriteDtoService<D, F>, AuthorizableService<D>, AdaptableService<D, F, IdmRequestIdentityRoleDto> {

    void addToLog(Loggable logItem, String text);

    abstract List<D> findAllByRoleRequest(UUID requestId);

    /**
     * Set concept state to CANCELED and stop workflow process (connected to this
     * concept)
     *
     * @param dto
     */
    D cancel(D dto);

    /**
     * Return form instance for given concept. Values contains changes evaluated
     * against the identity-role form values.
     *
     * @param dto
     * @param checkChanges If true, then changes against the identity role will be evaluated.
     * @return
     */
    IdmFormInstanceDto getRoleAttributeValues(D dto, boolean checkChanges);

    /**
     * Validate form attributes for given concept
     *
     * @param concept
     * @return
     */
    List<InvalidFormAttributeDto> validateFormAttributes(D concept);

    //TODO javadoc
    IdmRoleDto determineRoleFromConcept(D concept);

    boolean validOwnership(D concept, UUID applicantId);

    List<D> findAllByRoleRequest(UUID requestId, Pageable pa, IdmBasePermission... permissions);

    CoreEvent<A> removeRelatedRoleAssignment(D concept, EntityEvent<IdmRoleRequestDto> requestEvent);

    void updateAssignedRole(List<D> allApprovedConcepts, D concept, EntityEvent<IdmRoleRequestDto> requestEvent);

    void createAssignedRole(List<D> allApprovedConcepts, D concept, EntityEvent<IdmRoleRequestDto> requestEvent);
    void removeAssignedRole(D concept, EntityEvent<IdmRoleRequestDto> requestEvent);

    Collection<D> getConceptsToRemoveDuplicates(AbstractRoleAssignmentDto tempIdentityRoleSub, List<AbstractRoleAssignmentDto> allByIdentity);

    <E extends  AbstractConceptRoleRequestDto> E createConceptToRemoveIdentityRole(E concept, A identityRoleAssignment);

    boolean cancelInvalidConcept(List<A> automaticRoles, D concept, IdmRoleRequestDto request);

    A createAssignmentFromConcept(D concept);

    A getRoleAssignmentDto(D concept, IdmRoleDto subRole);

    Class<D> getType();

    F getFilter();

    IdmRequestIdentityRoleDto saveRequestRole(IdmRequestIdentityRoleDto dto, BasePermission[] permission);

    IdmRequestIdentityRoleDto deleteRequestRole(IdmRequestIdentityRoleDto dto, BasePermission[] permission);

    Set<String> getTransitivePermissions(D concept);

    A getEmbeddedAssignment(D concept);
}
