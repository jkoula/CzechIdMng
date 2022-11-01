package eu.bcvsolutions.idm.acc.wizard;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Wizard for personal accounts
 * @author Roman Kucera
 */
@Component(PersonalAccountWizard.NAME)
public class PersonalAccountWizard extends AbstractPersonalAccountWizard {

	public static final String NAME = "personal-account-wizard";

	public PersonalAccountWizard(IdmFormDefinitionService formDefinitionService, SysSystemAttributeMappingService systemAttributeMappingService,
									  AccAccountService accountService, SysSystemMappingService systemMappingService,
									  IdmIdentityService identityService, SysRoleSystemService roleSystemService, IdmIdentityContractService contractService,
									  ProvisioningService provisioningService, AccIdentityAccountService identityAccountService, IdmIdentityRoleService identityRoleService) {
		super(formDefinitionService, systemAttributeMappingService, accountService, systemMappingService, identityService,
				roleSystemService, contractService, provisioningService, identityAccountService, identityRoleService);
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports() {
		return true;
	}
}
