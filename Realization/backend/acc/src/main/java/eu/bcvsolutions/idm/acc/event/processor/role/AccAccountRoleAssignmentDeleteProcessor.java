package eu.bcvsolutions.idm.acc.event.processor.role;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.AbstractRoleAssignmentDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityRoleDeleteProcessor;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component(AccAccountRoleAssignmentDeleteProcessor.NAME)
@Description("Deletes identity role from repository.")
public class AccAccountRoleAssignmentDeleteProcessor extends AbstractRoleAssignmentDeleteProcessor<AccAccountRoleAssignmentDto>  {

    public static final String NAME = "acc-account-role-assignment-delete-processor";

    @Override
    public String getName() {
        return NAME;
    }
}
