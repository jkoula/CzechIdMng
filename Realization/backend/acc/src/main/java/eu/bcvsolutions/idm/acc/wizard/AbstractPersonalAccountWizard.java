package eu.bcvsolutions.idm.acc.wizard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Common logic for personal and personal other account
 * @author Roman Kucera
 */
public abstract class AbstractPersonalAccountWizard extends AbstractAccountWizard {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractPersonalAccountWizard.class);


	private final AccAccountService accountService;
	private final IdmIdentityService identityService;
	private final SysRoleSystemService roleSystemService;
	private final IdmIdentityContractService contractService;
	private final ProvisioningService provisioningService;
	private final AccIdentityAccountService identityAccountService;
	private final IdmIdentityRoleService identityRoleService;

	public AbstractPersonalAccountWizard(IdmFormDefinitionService formDefinitionService, SysSystemAttributeMappingService systemAttributeMappingService,
									  AccAccountService accountService, SysSystemMappingService systemMappingService,
									  IdmIdentityService identityService, SysRoleSystemService roleSystemService, IdmIdentityContractService contractService,
									  ProvisioningService provisioningService, AccIdentityAccountService identityAccountService, IdmIdentityRoleService identityRoleService) {
		super(systemMappingService, systemAttributeMappingService, formDefinitionService);
		this.accountService = accountService;
		this.identityService = identityService;
		this.roleSystemService = roleSystemService;
		this.contractService = contractService;
		this.provisioningService = provisioningService;
		this.identityAccountService = identityAccountService;
		this.identityRoleService = identityRoleService;
	}

	@Override
	protected AccountWizardDto executeFirstStep(AccountWizardDto wizardDto) {
		Map<String, String> metadata = wizardDto.getMetadata();
		IdmFormDefinitionDto formDefinitionDto = getFormDefinition(metadata);

		Map<String, SysSystemAttributeMappingDto> attributeMappingMap = new HashMap<>();
		List<String> names = getNames(metadata, attributeMappingMap);

		List<IdmFormAttributeDto> attributes = getAttributes(formDefinitionDto, names);

		formDefinitionDto.setFormAttributes(attributes);
		wizardDto.setFormDefinition(formDefinitionDto);

		String ownerId = metadata.getOrDefault("owner", null);
		IdmIdentityDto ownerDto = identityService.get(ownerId);
		List<IdmFormValueDto> valuesDto = getValuesDto(attributeMappingMap, attributes, ownerDto);
		wizardDto.setValues(valuesDto);

		return wizardDto;
	}

	@Override
	protected void executeStepFinish(AccountWizardDto wizardDto) {
		Map<String, String> metadata = wizardDto.getMetadata();
		String systemMappingId = metadata.getOrDefault("systemMapping", null);

		AccAccountDto accountDto = prepareAccount(wizardDto, systemMappingId);

		// get owner, it's identity
		String ownerId = metadata.getOrDefault("owner", null);
		IdmIdentityDto ownerDto = identityService.get(ownerId);
		IdmIdentityContractDto primeValidContract = contractService.getPrimeValidContract(UUID.fromString(ownerId));

		String uidValue = getUidValue(wizardDto, ownerDto, UUID.fromString(systemMappingId));
		// save Acc
		accountDto.setUid(uidValue);
		accountDto = accountService.save(accountDto);

		// Do provisioning for acc and owner
		provisioningService.doProvisioning(accountDto, ownerDto);

		// Login role
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setSystemMappingId(UUID.fromString(systemMappingId));
		roleSystemFilter.setCreateAccountByDefault(true);
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		if (roleSystemDtos.isEmpty()) {
			LOG.info("No login role found");
			return;
		}
		SysRoleSystemDto roleSystemDto = roleSystemDtos.get(0);

		// assign role
		IdmIdentityRoleDto identityRoleDto = new IdmIdentityRoleDto();
		identityRoleDto.setRole(roleSystemDto.getRole());
		identityRoleDto.setIdentityContractDto(primeValidContract);
		identityRoleDto = identityRoleService.saveInternal(identityRoleDto);

		// link acc to user
		AccIdentityAccountDto identityAccountDto = new AccIdentityAccountDto();
		identityAccountDto.setIdentity(ownerDto.getId());
		identityAccountDto.setAccount(accountDto.getId());
		identityAccountDto.setOwnership(true);
		identityAccountDto.setRoleSystem(roleSystemDto.getId());
		identityAccountDto.setIdentityRole(identityRoleDto.getId());
		identityAccountDto.getEmbedded().put(AccIdentityAccount_.account.getName(), accountDto);
		identityAccountService.save(identityAccountDto);
	}
}
