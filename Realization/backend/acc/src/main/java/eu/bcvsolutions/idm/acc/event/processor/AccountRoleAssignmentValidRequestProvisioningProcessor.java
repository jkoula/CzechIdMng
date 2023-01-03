package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccRoleAssignmentValidRequestDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Processor for catch {@link IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID} - start account management for newly valid identityRoles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Start provisioning for role valid request result operation type [IDENTITY_ROLE_VALID].")
public class AccountRoleAssignmentValidRequestProvisioningProcessor extends AbstractRoleAssignmentValidProcessor<AccAccountRoleAssignmentDto, AccRoleAssignmentValidRequestDto> {

	public static final String PROCESSOR_NAME = "account-role-assignment-valid-request-provisioning-processor";

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AccountRoleAssignmentValidRequestProvisioningProcessor.class);


	private final AccIdentityAccountService identityAccountService;

	@Autowired
	public AccountRoleAssignmentValidRequestProvisioningProcessor(
			ApplicationContext applicationContext,
			AccAccountRoleAssignmentService accAccountRoleAssignmentService,
			IdmIdentityContractService identityContractService, AccIdentityAccountService identityAccountService) {
		super(accAccountRoleAssignmentService, applicationContext);
		this.identityAccountService = identityAccountService;
		//
		Assert.notNull(identityContractService, "Service is required.");
		//
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 10;
	}


	@Override
	protected AbstractDto getDtoToProvision(AccAccountRoleAssignmentDto roleAssignment) {
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setAccountId(roleAssignment.getAccount());
		final List<AccIdentityAccountDto> accIdentityAccountDtos = identityAccountService.find(identityAccountFilter, null).getContent();
		if (accIdentityAccountDtos.size() > 1) {
			LOG.warn("Multiple owners of account {} found. None will be provisioned", roleAssignment.getAccount());
			return null;
		}

		if (accIdentityAccountDtos.size() == 1) {
			final AccIdentityAccountDto accIdentityAccountDto = accIdentityAccountDtos.get(0);
			return DtoUtils.getEmbedded(accIdentityAccountDto, AccIdentityAccount_.identity);
		} else {
			LOG.warn("No identities found for provisioning of an account {}", roleAssignment.getAccount());
			return null;
		}
	}
}
