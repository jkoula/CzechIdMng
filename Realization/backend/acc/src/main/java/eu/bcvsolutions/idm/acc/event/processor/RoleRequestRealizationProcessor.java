package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.impl.AbstractAccountManagementService;
import eu.bcvsolutions.idm.acc.service.impl.DefaultAccAccountManagementService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;

import java.util.HashMap;
import java.util.Map;

/**
 * Realization of request in ACC module - ensure account management and
 * provisioning.
 * 
 * @author Vít Švanda
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@Component("accRoleRequestRealizationProcessor")
@Description("Realization of request in ACC module - ensure account management and provisioning.")
public class RoleRequestRealizationProcessor extends AbstractRoleRequestRealizationProcessor<IdmIdentityDto> {

	public static final String PROCESSOR_NAME = "acc-role-request-realization-processor";

	private final IdmIdentityService identityService;
	private final DefaultAccAccountManagementService accountManagementService;

	@Autowired
	public RoleRequestRealizationProcessor(IdmIdentityService identityService, DefaultAccAccountManagementService accountManagementService, ProvisioningService provisioningService,
			AccAccountService accountService) {
		super(provisioningService, accountService);
		this.identityService = identityService;
		this.accountManagementService = accountManagementService;
	}

	@Override
	protected AbstractAccountManagementService<IdmIdentityDto,? extends EntityAccountDto> getAccountManagementService() {
		return accountManagementService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}


	@Override
	protected IdmIdentityDto getOwner(IdmRoleRequestDto request) {
		return identityService.get(request.getApplicant());
	}

	@Override
	protected void initContext(IdmIdentityDto owner, IdmRoleRequestDto request) {
		Map<String, Object> context = owner.getContext();
		if (context == null) {
			context = new HashMap<>();
		}
		context.put(IdmRoleRequestService.ROLE_REQUEST_ID_KEY, request.getId());
		owner.setContext(context);
	}

	@Override
	protected Class<IdmIdentityDto> getOwnerClass() {
		return IdmIdentityDto.class;
	}
}
