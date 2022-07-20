package eu.bcvsolutions.idm.acc.connector;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.ReconciliationMissingAccountActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationLinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationMissingEntityActionType;
import eu.bcvsolutions.idm.acc.domain.SynchronizationUnlinkedActionType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncRoleConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSyncConfigFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.event.processor.MsAdSyncMappingRoleAutoAttributesProcessor;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * AD wizard for groups.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Component(AdGroupConnectorType.NAME)
public class AdGroupConnectorType extends AdUserConnectorType {

	public static final String MEMBER_SYSTEM_MAPPING = "memberSystemMappingId";
	public static final String GROUP_SYNC_ID = "groupSyncId";
	public static final String GROUP_CONTAINER_KEY = "groupContainer";
	public static final String OBJECT_GUID_ATTRIBUTE = "objectGUID";
	private static final String UID_FOR_GROUP_ATTRIBUTE = "gidAttribute";
	public static final String MAIN_ROLE_CATALOG = "mainRoleCatalog";
	public static final String NEW_ROLE_CATALOG = "newRoleCatalog";
	public static final String BASE_CONTEXT_GROUP_KEY = "groupBaseContexts";

	// Default values
	protected static final String ENTRY_OBJECT_CLASS_GROUP = "group";
	protected static final String[] ENTRY_OBJECT_CLASSES_DEFAULT_VALUES = {"top", ENTRY_OBJECT_CLASS_GROUP};
	private static final int PAGE_SIZE_DEFAULT_VALUE = 100;
	private static final String CN_VALUE = "cn";
	private static final String GROUP_SYNC_NAME = "Group sync";
	public static final char LINE_SEPARATOR = '\n'; //System independent line separator.

	// Connector type ID.
	public static final String NAME = "ad-group-connector-type";

	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;

	@Override
	public String getIconKey() {
		return "ad-group-connector-icon";
	}

	@Override
	protected String getSchemaType() {
		return IcObjectClassInfo.GROUP;
	}

	@Override
	public int getOrder() {
		return 190;
	}

	@Override
	public Map<String, String> getMetadata() {
		// Default values:
		Map<String, String> metadata = super.getMetadata();
		metadata.put(SYSTEM_NAME, this.findUniqueSystemName("MS AD - Groups", 1));
		metadata.put(PORT, "636");
		metadata.put(PAIRING_SYNC_DN_ATTR_KEY, DN_ATTR_CODE);
		metadata.put(PROTECTED_MODE_SWITCH_KEY, "false");
		return metadata;
	}

