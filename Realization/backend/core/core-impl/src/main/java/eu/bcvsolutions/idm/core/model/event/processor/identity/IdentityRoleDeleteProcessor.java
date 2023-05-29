package eu.bcvsolutions.idm.core.model.event.processor.identity;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Delete identity role.
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdentityRoleDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes identity role from repository.")
public class IdentityRoleDeleteProcessor 
		extends AbstractRoleAssignmentDeleteProcessor<IdmIdentityRoleDto>
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-delete-processor";

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	

}
