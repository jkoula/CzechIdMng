package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component(AccountRoleDeleteProvisioningProcessor.PROCESSOR_NAME)
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after account role is deleted.")
public class AccountRoleDeleteProvisioningProcessor extends AbstractRoleAssignmentDeleteProvisioningProcessor<IdmIdentityDto> {
    public static final String PROCESSOR_NAME = "account-role-delete-provisioning-processor";

    private final AccIdentityAccountService identityAccountService;

    protected AccountRoleDeleteProvisioningProcessor(ProvisioningService provisioningService, SysRoleSystemService roleSystemService, AccAccountService accountService,
            SysSystemGroupSystemService systemGroupSystemService, EntityEventManager entityEventManager, AccIdentityAccountService identityAccountService) {
        super(provisioningService, roleSystemService, accountService, systemGroupSystemService, entityEventManager);
        this.identityAccountService = identityAccountService;
    }

    @Override
    protected IdmIdentityDto getOwner(AbstractRoleAssignmentDto roleAssignment) {
        AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
        filter.setAccountId(roleAssignment.getEntity());
        filter.setOwnership(Boolean.TRUE);
        filter.setIdentityRoleId(roleAssignment.getId());
        //
        return identityAccountService.find(filter, null).stream()
                .map(acc -> DtoUtils.getEmbedded(acc, AccIdentityAccount_.identity, IdmIdentityDto.class))
                .findFirst().orElse(null);
    }

    @Override
    public String getName() {
        return PROCESSOR_NAME;
    }
}
