package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType;

/**
 * Processor for catch {@link IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID} - start account management for newly valid identityRoles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Start provisioning for role valid request result operation type [IDENTITY_ROLE_VALID].")
public class IdentityRoleValidRequestProvisioningProcessor extends AbstractRoleAssignmentValidProcessor<IdmIdentityRoleDto, IdmIdentityRoleValidRequestDto> {
	
	public static final String PROCESSOR_NAME = "identity-role-valid-request-provisioning-processor";


	private final IdmIdentityContractService identityContractService;
	
	@Autowired
	public IdentityRoleValidRequestProvisioningProcessor(
			ApplicationContext applicationContext,
			IdmIdentityRoleService identityRoleService,
			IdmIdentityContractService identityContractService) {
		super(identityRoleService, applicationContext);
		//
		Assert.notNull(identityContractService, "Service is required.");
		//
		this.identityContractService = identityContractService;
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
	protected AbstractDto getDtoToProvision(IdmIdentityRoleDto roleAssignment) {
		IdmIdentityContractDto identityContract = identityContractService.get(roleAssignment.getIdentityContract());
		if (identityContract != null) {
			return DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity);
		} else {
			return null;
		}
	}
}
