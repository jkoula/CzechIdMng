package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Identity provisioning after role has been deleted.
 *
 * @author Jan Helbich
 * @author Radek Tomiška
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component(IdentityRoleDeleteProvisioningProcessor.PROCESSOR_NAME)
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after identity role is deleted.")
public class IdentityRoleDeleteProvisioningProcessor extends AbstractRoleAssignmentDeleteProvisioningProcessor<IdmIdentityDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-provisioning-processor";

	private final IdmIdentityContractService identityContractService;

	public IdentityRoleDeleteProvisioningProcessor(IdmIdentityContractService identityContractService, ProvisioningService provisioningService, SysRoleSystemService roleSystemService,
			AccAccountService accountService, SysSystemGroupSystemService systemGroupSystemService, EntityEventManager entityEventManager) {
		super(provisioningService, roleSystemService, accountService,  systemGroupSystemService,  entityEventManager);
		this.identityContractService = identityContractService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}


	@Override
	protected IdmIdentityDto getOwner(AbstractRoleAssignmentDto identityRole) {
		// TODO: Optimalization - load identity by identity-role with filter
		IdmIdentityContractDto identityContract = identityContractService.get(identityRole.getEntity());
		return Objects.nonNull(identityContract) ? DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity) : null;
	}
}
