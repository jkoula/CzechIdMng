package eu.bcvsolutions.idm.core.model.service.impl.adapter;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleSystemFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest_;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import org.modelmapper.ModelMapper;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation.ADD;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class DefaultRequestRoleConceptAdapter<C extends AbstractConceptRoleRequestDto, A extends AbstractRoleAssignmentDto> implements DtoAdapter<C, IdmRequestIdentityRoleDto> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(DefaultRequestRoleConceptAdapter.class);

    private final IdmRoleAssignmentService<A, ?> roleAssignmentService;

    private final IdmGeneralConceptRoleRequestService<A, C, ?> conceptRoleRequestService;

    //private final IdmRoleRequestService roleRequestService;

    private final IdmRoleSystemService roleSystemService;

    private final IdmRequestIdentityRoleFilter originalFilter;

    private final WorkflowProcessInstanceService workflowProcessInstanceService;

    private final ModelMapper modelMapper;


    public DefaultRequestRoleConceptAdapter(IdmRoleAssignmentService<A, ?> roleAssignmentService, IdmGeneralConceptRoleRequestService<A, C, ?> conceptRoleRequestService,
             IdmRoleSystemService roleSystemService, IdmRequestIdentityRoleFilter originalFilter,
            WorkflowProcessInstanceService workflowProcessInstanceService, ModelMapper modelMapper) {
        this.roleAssignmentService = roleAssignmentService;
        this.conceptRoleRequestService = conceptRoleRequestService;
        //this.roleRequestService = roleRequestService;
        this.roleSystemService = roleSystemService;
        this.originalFilter = originalFilter;
        this.workflowProcessInstanceService = workflowProcessInstanceService;
        this.modelMapper = modelMapper;
    }


    @Override
    public Stream<IdmRequestIdentityRoleDto> transform(Stream<C> data) {
        if (data == null) {
            return Stream.<IdmRequestIdentityRoleDto>builder().build();
        }
        final UUID identityId = originalFilter.getIdentity();
        LOG.debug(MessageFormat.format("Start searching duplicates for identity [{1}].", identityId));
        Assert.notNull(identityId, "Identity identifier is required.");

        Collection<A> identityRoles = roleAssignmentService.findAllByIdentity(identityId);
        // Add to all identity roles form instance. For identity role can exist only
        // one form instance.
        identityRoles.forEach(identityRole -> {
            IdmFormInstanceDto formInstance = roleAssignmentService.getRoleAttributeValues(identityRole);
            if (formInstance != null) {
                identityRole.setEavs(Lists.newArrayList(formInstance));
            }
        });
        // Find potential duplicated concepts (only ADD and not in terminated state)
       /* List<AbstractConceptRoleRequestDto> conceptsForMarkDuplicates = data //
                .filter(concept -> ADD == concept.getOperation()) //
                .filter(concept -> !concept.getState().isTerminatedState()) //
                .collect(Collectors.toList()); *///
        //TODO solve circular dependency
        //roleRequestService.markDuplicates(conceptsForMarkDuplicates, new ArrayList<>(identityRoles));
        // End mark duplicates
        LOG.debug(MessageFormat.format("End searching duplicates for identity [{1}].", identityId));

        return data.map(this::conceptToRequestIdentityRole);
    }


    /**
     * Converts concept to the request-identity-roles.
     *
     * @param concept
     * @return
     */
    @SuppressWarnings("unchecked")
    private IdmRequestIdentityRoleDto conceptToRequestIdentityRole(C concept) {
        IdmRequestIdentityRoleDto result = modelMapper.map(concept, IdmRequestIdentityRoleDto.class);
        result.setAssignmentType(conceptRoleRequestService.getType());
        // load permission from related contract or role (OR)
        if (originalFilter != null // from find method only
                && ADD == concept.getOperation() // newly requested role only
                && !concept.getState().isTerminatedState()) { // not terminated concepts
            // by related contract (backward compatible)
            final Set<String> transitivePermissions = conceptRoleRequestService.getTransitivePermissions(concept);
            final Set<String> permissions = Optional.ofNullable(result.getPermissions()).orElse(Collections.emptySet());
            result.setPermissions(Sets.union(transitivePermissions, permissions));
        }

        if (originalFilter != null && originalFilter.isIncludeEav()) {
            IdmFormInstanceDto formInstanceDto;
            if (ConceptRoleRequestOperation.REMOVE == concept.getOperation()) {
                A assignment = conceptRoleRequestService.getEmbeddedAssignment(concept);
                if (assignment == null) {
                    // Identity-role was not found, remove concept was executed (identity-role was removed).
                    return addCandidates(result, concept);
                }
                formInstanceDto  = roleAssignmentService.getRoleAttributeValues(assignment);
            } else {
                // Check on change of values is made only on ended request! 'Original' value is current value and in audit it was confusing (only 'new' value is show now).
                formInstanceDto = conceptRoleRequestService.getRoleAttributeValues(concept, !concept.getState().isTerminatedState());
            }
            addEav(concept, result, formInstanceDto);
        }
        // Include info if is role in cross-domain group.
        if (originalFilter != null && originalFilter.isIncludeCrossDomainsSystemsCount()) {
            if (ConceptRoleRequestOperation.REMOVE != concept.getOperation()) {
                IdmRoleDto roleDto = DtoUtils.getEmbedded(concept, AbstractConceptRoleRequest_.role.getName(), IdmRoleDto.class, null);
                if (roleDto != null && this.roleSystemService instanceof AbstractReadDtoService) {
                    AbstractReadDtoService<?, ?, IdmRoleSystemFilter> roleSystemService = (AbstractReadDtoService<?, ?, IdmRoleSystemFilter>) this.roleSystemService;
                    BaseFilter roleSystemFilter = roleSystemService.createFilterInstance();
                    if (roleSystemFilter instanceof IdmRoleSystemFilter) {
                        IdmRoleSystemFilter idmRoleSystemFilter = (IdmRoleSystemFilter) roleSystemFilter;
                        idmRoleSystemFilter.setIsInCrossDomainGroupRoleId(roleDto.getId());
                        long count = roleSystemService.count(idmRoleSystemFilter);
                        roleDto.setSystemsInCrossDomains(count);
                    }
                }
            }

        }

        return addCandidates(result, concept);
    }

    /**
     * Add candidates to given {@link IdmRequestIdentityRoleDto}. Candidates will be added only if filter has includesCandidates = true
     *
     * @param requestIdentityRoleDto
     * @param concept
     * @return
     */
    private IdmRequestIdentityRoleDto addCandidates(IdmRequestIdentityRoleDto requestIdentityRoleDto, C concept) {
        if (originalFilter != null && originalFilter.isIncludeCandidates() && concept.getWfProcessId() != null) {
            // Concept has own process (subprocess), method getApproversForProcess also include approvers for subprocess
            requestIdentityRoleDto.setCandidates(workflowProcessInstanceService.getApproversForProcess(concept.getWfProcessId()));
        }

        return requestIdentityRoleDto;
    }

    /**
     * Adds given EAVs attributes to the request-identity-role
     *
     * @param concept
     * @param result
     * @param formInstanceDto
     */
    private void addEav(C concept, IdmRequestIdentityRoleDto result, IdmFormInstanceDto formInstanceDto) {
        if (formInstanceDto != null) {
            result.getEavs().clear();
            result.getEavs().add(formInstanceDto);
            // If concept is in the terminated state, then we need to skip validation (because for example, for unique can be false positive).
            if (result.getState() != null && result.getState().isTerminatedState()){
                return;
            }
            // Validate the concept
            List<InvalidFormAttributeDto> validationResults = conceptRoleRequestService.validateFormAttributes(concept);
            formInstanceDto.setValidationErrors(validationResults);
            if (validationResults != null && !validationResults.isEmpty()) {
                // Concept is not valid (no other metadata for validation problem is not
                // necessary now).
                result.setValid(false);
            }
        }
    }

}
