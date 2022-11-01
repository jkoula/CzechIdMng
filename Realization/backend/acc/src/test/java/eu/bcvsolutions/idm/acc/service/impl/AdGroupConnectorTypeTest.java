package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.connector.AdGroupConnectorType;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
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
import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig_;
import eu.bcvsolutions.idm.acc.event.processor.MsAdSyncMappingRoleAutoAttributesProcessor;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.mock.MockAdGroupConnectorType;
import eu.bcvsolutions.idm.acc.service.impl.mock.MockAdUserConnectorType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCatalogueService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests for AD Group connector type.
 *
 * @author Vít Švanda
 * @since 11.1.0
 */
@Transactional
public class AdGroupConnectorTypeTest extends AbstractIntegrationTest {

	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmEntityStateService entityStateService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSchemaObjectClassService schemaService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSyncConfigService syncConfigService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmRoleCatalogueService roleCatalogueService;


	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testStepOne() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testStepOneByMemberSystem() {
		// Create system with members.
		SysSystemDto memberSystemDto = createMemberSystem();
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(memberSystemDto.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		SysSystemMappingDto mappingDto = mappingService.find(mappingFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(mappingDto);

		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.setReopened(false);
		connectorManager.load(connectorTypeDto);
		assertNotNull(connectorTypeDto);

		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_NAME, this.getHelper().createName());
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MEMBER_SYSTEM_MAPPING, mappingDto.getId().toString());
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_ONE);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);
		BaseDto systemDto = stepExecutedResult.getEmbedded().get(MockAdGroupConnectorType.SYSTEM_DTO_KEY);
		assertNotNull("System ID cannot be null!", systemDto);
		SysSystemDto system = systemService.get(systemDto.getId());
		assertNotNull(system);
		// Clean
		systemService.delete((SysSystemDto) systemDto);
		systemService.delete(memberSystemDto);
	}

	@Test(expected = ResultCodeException.class)
	public void testStepOneBySystemWithWrongConnector() {
		// Create system with table connector -> should cause an exception!
		SysSystemDto tableSystem = helper.createTestResourceSystem(true);
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(tableSystem.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		SysSystemMappingDto mappingDto = mappingService.find(mappingFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(mappingDto);

		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.setReopened(false);
		connectorManager.load(connectorTypeDto);
		assertNotNull(connectorTypeDto);

		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_NAME, this.getHelper().createName());
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MEMBER_SYSTEM_MAPPING, mappingDto.getId().toString());
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_ONE);

		// Execute the first step.
		try {
			connectorManager.execute(connectorTypeDto);
		} finally {
			// Clean
			systemService.delete(tableSystem);
		}
	}

	@Test
	public void testCreateUser() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_CREATE_USER_TEST);
		// Execute step for testing permissions to create user.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		String entityStateId = stepExecutedResult.getMetadata().get(MockAdGroupConnectorType.ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		assertNotNull(entityStateId);
		String testUserName = stepExecutedResult.getMetadata().get(MockAdGroupConnectorType.TEST_USERNAME_KEY);
		String createdTestUserName = stepExecutedResult.getMetadata().get(MockAdGroupConnectorType.TEST_CREATED_USER_DN_KEY);
		assertNotNull(createdTestUserName);
		assertEquals(testUserName, createdTestUserName);

		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testDeleteUser() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_CREATE_USER_TEST);
		// Execute step for testing permissions to create user.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		String entityStateId = stepExecutedResult.getMetadata().get(MockAdGroupConnectorType.ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		assertNotNull(entityStateId);
		IdmEntityStateDto entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNotNull(entityStateDto);

		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_DELETE_USER_TEST);
		// Execute step for testing permissions to delete user.
		connectorManager.execute(connectorTypeDto);
		entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNull(entityStateDto);

		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testAssignUserToGroup() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_CREATE_USER_TEST);
		// Execute step for testing permissions to create user.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);

		String entityStateId = stepExecutedResult.getMetadata().get(MockAdGroupConnectorType.ENTITY_STATE_WITH_TEST_CREATED_USER_DN_KEY);
		assertNotNull(entityStateId);
		IdmEntityStateDto entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNotNull(entityStateDto);

		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_ASSIGN_GROUP_TEST);
		// Execute step for testing permissions to assign user to the group.
		connectorManager.execute(connectorTypeDto);
		entityStateDto = entityStateService.get(UUID.fromString(entityStateId));
		assertNotNull(entityStateDto);

		// Clean
		entityStateService.delete(entityStateDto);
		systemService.delete(systemDto);
	}

	@Test
	public void testStepFour() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		StringBuilder sb = new StringBuilder();
		sb.append(this.getHelper().createName());
		sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		sb.append(this.getHelper().createName());

		String groupContainersMock = sb.toString();
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.GROUP_CONTAINER_KEY, groupContainersMock);
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);
		//  Execute step four.
		connectorManager.execute(connectorTypeDto);

		// Check containers in connector configuration.
		systemDto = systemService.get(systemDto.getId());
		IdmFormDefinitionDto connectorFormDefinition = systemService.getConnectorFormDefinition(systemDto);
		String newGroupContainers = containersToString(getValuesFromConnectorInstance(MockAdGroupConnectorType.BASE_CONTEXT_GROUP_KEY, systemDto, connectorFormDefinition));
		assertEquals(groupContainersMock.trim(), newGroupContainers.trim());

		// Check created schema attributes.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemId(systemDto.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> RoleSynchronizationExecutor.ROLE_MEMBERSHIP_ID_FIELD.equals(attribute.getIdmPropertyName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> RoleSynchronizationExecutor.ROLE_MEMBERS_FIELD.equals(attribute.getIdmPropertyName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> RoleSynchronizationExecutor.ROLE_FORWARD_ACM_FIELD.equals(attribute.getIdmPropertyName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> RoleSynchronizationExecutor.ROLE_SKIP_VALUE_IF_EXCLUDED_FIELD.equals(attribute.getIdmPropertyName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> RoleSynchronizationExecutor.ROLE_CATALOGUE_FIELD.equals(attribute.getIdmPropertyName())));

		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testOfDefaultSync() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		StringBuilder sb = new StringBuilder();
		sb.append(this.getHelper().createName());
		sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		sb.append(this.getHelper().createName());

		String groupContainersMock = sb.toString();
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.GROUP_CONTAINER_KEY, groupContainersMock);
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);
		//  Execute step four.
		connectorManager.execute(connectorTypeDto);

		SysSyncConfigFilter syncConfigFilter = new SysSyncConfigFilter();
		syncConfigFilter.setSystemId(systemDto.getId());
		AbstractSysSyncConfigDto syncConfigDto = syncConfigService.find(syncConfigFilter, null).getContent().stream().findFirst().orElse(null);
		assertTrue(syncConfigDto instanceof SysSyncRoleConfigDto);
		SysSyncRoleConfigDto syncRoleConfigDto = (SysSyncRoleConfigDto) syncConfigDto;

		assertFalse(syncRoleConfigDto.isMembershipSwitch());
		assertFalse(syncRoleConfigDto.isAssignRoleSwitch());
		assertFalse(syncRoleConfigDto.isAssignCatalogueSwitch());
		assertFalse(syncRoleConfigDto.isForwardAcmSwitch());
		assertFalse(syncRoleConfigDto.isSkipValueIfExcludedSwitch());

		assertNotNull(syncRoleConfigDto.getAssignCatalogueMappingAttribute());
		assertNotNull(syncRoleConfigDto.getSkipValueIfExcludedMappingAttribute());
		assertNotNull(syncRoleConfigDto.getForwardAcmMappingAttribute());
		assertNotNull(syncRoleConfigDto.getRoleMembersMappingAttribute());
		assertNotNull(syncRoleConfigDto.getRoleIdentifiersMappingAttribute());
		assertNull(syncRoleConfigDto.getMemberIdentifierAttribute());
		assertNull(syncRoleConfigDto.getMemberOfAttribute());

		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testOfFullSync() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		// Create system with members.
		SysSystemDto memberSystemDto = createMemberSystem();
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(memberSystemDto.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		SysSystemMappingDto mappingDto = mappingService.find(mappingFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(mappingDto);

		StringBuilder sb = new StringBuilder();
		sb.append(this.getHelper().createName());
		sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		sb.append(this.getHelper().createName());

		String groupContainersMock = sb.toString();
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.GROUP_CONTAINER_KEY, groupContainersMock);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MEMBER_SYSTEM_MAPPING, mappingDto.getId().toString());
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);

		IdmRoleCatalogueDto mainCatalog = getHelper().createRoleCatalogue(getHelper().createName());

		// Enable wizard switches.
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.membershipSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignCatalogueSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MAIN_ROLE_CATALOG, mainCatalog.getId().toString());

		//  Execute step four.
		connectorManager.execute(connectorTypeDto);

		SysSyncConfigFilter syncConfigFilter = new SysSyncConfigFilter();
		syncConfigFilter.setSystemId(systemDto.getId());
		AbstractSysSyncConfigDto syncConfigDto = syncConfigService.find(syncConfigFilter, null).getContent().stream().findFirst().orElse(null);
		assertTrue(syncConfigDto instanceof SysSyncRoleConfigDto);
		SysSyncRoleConfigDto syncRoleConfigDto = (SysSyncRoleConfigDto) syncConfigDto;

		assertTrue(syncRoleConfigDto.isMembershipSwitch());
		assertTrue(syncRoleConfigDto.isRemoveCatalogueRoleSwitch());
		assertTrue(syncRoleConfigDto.isAssignRoleRemoveSwitch());
		assertTrue(syncRoleConfigDto.isAssignRoleSwitch());
		assertTrue(syncRoleConfigDto.isAssignCatalogueSwitch());

		assertNotNull(syncRoleConfigDto.getAssignCatalogueMappingAttribute());
		assertNotNull(syncRoleConfigDto.getSkipValueIfExcludedMappingAttribute());
		assertNotNull(syncRoleConfigDto.getForwardAcmMappingAttribute());
		assertNotNull(syncRoleConfigDto.getRoleMembersMappingAttribute());
		assertNotNull(syncRoleConfigDto.getRoleIdentifiersMappingAttribute());
		assertNotNull(syncRoleConfigDto.getMemberIdentifierAttribute());
		assertNotNull(syncRoleConfigDto.getMemberOfAttribute());

		// Clean
		systemService.delete(systemDto);
		systemService.delete(memberSystemDto);
	}
	
	@Test
	public void testReuseNewCatalog() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		// Create system with members.
		SysSystemDto memberSystemDto = createMemberSystem();
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(memberSystemDto.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		SysSystemMappingDto mappingDto = mappingService.find(mappingFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(mappingDto);

		StringBuilder sb = new StringBuilder();
		sb.append(this.getHelper().createName());
		sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		sb.append(this.getHelper().createName());

		String groupContainersMock = sb.toString();
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.GROUP_CONTAINER_KEY, groupContainersMock);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MEMBER_SYSTEM_MAPPING, mappingDto.getId().toString());
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);

		IdmRoleCatalogueDto mainCatalog = getHelper().createRoleCatalogue(getHelper().createName());

		// Enable wizard switches.
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.membershipSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignCatalogueSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.NEW_ROLE_CATALOG, mainCatalog.getCode());

		//  Execute step four.
		connectorManager.execute(connectorTypeDto);

		SysSyncConfigFilter syncConfigFilter = new SysSyncConfigFilter();
		syncConfigFilter.setSystemId(systemDto.getId());
		AbstractSysSyncConfigDto syncConfigDto = syncConfigService.find(syncConfigFilter, null).getContent().stream().findFirst().orElse(null);
		assertTrue(syncConfigDto instanceof SysSyncRoleConfigDto);
		SysSyncRoleConfigDto syncRoleConfigDto = (SysSyncRoleConfigDto) syncConfigDto;
		
		assertTrue(syncRoleConfigDto.isRemoveCatalogueRoleSwitch());
		assertTrue(syncRoleConfigDto.isAssignCatalogueSwitch());

		UUID mainCatalogueRoleNode = syncRoleConfigDto.getMainCatalogueRoleNode();
		assertEquals(mainCatalog.getId(), mainCatalogueRoleNode);
		UUID removeCatalogueRoleParentNode = syncRoleConfigDto.getRemoveCatalogueRoleParentNode();
		assertEquals(mainCatalog.getId(), removeCatalogueRoleParentNode);

		// Clean
		systemService.delete(systemDto);
		systemService.delete(memberSystemDto);
	}
	
	@Test
	public void testCreateNewCatalog() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		// Create system with members.
		SysSystemDto memberSystemDto = createMemberSystem();
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setSystemId(memberSystemDto.getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		mappingFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		SysSystemMappingDto mappingDto = mappingService.find(mappingFilter, null).getContent()
				.stream()
				.findFirst()
				.orElse(null);
		assertNotNull(mappingDto);

		StringBuilder sb = new StringBuilder();
		sb.append(this.getHelper().createName());
		sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		sb.append(this.getHelper().createName());

		String groupContainersMock = sb.toString();
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.GROUP_CONTAINER_KEY, groupContainersMock);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MEMBER_SYSTEM_MAPPING, mappingDto.getId().toString());
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);

		String mainCatalog = getHelper().createName();

		// Enable wizard switches.
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.membershipSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignCatalogueSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.NEW_ROLE_CATALOG, mainCatalog);

		//  Execute step four.
		connectorManager.execute(connectorTypeDto);

		SysSyncConfigFilter syncConfigFilter = new SysSyncConfigFilter();
		syncConfigFilter.setSystemId(systemDto.getId());
		AbstractSysSyncConfigDto syncConfigDto = syncConfigService.find(syncConfigFilter, null).getContent().stream().findFirst().orElse(null);
		assertTrue(syncConfigDto instanceof SysSyncRoleConfigDto);
		SysSyncRoleConfigDto syncRoleConfigDto = (SysSyncRoleConfigDto) syncConfigDto;
		
		assertTrue(syncRoleConfigDto.isRemoveCatalogueRoleSwitch());
		assertTrue(syncRoleConfigDto.isAssignCatalogueSwitch());

		IdmRoleCatalogueDto roleCatalogueDto = roleCatalogueService.getByCode(mainCatalog);
		assertNotNull(roleCatalogueDto);

		UUID mainCatalogueRoleNode = syncRoleConfigDto.getMainCatalogueRoleNode();
		assertEquals(roleCatalogueDto.getId(), mainCatalogueRoleNode);
		UUID removeCatalogueRoleParentNode = syncRoleConfigDto.getRemoveCatalogueRoleParentNode();
		assertEquals(roleCatalogueDto.getId(), removeCatalogueRoleParentNode);

		// Clean
		systemService.delete(systemDto);
		systemService.delete(memberSystemDto);
	}

	@Test
	public void testReopenSystem() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		StringBuilder sb = new StringBuilder();
		sb.append(this.getHelper().createName());
		sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		sb.append(this.getHelper().createName());

		String groupContainersMock = sb.toString();
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.GROUP_CONTAINER_KEY, groupContainersMock);
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockSchema(systemDto);

		IdmRoleCatalogueDto mainCatalog = getHelper().createRoleCatalogue(getHelper().createName());

		// Enable wizard switches.
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.membershipSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignCatalogueSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName(), Boolean.TRUE.toString());
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.MAIN_ROLE_CATALOG, mainCatalog.getId().toString());

		//  Execute step four. 
		connectorManager.execute(connectorTypeDto);

		connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.setReopened(true);
		connectorTypeDto.getEmbedded().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		ConnectorTypeDto loadedConnectorTypeDto = connectorManager.load(connectorTypeDto);
		assertNotNull(loadedConnectorTypeDto);
		assertNotNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.PORT));
		assertNotNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.HOST));
		assertNotNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.USER));
		assertNotNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.SSL_SWITCH));
		assertNotNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.GROUP_CONTAINER_KEY));
		assertNotNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.GROUP_SYNC_ID));
		assertTrue(Boolean.parseBoolean(loadedConnectorTypeDto.getMetadata().get(SysSyncRoleConfig_.membershipSwitch.getName())));
		assertTrue(Boolean.parseBoolean(loadedConnectorTypeDto.getMetadata().get(SysSyncRoleConfig_.assignCatalogueSwitch.getName())));
		assertTrue(Boolean.parseBoolean(loadedConnectorTypeDto.getMetadata().get(SysSyncRoleConfig_.assignRoleSwitch.getName())));
		assertTrue(Boolean.parseBoolean(loadedConnectorTypeDto.getMetadata().get(SysSyncRoleConfig_.assignRoleRemoveSwitch.getName())));
		assertTrue(Boolean.parseBoolean(loadedConnectorTypeDto.getMetadata().get(SysSyncRoleConfig_.removeCatalogueRoleSwitch.getName())));
		assertNull(loadedConnectorTypeDto.getMetadata().get(MockAdGroupConnectorType.MEMBER_SYSTEM_MAPPING));

		// Clean
		systemService.delete(systemDto);
	}

	@Test
	public void testReopenSystemWithoutOptionsAttributes() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		IdmFormDefinitionDto operationOptionsConnectorFormDefinition = systemService.getOperationOptionsConnectorFormDefinition(systemDto);
		// Try to find attribute for one of container. If exist -> change code = simulate delete.
		IdmFormAttributeDto userSearchContainerAttribute = operationOptionsConnectorFormDefinition.getMappedAttributeByCode(MockAdGroupConnectorType.USER_SEARCH_CONTAINER_KEY);
		if (userSearchContainerAttribute != null) {
			userSearchContainerAttribute.setCode(getHelper().createName());
			formService.saveAttribute(userSearchContainerAttribute);
		}

		connectorType = connectorManager.getWizardType(MockAdGroupConnectorType.NAME);
		connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		connectorTypeDto.setReopened(true);
		connectorTypeDto.getEmbedded().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());
		ConnectorTypeDto loadedConnectorTypeDto = connectorManager.load(connectorTypeDto);
		assertNotNull(loadedConnectorTypeDto);

		// Clean
		systemService.delete(systemDto);
	}


	private void generateMockSchema(SysSystemDto systemDto) {
		SysSchemaObjectClassDto schemaAccount = new SysSchemaObjectClassDto();
		schemaAccount.setSystem(systemDto.getId());
		schemaAccount.setObjectClassName(IcObjectClassInfo.GROUP);
		schemaAccount = schemaService.save(schemaAccount);
		// __NAME__
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(IcAttributeInfo.NAME);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
		// name
		schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName("name");
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
		// DN
		schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(AdGroupConnectorType.DN_ATTR_CODE);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
		// member
		schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(MsAdSyncMappingRoleAutoAttributesProcessor.MEMBER_ATTR_CODE);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
	}

	private SysSystemDto createSystem(String systemName, ConnectorTypeDto connectorTypeDto) {

		connectorTypeDto.setReopened(false);
		connectorManager.load(connectorTypeDto);
		assertNotNull(connectorTypeDto);

		String fakeHost = this.getHelper().createName();

		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.HOST, fakeHost);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.PORT, "636");
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.USER, fakeHost);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.PASSWORD, fakeHost);
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SSL_SWITCH, "false");
		connectorTypeDto.getMetadata().put(MockAdGroupConnectorType.SYSTEM_NAME, systemName);
		connectorTypeDto.setWizardStepName(MockAdGroupConnectorType.STEP_ONE);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);
		BaseDto systemDto = stepExecutedResult.getEmbedded().get(MockAdGroupConnectorType.SYSTEM_DTO_KEY);
		assertNotNull("System ID cannot be null!", systemDto);
		SysSystemDto system = systemService.get(systemDto.getId());
		assertNotNull(system);
		return system;
	}

	private SysSystemDto createMemberSystem() {
		ConnectorType connectorType = connectorManager.getWizardType(MockAdUserConnectorType.NAME);
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		String newUserContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.NEW_USER_CONTAINER_KEY, newUserContainerMock);
		String userContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.USER_SEARCH_CONTAINER_KEY, userContainerMock);
		String deletedUserContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.DELETE_USER_CONTAINER_KEY, deletedUserContainerMock);
		String domainMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockAdUserConnectorType.DOMAIN_KEY, domainMock);
		connectorTypeDto.setWizardStepName(MockAdUserConnectorType.STEP_FOUR);

		// Generate mock schema.
		generateMockMemberSchema(systemDto);
		//  Execute step four.
		connectorManager.execute(connectorTypeDto);

		// Check containers on the system's operationOptions.
		systemDto = systemService.get(systemDto.getId());
		IdmFormDefinitionDto operationOptionsFormDefinition = systemService.getOperationOptionsConnectorFormDefinition(systemDto);
		String newUserContainer = getValueFromConnectorInstance(MockAdUserConnectorType.NEW_USER_CONTAINER_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(newUserContainerMock, newUserContainer);
		String deletedUserContainer = getValueFromConnectorInstance(MockAdUserConnectorType.DELETE_USER_CONTAINER_KEY, systemDto, operationOptionsFormDefinition);
		// Protected mode is not active -> delete user container should be null.
		assertNull(deletedUserContainer);
		String searchUserContainer = getValueFromConnectorInstance(MockAdUserConnectorType.USER_SEARCH_CONTAINER_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(userContainerMock, searchUserContainer);
		String domain = getValueFromConnectorInstance(MockAdUserConnectorType.DOMAIN_KEY, systemDto, operationOptionsFormDefinition);
		assertEquals(domainMock, domain);

		// Check created schema attributes.
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(systemDto.getId());
		List<SysSchemaAttributeDto> attributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
		assertTrue(attributes.stream().anyMatch(attribute -> IcAttributeInfo.NAME.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> IcAttributeInfo.PASSWORD.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> IcAttributeInfo.ENABLE.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> MockAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(attribute.getName())));
		assertTrue(attributes.stream().anyMatch(attribute -> MockAdUserConnectorType.SAM_ACCOUNT_NAME_ATTRIBUTE.equals(attribute.getName())));

		// Check created schema attributes.
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemId(systemDto.getId());
		List<SysSystemAttributeMappingDto> attributeMappingDtos = attributeMappingService.find(attributeMappingFilter, null).getContent();
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> IcAttributeInfo.NAME.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> IcAttributeInfo.PASSWORD.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> IcAttributeInfo.ENABLE.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> MockAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(attribute.getName())));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute ->
				MockAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(attribute.getName())
						&& AttributeMappingStrategyType.MERGE == attribute.getStrategyType()));
		assertTrue(attributeMappingDtos.stream().anyMatch(attribute -> MockAdUserConnectorType.SAM_ACCOUNT_NAME_ATTRIBUTE.equals(attribute.getName())));

		return systemDto;
	}

	private void generateMockMemberSchema(SysSystemDto systemDto) {
		SysSchemaObjectClassDto schemaAccount = new SysSchemaObjectClassDto();
		schemaAccount.setSystem(systemDto.getId());
		schemaAccount.setObjectClassName(IcObjectClassInfo.ACCOUNT);
		schemaAccount = schemaService.save(schemaAccount);
		SysSchemaAttributeDto schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(IcAttributeInfo.NAME);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
		// DN
		schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(AdGroupConnectorType.DN_ATTR_CODE);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(false);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
		// Ldap groups
		schemaAttribute = new SysSchemaAttributeDto();
		schemaAttribute.setName(AdGroupConnectorType.LDAP_GROUPS_ATTRIBUTE);
		schemaAttribute.setObjectClass(schemaAccount.getId());
		schemaAttribute.setMultivalued(true);
		schemaAttribute.setReadable(true);
		schemaAttribute.setUpdateable(true);
		schemaAttribute.setReturnedByDefault(true);
		schemaAttribute.setRequired(true);
		schemaAttribute.setClassType(String.class.getName());
		schemaAttributeService.save(schemaAttribute);
	}

	protected String getValueFromConnectorInstance(String attributeCode, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		List<IdmFormValueDto> values = formService.getValues(systemDto, attribute, IdmBasePermission.READ);
		if (values != null && values.size() == 1) {
			return (String) values.get(0).getValue();
		}
		return null;
	}

	protected List<String> getValuesFromConnectorInstance(String attributeCode, SysSystemDto systemDto, IdmFormDefinitionDto connectorFormDef) {
		IdmFormAttributeDto attribute = connectorFormDef.getMappedAttributeByCode(attributeCode);
		if (attribute != null) {
			List<IdmFormValueDto> values = formService.getValues(systemDto, attribute, IdmBasePermission.READ);
			return values.stream()
					.map(IdmFormValueDto::getStringValue)
					.collect(Collectors.toList());
		}
		return Lists.newArrayList();
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
			sb.append(AdGroupConnectorType.LINE_SEPARATOR);
		});
		return sb.toString();
	}
}