	@Override
	public ConnectorTypeDto load(ConnectorTypeDto connectorType) {
		super.load(connectorType);
		if (!connectorType.isReopened()) {
			return connectorType;
		}
		
		// Load the system.
		SysSystemDto systemDto = (SysSystemDto) connectorType.getEmbedded().get(SYSTEM_DTO_KEY);
		Assert.notNull(systemDto, "System must exists!");
		connectorType.getMetadata().put(SYSTEM_NAME, systemDto.getName());
		Map<String, String> metadata = connectorType.getMetadata();

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		// Find attribute with port.
		metadata.put(PORT, getValueFromConnectorInstance(PORT, systemDto, connectorFormDef));
		// Find attribute with host.
		metadata.put(HOST, getValueFromConnectorInstance(HOST, systemDto, connectorFormDef));
		// Find attribute with user.
		metadata.put(USER, getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef));
		// Find attribute with ssl switch.
		metadata.put(SSL_SWITCH, getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		// Find group container.
		List<String> containers = getValuesFromConnectorInstance(BASE_CONTEXT_GROUP_KEY, systemDto, connectorFormDef);
		metadata.put(GROUP_CONTAINER_KEY, containersToString(containers));

		// Load the sync mapping.
		SysSystemMappingFilter syncMappingFilter = new SysSystemMappingFilter();
		syncMappingFilter.setSystemId(systemDto.getId());
		syncMappingFilter.setOperationType(SystemOperationType.SYNCHRONIZATION);
		SysSystemMappingDto syncMappingDto = getSystemMappingService().find(syncMappingFilter, null)
				.getContent()
				.stream().min(Comparator.comparing(SysSystemMappingDto::getCreated))
				.orElse(null);
		if (syncMappingDto != null) {
			connectorType.getMetadata().put(MAPPING_SYNC_ID, syncMappingDto.getId().toString());
			// Load the sync.
			SysSyncConfigFilter syncFilter = new SysSyncConfigFilter();
			syncFilter.setSystemId(systemDto.getId());
			syncFilter.setSystemMappingId(syncMappingDto.getId());

			AbstractSysSyncConfigDto syncDto = getSyncConfigService().find(syncFilter, null)
					.getContent()
					.stream().min(Comparator.comparing(AbstractDto::getCreated))
					.orElse(null);
			if (syncDto != null) {
				connectorType.getMetadata().put(GROUP_SYNC_ID, syncDto.getId().toString());
				if (syncDto instanceof SysSyncRoleConfigDto) {
					SysSyncRoleConfigDto roleConfigDto = (SysSyncRoleConfigDto) syncDto;
					if (roleConfigDto.getMemberSystemMapping() != null) {
						connectorType.getMetadata().put(MEMBER_SYSTEM_MAPPING, roleConfigDto.getMemberSystemMapping().toString());
					}
					// Load setting of group sync.
					connectorType.getMetadata().put(SysSyncRoleConfig_.membershipSwitch.getName(), String.valueOf(roleConfigDto.isMembershipSwitch()));
					connectorType.getMetadata().put(SysSyncRoleConfig_.assignCatalogueSwitch.getName(), String.valueOf(roleConfigDto.isAssignCatalogueSwitch()));
					connectorType.getMetadata().put(SysSyncRoleConfig_.assignRoleSwitch.getName(), String.valueOf(roleConfigDto.isAssignRoleSwitch()));
					connectorType.getMetadata().put(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName(), String.valueOf(roleConfigDto.isAssignRoleRemoveSwitch()));
					connectorType.getMetadata().put(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName(), String.valueOf(roleConfigDto.isRemoveCatalogueRoleSwitch()));
					connectorType.getMetadata().put(MAIN_ROLE_CATALOG,
							roleConfigDto.getMainCatalogueRoleNode() != null ? roleConfigDto.getMainCatalogueRoleNode().toString() : null
					);
				}
			}
		}

		return connectorType;
	}

