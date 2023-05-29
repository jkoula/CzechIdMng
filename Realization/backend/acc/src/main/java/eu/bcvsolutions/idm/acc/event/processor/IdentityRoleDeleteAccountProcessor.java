package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.impl.AbstractAccountManagementService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultAccAccountManagementService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * Identity-role delete integrity processor (resolve only identity-account, not identity-role-account)
 *
 * @author Radek Tomiška
 */
@Component(IdentityRoleDeleteAccountProcessor.PROCESSOR_NAME)
@Description("Executes delete of identity-account before identity-role is deleted. (resolve only identity-account, not identity-role-account)")
public class IdentityRoleDeleteAccountProcessor extends AbstractRoleAssignmentDeleteAccountProcessor<IdmIdentityDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-account-processor";

	private final DefaultAccAccountManagementService accountManagementService;

	public IdentityRoleDeleteAccountProcessor(DefaultAccAccountManagementService accountManagementService) {
		super();
		this.accountManagementService = accountManagementService;
	}

	@Override
	protected AbstractAccountManagementService<IdmIdentityDto, ? extends EntityAccountDto> getAccountManagementService() {
		return accountManagementService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}


}