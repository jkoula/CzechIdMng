package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Identity-role delete integrity processor (resolve only identity-account, not identity-role-account)
 *
 * @author Radek Tomiška
 */
@Component(IdentityRoleDeleteAccountProcessor.PROCESSOR_NAME)
@Description("Executes delete of identity-account before identity-role is deleted. (resolve only identity-account, not identity-role-account)")
public class IdentityRoleDeleteAccountProcessor extends AbstractEntityEventProcessor<AbstractRoleAssignmentDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-account-processor";
	//
	@Autowired private AccAccountManagementService accountManagementService;

	public IdentityRoleDeleteAccountProcessor() {
		super(AbstractRoleAssignmentEvent.RoleAssignmentEventType.DELETE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<AbstractRoleAssignmentDto> process(EntityEvent<AbstractRoleAssignmentDto> event) {
		accountManagementService.deleteIdentityAccount(event);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}

	@Override
	public int getOrder() {
		return -ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}