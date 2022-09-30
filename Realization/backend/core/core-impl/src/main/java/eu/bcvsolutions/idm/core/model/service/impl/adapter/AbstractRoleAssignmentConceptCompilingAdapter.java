package eu.bcvsolutions.idm.core.model.service.impl.adapter;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.ADD;
import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.UPDATE;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentConceptCompilingAdapter<D extends AbstractRoleAssignmentDto, C extends AbstractConceptRoleRequestDto, F extends IdmBaseConceptRoleRequestFilter> implements DtoAdapter<D, IdmRequestIdentityRoleDto> {

    protected final IdmRequestIdentityRoleFilter originalFilter;
    protected final BasePermission permission;
    protected final IdmGeneralConceptRoleRequestService<D, C, F> conceptService;

    protected AbstractRoleAssignmentConceptCompilingAdapter(IdmRequestIdentityRoleFilter originalFilter, BasePermission permission,
            IdmGeneralConceptRoleRequestService<D, C, F> conceptRoleRequestService) {
        this.originalFilter = originalFilter;
        this.permission = permission;
        this.conceptService = conceptRoleRequestService;
    }

    @Override
    public Stream<IdmRequestIdentityRoleDto> transform(Stream<D> input) {
        final List<D> inputRoles = input.collect(Collectors.toList());
        final List<IdmRequestIdentityRoleDto> idmRequestIdentityRoleDtos = identityRolesToRequestIdentityRoles(inputRoles, originalFilter);
        if (originalFilter.getRoleRequestId() != null && !idmRequestIdentityRoleDtos.isEmpty()) {
            compileIdentityRolesWithConcepts(idmRequestIdentityRoleDtos, inputRoles, originalFilter, permission);
        }
        return idmRequestIdentityRoleDtos.stream();
    }

    /**
     * Convert request-identity-role-filter to identity-role-filter.
     * //TODO rely on filter translation or implement somthing to allow filter modification from outside?
     * @param filter
     * @return
     */
    private IdmIdentityRoleFilter toIdentityRoleFilter(IdmRequestIdentityRoleFilter filter) {
        IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();

        identityRoleFilter.setIdentityContractId(filter.getIdentityContractId());
        identityRoleFilter.setIdentityId(filter.getIdentityId());
        identityRoleFilter.setRoleId(filter.getRoleId());
        identityRoleFilter.setRoleText(filter.getRoleText());
        identityRoleFilter.setRoleEnvironments(filter.getRoleEnvironments());
        identityRoleFilter.setAddPermissions(true); // permissions are required

        return identityRoleFilter;
    }

    /**
     * Find concepts for given identity-roles. If some exists (in given request),
     * then will be altered for concept metadata (operation, EAVs)
     *
     * @param requestIdentityRoles
     * @param identityRoles
     * @param filter
     * @param permission
     */
    private void compileIdentityRolesWithConcepts(List<IdmRequestIdentityRoleDto> requestIdentityRoles,
            List<D> identityRoles, IdmRequestIdentityRoleFilter filter, BasePermission... permission) {
        // Convert identity-roles to Set of IDs.
        Set<UUID> identityRoleIds = identityRoles.stream().map(AbstractRoleAssignmentDto::getId)
                .collect(Collectors.toSet());
        // Find concepts by identity-roles IDs.
        F conceptFilter = conceptService.getFilter();
        conceptFilter.setRoleAssignmentUuids(identityRoleIds);
        conceptFilter.setRoleRequestId(filter.getRoleRequestId());

        List<C> conceptsForThisPage = conceptService.find(conceptFilter, null, permission).getContent();
        conceptsForThisPage.stream() //
                .filter(concept -> ADD != concept.getOperation()) //
                .forEach(concept -> { //

                    IdmRequestIdentityRoleDto requestIdentityRoleWithConcept= requestIdentityRoles.stream() //
                            .filter(idmRequestIdentityRoleDto -> getIdmRequestIdentityRoleDtoPredicate(concept, idmRequestIdentityRoleDto))
                            .findFirst() //
                            .orElse(null); //
                    if (requestIdentityRoleWithConcept != null) {
                        requestIdentityRoleWithConcept.setOperation(concept.getOperation());
                        requestIdentityRoleWithConcept.setId(concept.getId());
                        requestIdentityRoleWithConcept.setValidFrom(concept.getValidFrom());
                        requestIdentityRoleWithConcept.setValidTill(concept.getValidTill());
                        requestIdentityRoleWithConcept.setRoleRequest(concept.getRoleRequest());
                        IdmFormInstanceDto formInstanceDto;
                        // For updated identity-role replace EAVs from the concept
                        if (UPDATE == concept.getOperation()) {
                            // Check on change of values is made only on ended request! 'Original' value is current value and in audit it was confusing (only 'new' value is show now).
                            formInstanceDto = conceptService.getRoleAttributeValues(concept, !concept.getState().isTerminatedState());
                            addEav(concept,requestIdentityRoleWithConcept, formInstanceDto);
                        }
                    }
                });
    }

    private boolean getIdmRequestIdentityRoleDtoPredicate(C concept, IdmRequestIdentityRoleDto idmRequestIdentityRoleDto) {
        return idmRequestIdentityRoleDto.getRoleAssignmentUuid() != null &&
                idmRequestIdentityRoleDto.getRoleAssignmentUuid().equals(concept.getRoleAssignmentUuid()) &&
                idmRequestIdentityRoleDto.getId().equals(concept.getRoleAssignmentUuid());
    }


    /**
     * Converts identity-roles to request-identity-roles
     *
     * @param identityRoles
     * @param filter
     * @return
     */
    private List<IdmRequestIdentityRoleDto> identityRolesToRequestIdentityRoles(List<D> identityRoles, IdmRequestIdentityRoleFilter filter) {
        List<IdmRequestIdentityRoleDto> concepts = Lists.newArrayList();

        if (identityRoles  == null) {
            return concepts;
        }

        identityRoles.forEach(identityRole -> {
            IdmRequestIdentityRoleDto request = roleAssignmentToReqIdentityRole(identityRole);
            request.setId(identityRole.getId());
            request.setRole(identityRole.getRole());
            request.setRoleAssignmentUuid(identityRole.getId());
            request.setDirectRole(identityRole.getDirectRole());
            request.setRoleSystem(identityRole.getRoleSystem());
            request.setRoleComposition(identityRole.getRoleComposition());
            request.setValidFrom(identityRole.getValidFrom());
            request.setValidTill(identityRole.getValidTill());
            request.setAutomaticRole(identityRole.getAutomaticRole());
            request.setTrimmed(true);
            request.getEmbedded().put(AbstractRoleAssignment_.role.getName(),
                    identityRole.getEmbedded().get(AbstractRoleAssignment_.role.getName()));
            request.getEmbedded().put(IdmIdentityRole_.identityContract.getName(),
                    identityRole.getEmbedded().get(IdmIdentityRole_.identityContract.getName()));
            request.setPermissions(identityRole.getPermissions());

            if (filter.isIncludeEav()) {
                IdmFormInstanceDto formInstanceDto  = getFormInstance(identityRole);
                addEav(identityRole, request, formInstanceDto);
            }

            concepts.add(request);
        });

        return concepts;
    }

    protected abstract IdmFormInstanceDto getFormInstance(D identityRole);

    protected abstract IdmRequestIdentityRoleDto roleAssignmentToReqIdentityRole(D identityRole);

    /**
     * Adds given EAVs attributes to the request-identity-role
     *
     * @param identityRole
     * @param result
     * @param formInstanceDto
     */
    private void addEav(D identityRole, IdmRequestIdentityRoleDto result, IdmFormInstanceDto formInstanceDto) {
        if (formInstanceDto != null) {
            result.getEavs().clear();
            result.getEavs().add(formInstanceDto);
            // If concept is in the terminated state, then we need to skip validation (because for example, for unique can be false positive).
            if (result.getState() != null && result.getState().isTerminatedState()){
                return;
            }
            // Validate the concept
            List<InvalidFormAttributeDto> validationResults = validateFormAttributes(identityRole);//identityRoleService.validateFormAttributes(identityRole);
            formInstanceDto.setValidationErrors(validationResults);
            if (validationResults != null && !validationResults.isEmpty()) {
                // Concept is not valid (no other metadata for validation problem is not
                // necessary now).
                result.setValid(false);
            }
        }
    }

    protected abstract List<InvalidFormAttributeDto> validateFormAttributes(D identityRole);

    private void addEav(C concept, IdmRequestIdentityRoleDto requestIdentityRoleWithConcept, IdmFormInstanceDto formInstanceDto) {
        if (formInstanceDto != null) {
            requestIdentityRoleWithConcept.getEavs().clear();
            requestIdentityRoleWithConcept.getEavs().add(formInstanceDto);
            // If concept is in the terminated state, then we need to skip validation (because for example, for unique can be false positive).
            if (requestIdentityRoleWithConcept.getState() != null && requestIdentityRoleWithConcept.getState().isTerminatedState()){
                return;
            }
            // Validate the concept
            List<InvalidFormAttributeDto> validationResults = conceptService.validateFormAttributes(concept);
            formInstanceDto.setValidationErrors(validationResults);
            if (validationResults != null && !validationResults.isEmpty()) {
                // Concept is not valid (no other metadata for validation problem is not
                // necessary now).
                requestIdentityRoleWithConcept.setValid(false);
            }
        }
    }

}
