package eu.bcvsolutions.idm.acc.event.processor.role;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.event.processor.identity.AbstractRoleAssignmentSaveProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityRoleSaveProcessor;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component(AccAccountRoleAssignmentSaveProcessor.NAME)
@Description("Persists acount role.")
public class AccAccountRoleAssignmentSaveProcessor extends AbstractRoleAssignmentSaveProcessor<AccAccountRoleAssignmentDto> {

    public static final String NAME = "account-role-assignment-save-processor";

    public AccAccountRoleAssignmentSaveProcessor(AccAccountRoleAssignmentService service, IdmIdentityRoleValidRequestService validRequestService) {
        super(service, validRequestService, AbstractRoleAssignmentEvent.RoleAssignmentEventType.CREATE,AbstractRoleAssignmentEvent.RoleAssignmentEventType.UPDATE);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
