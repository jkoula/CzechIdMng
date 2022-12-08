package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;

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
