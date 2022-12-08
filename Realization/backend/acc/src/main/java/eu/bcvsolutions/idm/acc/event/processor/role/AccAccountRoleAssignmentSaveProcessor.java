package eu.bcvsolutions.idm.acc.event.processor.role;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentValidRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.event.processor.identity.AbstractRoleAssignmentSaveProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityRoleSaveProcessor;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component(AccAccountRoleAssignmentSaveProcessor.NAME)
@Description("Persists account role.")
public class AccAccountRoleAssignmentSaveProcessor extends AbstractRoleAssignmentSaveProcessor<AccAccountRoleAssignmentDto> {

    public static final String NAME = "account-role-assignment-save-processor";

    private final AccAccountRoleAssignmentValidRequestService validRequestService;


    public AccAccountRoleAssignmentSaveProcessor(AccAccountRoleAssignmentService service, AccAccountRoleAssignmentValidRequestService validRequestService) {
        super(service, AbstractRoleAssignmentEvent.RoleAssignmentEventType.CREATE,AbstractRoleAssignmentEvent.RoleAssignmentEventType.UPDATE);
        Assert.notNull(validRequestService, "Service is required.");
        this.validRequestService = validRequestService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    protected void createRoleValidReqForInvalidRole(AccAccountRoleAssignmentDto roleAssignment) {
        validRequestService.createByAccountRoleId(roleAssignment.getId());
    }
}
