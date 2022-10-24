package eu.bcvsolutions.idm.core.model.event.processor.identity;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentSaveProcessor<A extends AbstractRoleAssignmentDto> extends CoreEventProcessor<A> {

    private final IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> service;



    public AbstractRoleAssignmentSaveProcessor(IdmRoleAssignmentService<A, ? extends BaseRoleAssignmentFilter> service,
            AbstractRoleAssignmentEvent.RoleAssignmentEventType... eventTypes) {
        super(eventTypes);
        Assert.notNull(service, "Service is required.");
        this.service = service;
    }

    @Override
    public EventResult<A> process(EntityEvent<A> event) {
        A roleAssignment = event.getContent();
        roleAssignment = service.saveInternal(roleAssignment);
        // Validate form attributes
        validate(roleAssignment);

        event.setContent(roleAssignment);
        //
        // if identityRole isn't valid save request into validRequests
        if (!EntityUtils.isValid(roleAssignment)) {
            // create new IdmIdentityRoleValidRequest
            createRoleValidReqForInvalidRole(roleAssignment);
        }
        //
        return new DefaultEventResult<>(event, this);
    }

    public void validate(A roleAssignment) {
        List<InvalidFormAttributeDto> validationResults = service.validateFormAttributes(roleAssignment);
        if (validationResults != null && !validationResults.isEmpty()) {
            IdmRoleDto role = DtoUtils.getEmbedded(roleAssignment, AbstractRoleAssignment_.role, IdmRoleDto.class);
            //
            throw new ResultCodeException(CoreResultCode.IDENTITY_ROLE_UNVALID_ATTRIBUTE, Map.of( //
                    "identityRole", roleAssignment.getId(), //
                    "roleCode", role != null ? role.getCode() : "", "attributeCode", validationResults.get(0).getAttributeCode() //
            ) //
            ); //
        }
    }

    protected abstract void createRoleValidReqForInvalidRole(A roleAssignment);


}
