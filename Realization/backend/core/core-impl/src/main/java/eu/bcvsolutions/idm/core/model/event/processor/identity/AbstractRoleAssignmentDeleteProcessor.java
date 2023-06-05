package eu.bcvsolutions.idm.core.model.event.processor.identity;

import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class AbstractRoleAssignmentDeleteProcessor<A extends AbstractRoleAssignmentDto> extends CoreEventProcessor<A> {

//TODO inject concrete services in subclasses ion order to maike it more obvious, which service is being used
    @Autowired
    private IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> service;
    @Autowired private IdmIdentityRoleValidRequestService identityRoleValidRequestService;
    @Autowired private IdmConceptRoleRequestManager conceptRoleRequestManager;

    public AbstractRoleAssignmentDeleteProcessor() {
        super(AbstractRoleAssignmentEvent.RoleAssignmentEventType.DELETE);
    }

    @Override
    public EventResult<A> process(EntityEvent<A> event) {
        A identityRole = event.getContent();
        UUID identityRoleId = identityRole.getId();
        Assert.notNull(identityRoleId, "Content identifier is required.");

        // Find all concepts and remove relation on identity role
        Collection<AbstractConceptRoleRequestDto> allConcepts = conceptRoleRequestManager.findAllByRoleAssignment(identityRoleId);
        allConcepts.forEach(concept -> {
            String message = null;
            if (concept.getState().isTerminatedState()) {
                message = MessageFormat.format(
                        "IdentityRole [{0}] (reqested in concept [{1}]) was deleted (not from this role request)!",
                        identityRoleId, concept.getId());
            } else {
                message = MessageFormat.format(
                        "Request change in concept [{0}], was not executed, because requested IdentityRole [{1}] was deleted (not from this role request)!",
                        concept.getId(), identityRoleId);
                concept = conceptRoleRequestManager.getServiceForConcept(concept).cancel(concept);
            }
            conceptRoleRequestManager.getServiceForConcept(concept).addToLog(concept, message);
            concept.setRoleAssignmentUuid(null);
            conceptRoleRequestManager.getServiceForConcept(concept).save(concept);
        });
        //
        // remove all IdentityRoleValidRequest for this role
        List<IdmIdentityRoleValidRequestDto> validRequests = identityRoleValidRequestService.findAllValidRequestForIdentityRoleId(identityRoleId);
        identityRoleValidRequestService.deleteAll(validRequests);
        //
        // remove sub roles - just for sure, if role is not removed by role request
        service.unassignAllSubRoles(identityRoleId, event);

        //
        // Delete identity role
        service.deleteInternal(identityRole);
        //
        return new DefaultEventResult<>(event, this);
    }

}
