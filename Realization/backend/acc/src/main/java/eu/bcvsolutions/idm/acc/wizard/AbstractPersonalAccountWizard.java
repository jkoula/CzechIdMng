package eu.bcvsolutions.idm.acc.wizard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
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
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Common logic for personal and personal other account
 * @author Roman Kucera
 */
public abstract class AbstractPersonalAccountWizard extends AbstractAccountWizard {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractPersonalAccountWizard.class);

	public static final String STEP_FIRST = "accountNew";
	public static final String STEP_LAST = "accountRecapitulation";

	private final IdmFormDefinitionService formDefinitionService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final AccAccountService accountService;
	private final SysSystemMappingService systemMappingService;
	private final IdmIdentityService identityService;
	private final SysRoleSystemService roleSystemService;
	private final IdmIdentityContractService contractService;
	private final ProvisioningService provisioningService;
	private final AccIdentityAccountService identityAccountService;
	private final IdmIdentityRoleService identityRoleService;

	@Override
	public AccountWizardDto execute(AccountWizardDto wizardDto) {
		Assert.notNull(wizardDto.getWizardStepName(), "Wizard step name have to be filled for execute a connector type.");
		if (STEP_LAST.equals(wizardDto.getWizardStepName())) {
			// save all after last step = recapitulation
			executeStepFinish(wizardDto);
		} else if (STEP_FIRST.equals(wizardDto.getWizardStepName())) {
			wizardDto = executeFirstStep(wizardDto);
		}
		return wizardDto;
	}

	public AbstractPersonalAccountWizard(IdmFormDefinitionService formDefinitionService, SysSystemAttributeMappingService systemAttributeMappingService,
									  AccAccountService accountService, SysSystemMappingService systemMappingService,
									  IdmIdentityService identityService, SysRoleSystemService roleSystemService, IdmIdentityContractService contractService,
									  ProvisioningService provisioningService, AccIdentityAccountService identityAccountService, IdmIdentityRoleService identityRoleService) {
		this.formDefinitionService = formDefinitionService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.accountService = accountService;
		this.systemMappingService = systemMappingService;
		this.identityService = identityService;
		this.roleSystemService = roleSystemService;
		this.contractService = contractService;
		this.provisioningService = provisioningService;
		this.identityAccountService = identityAccountService;
		this.identityRoleService = identityRoleService;
	}

	protected AccountWizardDto executeFirstStep(AccountWizardDto wizardDto) {
		Map<String, String> metadata = wizardDto.getMetadata();

		String systemId = metadata.getOrDefault("system", null);
		String systemMappingId = metadata.getOrDefault("systemMapping", null);

		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setType(AccAccount.class.getName());
		formDefinitionFilter.setText(systemId);
		List<UUID> formDefinitionIds = formDefinitionService.findIds(formDefinitionFilter, null).getContent();
		if (formDefinitionIds.isEmpty()) {
			LOG.info("No form def found, returning wizard without it");
			return wizardDto;
		}
		IdmFormDefinitionDto formDefinitionDto = formDefinitionService.get(formDefinitionIds.get(0));

		// return only attributes from mapping
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(UUID.fromString(systemMappingId));
		List<SysSystemAttributeMappingDto> attributeMappingDtos = systemAttributeMappingService.find(attributeMappingFilter, null).getContent();
		Map<String, SysSystemAttributeMappingDto> attributeMappingMap = new HashMap<>();

		// get attributes names from schema attribute, filter merge and authoritative merge
		List<String> names = attributeMappingDtos.stream()
				.filter(sysSystemAttributeMappingDto -> !(sysSystemAttributeMappingDto.getStrategyType() == AttributeMappingStrategyType.AUTHORITATIVE_MERGE ||
						sysSystemAttributeMappingDto.getStrategyType() == AttributeMappingStrategyType.MERGE))
				.map(sysSystemAttributeMappingDto -> {
					SysSchemaAttributeDto schemaAttr = DtoUtils.getEmbedded(sysSystemAttributeMappingDto, SysSystemAttributeMapping_.schemaAttribute, SysSchemaAttributeDto.class);
					// add value to Map so we don;t to iterate over this list again later
					attributeMappingMap.put(schemaAttr.getName(), sysSystemAttributeMappingDto);
					return schemaAttr.getName();
				}).collect(Collectors.toList());

		List<IdmFormAttributeDto> attributes = new ArrayList<>();
		formDefinitionDto.getFormAttributes().forEach(idmFormAttributeDto -> {
			if (names.contains(idmFormAttributeDto.getCode())) {
				attributes.add(idmFormAttributeDto);
			}
		});

		formDefinitionDto.setFormAttributes(attributes);
		wizardDto.setFormDefinition(formDefinitionDto);

		List<IdmFormValueDto> valuesDto = new ArrayList<>();
		attributes.forEach(idmFormAttributeDto -> {
			IdmFormValueDto value = new IdmFormValueDto();
			value.setPersistentType(idmFormAttributeDto.getPersistentType());
			// get value
			String ownerId = metadata.getOrDefault("owner", null);
			IdmIdentityDto ownerDto = identityService.get(ownerId);
			Serializable attributeValue = (Serializable) systemAttributeMappingService.getAttributeValue(null, ownerDto, attributeMappingMap.get(idmFormAttributeDto.getCode()));
			value.setValue(attributeValue);

			value.setFormAttribute(idmFormAttributeDto.getId());
			value.getEmbedded().put("formAttribute", idmFormAttributeDto);
			valuesDto.add(value);
		});
		wizardDto.setValues(valuesDto);

		return wizardDto;
	}

	protected void executeStepFinish(AccountWizardDto wizardDto) {
		AccAccountDto accountDto = new AccAccountDto();

		Map<String, String> metadata = wizardDto.getMetadata();

		String systemId = metadata.getOrDefault("system", null);
		String systemMappingId = metadata.getOrDefault("systemMapping", null);
		IdmFormDefinitionDto formDefinition = wizardDto.getFormDefinition();

		accountDto.setSystem(UUID.fromString(systemId));
		accountDto.setSystemMapping(UUID.fromString(systemMappingId));
		accountDto.setFormDefinition(formDefinition.getId());

		SysSystemMappingDto systemMappingDto = systemMappingService.get(systemMappingId);
		accountDto.setEntityType(systemMappingDto.getEntityType());

		IdmFormInstanceDto formInstanceDto = new IdmFormInstanceDto(accountDto, formDefinition, wizardDto.getValues());
		accountDto.setEavs(Lists.newArrayList(formInstanceDto));

		// Get identifier for account
		SysSystemAttributeMappingFilter systemAttributeMappingFilter = new SysSystemAttributeMappingFilter();
		systemAttributeMappingFilter.setSystemMappingId(systemMappingDto.getId());
		systemAttributeMappingFilter.setIsUid(true);
		List<SysSystemAttributeMappingDto> uidAttributes = systemAttributeMappingService.find(systemAttributeMappingFilter, null).getContent();
		if (uidAttributes.size() != 1) {
			LOG.info("UID attribute should only one");
			return;
		}

		SysSystemAttributeMappingDto uidAttribute = uidAttributes.get(0);
		SysSchemaAttributeDto schemaUidAttributeDto = DtoUtils.getEmbedded(uidAttribute, SysSystemAttributeMapping_.schemaAttribute, SysSchemaAttributeDto.class);

		IdmFormAttributeDto formAttributeDto = formDefinition.getFormAttributes().stream()
				.filter(idmFormAttributeDto -> idmFormAttributeDto.getCode().equals(schemaUidAttributeDto.getName()))
				.findFirst().orElse(null);

		if (formAttributeDto == null) {
			LOG.info("No uid attribute found");
			return;
		}

		Serializable uidValue = wizardDto.getValues().stream()
				.filter(idmFormValueDto -> idmFormValueDto.getFormAttribute().equals(formAttributeDto.getId()))
				.findFirst().map(idmFormValueDto -> idmFormValueDto.getValue(formAttributeDto.getPersistentType())).orElse(null);

		accountDto.setUid(String.valueOf(uidValue));
		accountDto = accountService.save(accountDto);

		// get owner, it's identity
		String ownerId = metadata.getOrDefault("owner", null);
		IdmIdentityDto ownerDto = identityService.get(ownerId);
		IdmIdentityContractDto primeValidContract = contractService.getPrimeValidContract(UUID.fromString(ownerId));

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
