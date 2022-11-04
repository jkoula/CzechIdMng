package eu.bcvsolutions.idm.core.model.event.processor;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class ConceptCancellingProcessor<O extends BaseDto,
        C extends AbstractConceptRoleRequestDto, F extends IdmBaseConceptRoleRequestFilter, A extends AbstractRoleAssignmentDto> extends CoreEventProcessor<O> {

    protected final IdmGeneralConceptRoleRequestService<A, C, F> conceptRequestService;
    protected final IdmRoleAssignmentService<A, ?> roleAssignmentService;
    protected final IdmRoleRequestService roleRequestService;

    protected ConceptCancellingProcessor(IdmGeneralConceptRoleRequestService<A, C, F> conceptRequestService, IdmRoleAssignmentService<A, ?> roleAssignmentService,
            IdmRoleRequestService roleRequestService, EventType... eventTypes) {
        super(eventTypes);
        //
        this.conceptRequestService = conceptRequestService;
        this.roleRequestService = roleRequestService;
        this.roleAssignmentService = roleAssignmentService;
    }

    protected void removeRelatedConcepts(UUID contractId) {
        F conceptRequestFilter = conceptRequestService.getFilter();
        conceptRequestFilter.setOwnerUuid(contractId);
        conceptRequestService.find(conceptRequestFilter, null).getContent().forEach(concept -> {
            String message = null;
            if (concept.getState().isTerminatedState()) {
                message = MessageFormat.format(
                        "[{0}] [{1}] (requested in concept [{2}]) was deleted (not from this role request)!",
                        concept.getOwnerType().getName(), contractId, concept.getId());
            } else {
                message = MessageFormat.format(
                        "Request change in concept [{0}], was not executed, because requested [{1}] [{2}] was deleted (not from this role request)!",
                        concept.getId(),concept.getOwnerType().getName(), contractId);
                // Cancel concept and WF
                concept = conceptRequestService.cancel(concept);
            }
            IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
            roleRequestService.addToLog(request, message);
            conceptRequestService.addToLog(concept, message);
        });
    }

    protected void removeRelatedAssignedRoles(EntityEvent<O> event, UUID applicant, UUID ownerUuid, boolean forceDelete) {
        List<AbstractConceptRoleRequestDto> concepts = new ArrayList<>();
        roleAssignmentService.findAllByOwnerId(ownerUuid).forEach(identityRole -> {
            // Sub roles are removed different way (processor on direct identity role),
            // but automatic roles has to be removed in the same request.
            if (identityRole.getDirectRole() == null) {
                final C conceptToRemoveIdentityRole = conceptRequestService.createConceptToRemoveIdentityRole(identityRole);
                //
                concepts.add(conceptToRemoveIdentityRole);
            }
        });
        if (forceDelete) { // ~ async with force
            IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
            roleRequest.setApplicant(applicant);
            roleRequest.setConceptRoles(concepts);
            //
            RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEvent.RoleRequestEventType.EXCECUTE, roleRequest);
            requestEvent.setPriority(PriorityType.HIGH);
            //
            roleRequestService.startConcepts(requestEvent, event);
        } else {
            // ~ sync
            roleRequestService.executeConceptsImmediate(applicant, concepts);
        }
    }

}