	@Override
	@Transactional
	public ConnectorTypeDto execute(ConnectorTypeDto connectorType) {
		try {
			//super.super.execute(connectorType);
			if (STEP_ONE.equals(connectorType.getWizardStepName())) {
				executeStepOne(connectorType);
			} else if (STEP_CREATE_USER_TEST.equals(connectorType.getWizardStepName())) {
				executeCreateUserTest(connectorType);
			} else if (STEP_DELETE_USER_TEST.equals(connectorType.getWizardStepName())) {
				executeDeleteUserTest(connectorType);
			} else if (STEP_ASSIGN_GROUP_TEST.equals(connectorType.getWizardStepName())) {
				executeAssignTestUserToGroup(connectorType);
			} else if (STEP_FOUR.equals(connectorType.getWizardStepName())) {
				executeStepFour(connectorType);
			} else {
				// Default loading of system DTO.
				String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
				Assert.notNull(systemId, "System ID cannot be null!");
				SysSystemDto systemDto = this.getSystemService().get(systemId);
				connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);
			}
		} catch (ResultCodeException ex) {
			if (ex.getCause() instanceof AuthenticationException) {
				throw new ResultCodeException(AccResultCode.WIZARD_AD_AUTHENTICATION_FAILED, ex.getCause());
			}
			if (ex.getCause() instanceof CommunicationException) {
				CommunicationException exCause = (CommunicationException) ex.getCause();
				if (exCause.getRootCause() instanceof UnknownHostException) {
					UnknownHostException rootCause = (UnknownHostException) exCause.getRootCause();
					throw new ResultCodeException(AccResultCode.WIZARD_AD_UNKNOWN_HOST,
							ImmutableMap.of("host", rootCause.getLocalizedMessage()), ex.getCause());
				}
			}
			throw ex;
		} catch (IllegalArgumentException ex) {
			throw new ResultCodeException(CoreResultCode.BAD_VALUE,
					ImmutableMap.of("value", ex.getLocalizedMessage()), ex);
		}
		return connectorType;
	}

	@Override
	public boolean supportsSystem(SysSystemDto systemDto) {
		if (!super.supportsSystemByConnector(systemDto)) {
			return false;
		}

		try {
			IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
			// Find attribute with object classes to sync.
			// If contains "group", then we predicate that this system is for Group.
			IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(OBJECT_CLASSES_TO_SYNC_KEY);
			if (attribute != null) {
				List<IdmFormValueDto> values = getFormService().getValues(systemDto, attribute, IdmBasePermission.READ);
				if (values != null) {
					return values.stream()
							.anyMatch(value -> AdGroupConnectorType.ENTRY_OBJECT_CLASS_GROUP.equals(value.getValue()));
				}
			}
		} catch (IllegalStateException ex) {
			// Connector was not found -> continue with next.
			return false;
		}
		return false;
	}

	/**
	 * Execute first step of AD wizard.
	 */
	protected void executeStepOne(ConnectorTypeDto connectorType) {
		String memberSystemMappingId = connectorType.getMetadata().get(MEMBER_SYSTEM_MAPPING);

		SysSystemMappingDto systemMappingDto = null;
		if (memberSystemMappingId != null) {
			systemMappingDto = getSystemMappingService().get(UUID.fromString(memberSystemMappingId), IdmBasePermission.READ);
		}
		if (systemMappingDto != null) {
			SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
			Assert.notNull(objectClassDto, "Schema DTO cannot be null!");
			SysSystemDto memberSystemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);
			Assert.notNull(memberSystemDto, "Member system DTO cannot be null!");

			ConnectorType memberConnectorType = getConnectorManager().findConnectorTypeBySystem(memberSystemDto);
			if (!(memberConnectorType instanceof AdUserConnectorType)) {
				throw new ResultCodeException(
						AccResultCode.WIZARD_AD_GROUP_WRONG_MEMBER_CONNECTOR_TYPE,
						ImmutableMap.of("connectorType", memberConnectorType == null ? "none" : memberConnectorType.toString()
						)
				);
			}

			ConnectorTypeDto adUserSystemMockConnectorType = new ConnectorTypeDto();
			adUserSystemMockConnectorType.setReopened(true);
			adUserSystemMockConnectorType.getEmbedded().put(SYSTEM_DTO_KEY, memberSystemDto);
			adUserSystemMockConnectorType.getMetadata().put(SYSTEM_DTO_KEY, memberSystemDto.getId().toString());
			adUserSystemMockConnectorType = super.load(adUserSystemMockConnectorType);
			Map<String, String> metadata = connectorType.getMetadata();
			// Find attribute with port.
			metadata.put(PORT, adUserSystemMockConnectorType.getMetadata().get(PORT));
			// Find attribute with host.
			metadata.put(HOST, adUserSystemMockConnectorType.getMetadata().get(HOST));
			// Find attribute with user.
			metadata.put(USER, adUserSystemMockConnectorType.getMetadata().get(USER));
			// Find attribute with ssl switch.
			metadata.put(SSL_SWITCH, adUserSystemMockConnectorType.getMetadata().get(SSL_SWITCH));
			// Load password.
			IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(memberSystemDto);
			metadata.put(PASSWORD, this.getConfidentialValueFromConnectorInstance(CREDENTIALS, memberSystemDto, connectorFormDef));

		}
		super.executeStepOne(connectorType);
		String mappingSyncId = connectorType.getMetadata().get(MAPPING_SYNC_ID);
		if (mappingSyncId == null) {
			// This attributes will be updated only if system doesn't have mapping.
			// Checking by existing mapping and not by reopen flag solves a problem with reopen wizard for to early closed wizard.
			// For example in the certificate step.
			String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
			Assert.notNull(systemId, "System ID cannot be null!");
			SysSystemDto systemDto = this.getSystemService().get(systemId);
			initDefaultConnectorSettings(systemDto, this.getSystemService().getConnectorFormDefinition(systemDto));
		}
		// Get test group and find parent group container. Will be used as default group container.
		if (connectorType.getMetadata().get(GROUP_CONTAINER_KEY) == null) {
			String testGroup = connectorType.getMetadata().get(TEST_GROUP_KEY);
			connectorType.getMetadata().put(GROUP_CONTAINER_KEY, getParent(testGroup));
		}
	}

	/**
	 * Converts list of containers to the string, separated by line separator.
	 */
	private String containersToString(List<String> containers) {
		if (containers == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		containers.forEach(container -> {
			sb.append(container);
			sb.append(LINE_SEPARATOR);
		});
		return sb.toString();
	}
	
	/**
	 * Converts list of containers to the string, separated by line separator.
	 */
	private List<String> stringToContainers(String strContainers) {
		if (Strings.isBlank(strContainers)){
			return Lists.newArrayList();
		}
		return Arrays.stream(strContainers.split(String.valueOf(LINE_SEPARATOR)))
				.collect(Collectors.toList());
	}

	/**
	 * Step for filling additional information as connector (OU) DNs. Add pairing sync.
	 */
	private void executeStepFour(ConnectorTypeDto connectorType) {
		String systemId = connectorType.getMetadata().get(SYSTEM_DTO_KEY);
		// connectorType.getMetadata().get(GROUP_SYNC_ID);
		Assert.notNull(systemId, "System ID cannot be null!");
		SysSystemDto systemDto = this.getSystemService().get(systemId);
		connectorType.getEmbedded().put(SYSTEM_DTO_KEY, systemDto);

		IdmFormDefinitionDto connectorFormDef = this.getSystemService().getConnectorFormDefinition(systemDto);
		String port = getValueFromConnectorInstance(PORT, systemDto, connectorFormDef);
		String host = getValueFromConnectorInstance(HOST, systemDto, connectorFormDef);
		String user = getValueFromConnectorInstance(PRINCIPAL, systemDto, connectorFormDef);
		boolean ssl = Boolean.parseBoolean(getValueFromConnectorInstance(SSL, systemDto, connectorFormDef));
		String password = getConfidentialValueFromConnectorInstance(CREDENTIALS, systemDto, connectorFormDef);

		String groupContainersStr = connectorType.getMetadata().get(GROUP_CONTAINER_KEY);
		Assert.notNull(groupContainersStr, "Container with groups cannot be null!");
		List<String> groupContainers = stringToContainers(groupContainersStr);
		Assert.notEmpty(groupContainers, "Container with groups cannot be empty!");

		groupContainers.forEach(groupContainer -> {
			String groupContainerAD = this.findDn(
					MessageFormat.format("(&(distinguishedName={0})(|(objectClass=container)(objectClass=organizationalUnit)))", groupContainer)
					, port, host, user, password, ssl);
			if (Strings.isBlank(groupContainerAD)) {
				throw new ResultCodeException(AccResultCode.WIZARD_AD_CONTAINER_NOT_FOUND,
						ImmutableMap.of("dn", groupContainer
						)
				);
			}
		});

		// Base context for search groups.
		// We need to searching in all containers. So group container will be use in the base context.
		List<Serializable> values = Lists.newArrayList(groupContainers);
		this.setValueToConnectorInstance(BASE_CONTEXT_GROUP_KEY, values, systemDto, connectorFormDef);

		// Set root suffixes and generate a schema.
		SysSchemaObjectClassDto schemaDto = generateSchema(connectorType, systemDto, connectorFormDef, groupContainers.get(0), values);

		// Find 'Member' schema attribute.
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemaDto.getId());
		schemaAttributeFilter.setSystemId(systemDto.getId());
		schemaAttributeFilter.setName(MsAdSyncMappingRoleAutoAttributesProcessor.MEMBER_ATTR_CODE);
		SysSchemaAttributeDto memberAttribute = getSchemaAttributeService().find(schemaAttributeFilter, null)
				.stream()
				.findFirst()
				.orElse(null);
		if (memberAttribute == null) {
			// Attribute missing -> create it now.
			createSchemaAttribute(schemaDto, MsAdSyncMappingRoleAutoAttributesProcessor.MEMBER_ATTR_CODE, String.class.getName(), true, false, true);
		}

		String mappingSyncId = connectorType.getMetadata().get(MAPPING_SYNC_ID);
		if (mappingSyncId == null) {
			// Create role mapping for sync.
			SysSystemMappingDto mappingDto = new SysSystemMappingDto();
			mappingDto.setObjectClass(schemaDto.getId());
			mappingDto.setOperationType(SystemOperationType.SYNCHRONIZATION);
			mappingDto.setEntityType(SystemEntityType.ROLE);
			mappingDto.setName("AD role sync mapping.");
			mappingDto.setAccountType(AccountType.PERSONAL);
			mappingDto = getSystemMappingService().publish(
					new SystemMappingEvent(
							SystemMappingEvent.SystemMappingEventType.CREATE,
							mappingDto,
							ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, Boolean.TRUE)))
					.getContent();
			mappingDto = getSystemMappingService().save(mappingDto);
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
			connectorType.getMetadata().put(MAPPING_SYNC_ID, mappingDto.getId().toString());
		} else {
			SysSystemMappingDto mappingDto = getSystemMappingService().get(UUID.fromString(mappingSyncId));
			connectorType.getEmbedded().put(DefaultConnectorType.MAPPING_DTO_KEY, mappingDto);
		}

		// Create/update role sync.
		createSync(connectorType);

		// Update group base contexts on the system with members.
		// Will add group container to the system with members. Without that system with member will not see groups.
		String memberSystemMappingId = connectorType.getMetadata().get(MEMBER_SYSTEM_MAPPING);
		SysSystemMappingDto systemMappingDto = null;
		if (memberSystemMappingId != null) {
			systemMappingDto = getSystemMappingService().get(UUID.fromString(memberSystemMappingId), IdmBasePermission.READ);
			if (systemMappingDto != null) {
				SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
				Assert.notNull(objectClassDto, "Schema DTO cannot be null!");
				SysSystemDto memberSystemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);
				Assert.notNull(memberSystemDto, "Member system DTO cannot be null!");
				// Find attribute with group base contexts.
				IdmFormDefinitionDto memberConnectorFormDef = this.getSystemService().getConnectorFormDefinition(memberSystemDto);
				IdmFormAttributeDto groupContextBaseAttribute = memberConnectorFormDef.getMappedAttributeByCode(BASE_CONTEXT_GROUP_KEY);
				if (groupContextBaseAttribute != null) {
					groupContainers.forEach(groupContainer -> {
						List<IdmFormValueDto> groupContextBaseValues = getFormService().getValues(memberSystemDto, groupContextBaseAttribute, IdmBasePermission.READ);
						if (groupContextBaseValues != null) {
							boolean groupContainerSet = groupContextBaseValues.stream()
									.anyMatch(value -> groupContainer.equals(value.getValue()));
							if (!groupContainerSet) {
								List<String> currentRootSuffixes = groupContextBaseValues
										.stream()
										.map(IdmFormValueDto::getStringValue)
										.collect(Collectors.toList());
								List<Serializable> newRootSuffixes = Lists.newArrayList(currentRootSuffixes);
								newRootSuffixes.add(groupContainer);
								// Save new root suffixes to the system with members.
								getFormService().saveValues(memberSystemDto, groupContextBaseAttribute, newRootSuffixes, IdmBasePermission.UPDATE);
							}
						}
					});
				}
			}
		}
	}

	/**
	 * Creates role sync.
	 */
	private void createSync(ConnectorTypeDto connectorType) {
		boolean membershipSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(SysSyncRoleConfig_.membershipSwitch.getName()));
		boolean assignCatalogueSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(SysSyncRoleConfig_.assignCatalogueSwitch.getName()));
		boolean assignRoleSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(SysSyncRoleConfig_.assignRoleSwitch.getName()));
		boolean assignRoleRemoveSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName()));
		boolean removeCatalogueRoleSwitch = Boolean.parseBoolean(connectorType.getMetadata().get(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName()));
		
		UUID mainRoleCatalogId = connectorType.getMetadata().get(MAIN_ROLE_CATALOG) != null
				? UUID.fromString(connectorType.getMetadata().get(MAIN_ROLE_CATALOG)) : null;
		String newRoleCatalogCode = connectorType.getMetadata().get(NEW_ROLE_CATALOG);

		// Get mapping ID.
		String mappingSyncId = connectorType.getMetadata().get(MAPPING_SYNC_ID);
		Assert.notNull(mappingSyncId, "ID of mapping cannot be null!");
		// Get sync ID.
		String roleSyncId = connectorType.getMetadata().get(GROUP_SYNC_ID);
		SysSyncRoleConfigDto syncRoleConfigDto = null;
		if (roleSyncId == null) {
			SysSystemAttributeMappingFilter codeFilter = new SysSystemAttributeMappingFilter();
			codeFilter.setSystemMappingId(UUID.fromString(mappingSyncId));
			codeFilter.setIdmPropertyName(IdmRole_.baseCode.getName());
			SysSystemAttributeMappingDto codeAttribute = getSystemAttributeMappingService().find(codeFilter, null)
					.getContent()
					.stream()
					.filter(SysSystemAttributeMappingDto::isEntityAttribute)
					.findFirst()
					.orElse(null);
			Assert.notNull(codeAttribute, "Code attribute cannot be null!");


			syncRoleConfigDto = new SysSyncRoleConfigDto();
			syncRoleConfigDto.setName(GROUP_SYNC_NAME);
			syncRoleConfigDto.setReconciliation(true);
			syncRoleConfigDto.setDifferentialSync(false);
			syncRoleConfigDto.setSystemMapping(UUID.fromString(mappingSyncId));

			syncRoleConfigDto.setUnlinkedAction(SynchronizationUnlinkedActionType.LINK_AND_UPDATE_ENTITY);
			syncRoleConfigDto.setMissingEntityAction(SynchronizationMissingEntityActionType.IGNORE);
			syncRoleConfigDto.setMissingAccountAction(ReconciliationMissingAccountActionType.IGNORE);
			syncRoleConfigDto.setLinkedAction(SynchronizationLinkedActionType.UPDATE_ENTITY);
			syncRoleConfigDto.setCorrelationAttribute(codeAttribute.getId());
		} else {
			syncRoleConfigDto = (SysSyncRoleConfigDto) getSyncConfigService().get(UUID.fromString(roleSyncId));
		}

		String memberSystemMappingId = connectorType.getMetadata().get(MEMBER_SYSTEM_MAPPING);
		SysSystemMappingDto systemMappingDto = null;
		if (memberSystemMappingId != null) {
			systemMappingDto = getSystemMappingService().get(UUID.fromString(memberSystemMappingId), IdmBasePermission.READ);
		}
		if (systemMappingDto != null) {
			// LDAP groups attribute.
			SysSystemAttributeMappingFilter attributeFilter = new SysSystemAttributeMappingFilter();
			attributeFilter.setSystemMappingId(systemMappingDto.getId());
			attributeFilter.setSchemaAttributeName(LDAP_GROUPS_ATTRIBUTE);
			SysSystemAttributeMappingDto ldapGroupsAttribute = getSystemAttributeMappingService().find(attributeFilter, null)
					.getContent()
					.stream()
					.findFirst()
					.orElse(null);
			syncRoleConfigDto.setMembershipSwitch(true);
			syncRoleConfigDto.setMemberSystemMapping(systemMappingDto.getId());
			if (ldapGroupsAttribute != null) {
				syncRoleConfigDto.setMemberOfAttribute(ldapGroupsAttribute.getId());
			}
			// Member DN schema attribute.
			SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
			schemaAttributeFilter.setObjectClassId(systemMappingDto.getObjectClass());
			schemaAttributeFilter.setName(DN_ATTR_CODE);
			SysSchemaAttributeDto dnAttribute = getSchemaAttributeService().find(schemaAttributeFilter, null)
					.getContent()
					.stream()
					.findFirst()
					.orElse(null);
			if (dnAttribute != null) {
				syncRoleConfigDto.setMemberIdentifierAttribute(dnAttribute.getId());
			}
		}

		syncRoleConfigDto.setAssignRoleSwitch(assignRoleSwitch);
		syncRoleConfigDto.setAssignCatalogueSwitch(assignCatalogueSwitch);
		syncRoleConfigDto.setAssignRoleRemoveSwitch(assignRoleRemoveSwitch);
		syncRoleConfigDto.setMembershipSwitch(membershipSwitch);
		syncRoleConfigDto.setRemoveCatalogueRoleSwitch(removeCatalogueRoleSwitch);
		if (mainRoleCatalogId != null) {
			syncRoleConfigDto.setMainCatalogueRoleNode(mainRoleCatalogId);
		} else if (Strings.isNotBlank(newRoleCatalogCode)) {
			// Check if new catalog is unique.
			IdmRoleCatalogueDto newRoleCatalog = roleCatalogueService.getByCode(newRoleCatalogCode);
			if (newRoleCatalog == null) {
				// Create new catalog.
				newRoleCatalog = new IdmRoleCatalogueDto();
				newRoleCatalog.setCode(newRoleCatalogCode);
				newRoleCatalog.setName(newRoleCatalogCode);
				newRoleCatalog = roleCatalogueService.save(newRoleCatalog, IdmBasePermission.CREATE);
			}
			syncRoleConfigDto.setMainCatalogueRoleNode(newRoleCatalog.getId());
		}
		if (syncRoleConfigDto.isRemoveCatalogueRoleSwitch()) {
			// If removing of a catalog is enabled, then main catalog will be use as parent.
			syncRoleConfigDto.setRemoveCatalogueRoleParentNode(syncRoleConfigDto.getMainCatalogueRoleNode());
		}

		syncRoleConfigDto = (SysSyncRoleConfigDto) getSyncConfigService().save(syncRoleConfigDto);
		connectorType.getMetadata().put(GROUP_SYNC_ID, syncRoleConfigDto.getId().toString());
	}

	protected void initDefaultConnectorSettings(SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		// Set the entry object classes.
		List<Serializable> values = Lists.newArrayList((Serializable[]) ENTRY_OBJECT_CLASSES_DEFAULT_VALUES);
		this.setValueToConnectorInstance(ENTRY_OBJECT_CLASSES_KEY, values, systemDto, connectorFormDef);
		// Set the object classes to sync.
		values = Lists.newArrayList((Serializable[]) ENTRY_OBJECT_CLASSES_DEFAULT_VALUES);
		this.setValueToConnectorInstance(OBJECT_CLASSES_TO_SYNC_KEY, values, systemDto, connectorFormDef);
		// Set use VLV search.
		this.setValueToConnectorInstance(USE_VLV_SORT_KEY, Boolean.TRUE, systemDto, connectorFormDef);
		// Set the VLV attribute.
		this.setValueToConnectorInstance(VLV_SORT_ATTRIBUTE_KEY, CN_VALUE, systemDto, connectorFormDef);
		// Set the VLV page size attribute.
		this.setValueToConnectorInstance(PAGE_SIZE_KEY, PAGE_SIZE_DEFAULT_VALUE, systemDto, connectorFormDef);
		// Default UID key.
		this.setValueToConnectorInstance(DEFAULT_UID_KEY, OBJECT_GUID_ATTRIBUTE, systemDto, connectorFormDef);
		this.setValueToConnectorInstance(UID_FOR_GROUP_ATTRIBUTE, OBJECT_GUID_ATTRIBUTE, systemDto, connectorFormDef);

		// Enable connector pooling.
		IdmFormDefinitionDto poolingConnectorFormDefinition = getSystemService().getPoolingConnectorFormDefinition(systemDto);
		this.setValueToConnectorInstance(SysSystemService.POOLING_SUPPORTED_PROPERTY, Boolean.TRUE, systemDto, poolingConnectorFormDefinition);
	}
}
