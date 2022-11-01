package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.ProvisioningEventType;
import eu.bcvsolutions.idm.acc.domain.SystemGroupType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.ConnectorTypeDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ConnectorManager;
import eu.bcvsolutions.idm.acc.service.api.ConnectorType;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.impl.mock.MockCrossDomainAdUserConnectorType;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcConfigurationProperty;
import eu.bcvsolutions.idm.ic.api.IcConnectorConfiguration;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;
import eu.bcvsolutions.idm.ic.api.IcObjectClass;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcConnectorObjectImpl;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.common.security.SecurityUtil;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Tests for cross-domains on AD User connector type.
 *
 * @author Vít Švanda
 * @since 11.2.0
 * @noinspection OptionalGetWithoutIsPresent
 */
public class CrossDomainAdUserConnectorTypeTest extends AbstractIntegrationTest {

	@Autowired
	private ConnectorManager connectorManager;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private SysSchemaObjectClassService schemaService;
	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	@Autowired
	private TestHelper helper;
	@Autowired
	private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemGroupSystemService systemGroupSystemService;
	@Autowired
	private SysSystemGroupService systemGroupService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private SysProvisioningOperationService provisioningOperationService;
	@Autowired
	@Qualifier(MockCrossDomainAdUserConnectorType.NAME)
	private MockCrossDomainAdUserConnectorType mockCrossDomainAdUserConnectorType;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired
	private IdmRoleCompositionService roleCompositionService;
	@Autowired
	private IdmConceptRoleRequestService conceptRoleRequestService;

	@Before
	public void init() {
		loginAsAdmin();
	}

	@After
	public void logout() {
		super.logout();
	}

	@Test
	public void testRoleSystemInCrossDomainGroup() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemOne = systemGroupSystemService.save(systemGroupSystemOne);

		// Creates cross-domain no-login role.
		IdmRoleDto roleInCrossDomainGroup = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(roleInCrossDomainGroup, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(roleInCrossDomainGroup.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		// Clean
		systemGroupService.delete(groupSystemDto);
		assertNull(systemGroupSystemService.get(systemGroupSystemOne));
		systemService.delete(systemDto);
		roleService.delete(roleInCrossDomainGroup);
	}

	@Test
	public void testRoleSystemInDisabledCrossDomainGroup() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto.setDisabled(true);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemOne = systemGroupSystemService.save(systemGroupSystemOne);

		// Creates cross-domain no-login role.
		IdmRoleDto roleInCrossDomainGroup = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(roleInCrossDomainGroup, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(roleInCrossDomainGroup.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be not in a cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Flag 'isInCrossDomainGroup' in role-system should be false now.
		roleSystemFilter.setIsInCrossDomainGroupRoleId(null);
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertFalse(roleSystemDto.isInCrossDomainGroup());

		// Clean
		systemGroupService.delete(groupSystemDto);
		assertNull(systemGroupSystemService.get(systemGroupSystemOne));
		systemService.delete(systemDto);
		roleService.delete(roleInCrossDomainGroup);
	}

	@Test
	public void testRoleInCrossDomainGroupCannotCreateAccount() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates cross-domain no-login role.
		IdmRoleDto roleInCrossDomainGroup = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(roleInCrossDomainGroup, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(roleInCrossDomainGroup.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, roleInCrossDomainGroup);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNull(roleRequestDto.getSystemState());

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		systemGroupService.delete(groupSystemDto);
		getHelper().deleteIdentity(identity.getId());
	}

	@Test
	public void testUpdateAccountInCrossDomain() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates cross-domain no-login role.
		IdmRoleDto roleInCrossDomainGroup = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(roleInCrossDomainGroup, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(roleInCrossDomainGroup.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);

		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack((system, uid, objectClass) -> {
			IcConnectorObjectImpl connectorObject = new IcConnectorObjectImpl(identity.getUsername(), null, null);
			connectorObject.getAttributes().add(new IcAttributeImpl(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE, "TWO"));
			return mockCrossDomainAdUserConnectorType.getCrossDomainConnectorObject(system, uid, objectClass, connectorObject);
		});
		
		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, roleInCrossDomainGroup, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());


		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		assertEquals(ProvisioningEventType.UPDATE, provisioningOperationDto.getOperationType());


		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();


		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(1, ((List<?>) ldapGroupsValue).size());
		assertTrue(((List<?>) ldapGroupsValue).stream().anyMatch(value -> value.equals("ONE")));

		IcAttribute ldapGroups = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		IcAttribute ldapGroupsOld = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MessageFormat.format(MockCrossDomainAdUserConnectorType.OLD_ATTRIBUTE_PATTERN, MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE));
		assertNotNull(ldapGroups);
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("ONE")));
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("TWO")));
		assertNotNull(ldapGroupsOld);
		assertEquals(1, ldapGroupsOld.getValues().size());
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("TWO")));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		systemGroupService.delete(groupSystemDto);
		getHelper().deleteIdentity(identity.getId());
		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack(null);
	}
	
	@Test
	public void testUpdateAccountInCrossDomainOnTwoSystems() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		// System one
		SysSystemDto systemDto = initSystem(connectorType);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();
		// System two
		SysSystemDto systemTwoDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filterTwo = new SysSystemAttributeMappingFilter();
		filterTwo.setSystemId(systemTwoDto.getId());
		filterTwo.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributesTwo = attributeMappingService.find(filterTwo, null).getContent();
		assertEquals(1, attributesTwo.size());
		SysSystemAttributeMappingDto ldapGroupsAttributeTwo = attributesTwo.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);
		
		SysSystemGroupSystemDto systemGroupSystemTwo = new SysSystemGroupSystemDto();
		systemGroupSystemTwo.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemTwo.setMergeAttribute(ldapGroupsAttributeTwo.getId());
		systemGroupSystemTwo.setSystem(systemTwoDto.getId());
		systemGroupSystemService.save(systemGroupSystemTwo);

		// Creates the login role ONE.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);
		// Creates the login role TWO.
		IdmRoleDto loginRoleTwo = helper.createRole();
		helper.createRoleSystem(loginRoleTwo, systemTwoDto);

		// Creates cross-domain no-login role ONE.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);
		
		// Creates cross-domain no-login role TWO.
		SysRoleSystemDto roleSystemTwo = helper.createRoleSystem(noLoginRole, systemTwoDto);
		SysRoleSystemFilter roleSystemFilterTwo = new SysRoleSystemFilter();
		roleSystemFilterTwo.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilterTwo.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilterTwo.setId(roleSystemTwo.getId());
		List<SysRoleSystemDto> roleSystemDtosTwo = roleSystemService.find(roleSystemFilterTwo, null).getContent();
		assertEquals(0, roleSystemDtosTwo.size());
		createOverriddenLdapGroupAttribute(ldapGroupsAttributeTwo, roleSystemTwo, "return 'TWO';");

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilterTwo, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);

		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack((system, uid, objectClass) -> {
			IcConnectorObjectImpl connectorObject = new IcConnectorObjectImpl(identity.getUsername(), null, null);
			connectorObject.getAttributes().add(new IcAttributeImpl(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE, "THREE"));
			connectorObject.getAttributes().add(new IcAttributeImpl(MockCrossDomainAdUserConnectorType.SID_ATTRIBUTE_KEY, "SID".getBytes(StandardCharsets.UTF_8)));
			return mockCrossDomainAdUserConnectorType.getCrossDomainConnectorObject(system, uid, objectClass, connectorObject);
		});
		// Assign login (ONE and TWO) and no-login roles.
		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, noLoginRole, loginRole, loginRoleTwo);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		assertEquals(2, identityAccountService.find(identityAccountFilter, null).getContent().size());
		
		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		assertEquals(ProvisioningEventType.UPDATE, provisioningOperationDto.getOperationType());
		
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();
		
		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(1, ((List<?>) ldapGroupsValue).size());
		assertTrue(((List<?>) ldapGroupsValue).stream().anyMatch(value -> value.equals("ONE")));

		IcAttribute ldapGroups = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		IcAttribute ldapGroupsOld = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MessageFormat.format(MockCrossDomainAdUserConnectorType.OLD_ATTRIBUTE_PATTERN, MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE));
		assertNotNull(ldapGroups);
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("ONE")));
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("THREE")));
		assertNotNull(ldapGroupsOld);
		assertEquals(2, ldapGroupsOld.getValues().size());
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("THREE")));
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("EXTERNAL_ONE")));
		
		
		// Check if provisioning contains ldapGroups attribute with value ('TWO') from the role.
		provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemTwoDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		assertEquals(ProvisioningEventType.UPDATE, provisioningOperationDto.getOperationType());
		
		provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();
		
		assertNotNull(provisioningAttributeLdapGroupsDto);
		ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(1, ((List<?>) ldapGroupsValue).size());
		assertTrue(((List<?>) ldapGroupsValue).stream().anyMatch(value -> value.equals("TWO")));

		ldapGroups = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		ldapGroupsOld = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MessageFormat.format(MockCrossDomainAdUserConnectorType.OLD_ATTRIBUTE_PATTERN, MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE));
		assertNotNull(ldapGroups);
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("TWO")));
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("THREE")));
		assertNotNull(ldapGroupsOld);
		assertEquals(2, ldapGroupsOld.getValues().size());
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("THREE")));
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("EXTERNAL_ONE")));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		provisioningOperationService.deleteOperations(systemTwoDto.getId());
		systemGroupService.delete(groupSystemDto);
		getHelper().deleteIdentity(identity.getId());
		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack(null);
	}
	
	@Test
	public void testUpdateAccountInCrossDomainOnOneSystem() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		// System one
		SysSystemDto systemDto = initSystem(connectorType);
		
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();
		// System two
		SysSystemDto systemTwoDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filterTwo = new SysSystemAttributeMappingFilter();
		filterTwo.setSystemId(systemTwoDto.getId());
		filterTwo.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributesTwo = attributeMappingService.find(filterTwo, null).getContent();
		assertEquals(1, attributesTwo.size());
		SysSystemAttributeMappingDto ldapGroupsAttributeTwo = attributesTwo.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);
		
		SysSystemGroupSystemDto systemGroupSystemTwo = new SysSystemGroupSystemDto();
		systemGroupSystemTwo.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemTwo.setMergeAttribute(ldapGroupsAttributeTwo.getId());
		systemGroupSystemTwo.setSystem(systemTwoDto.getId());
		systemGroupSystemService.save(systemGroupSystemTwo);

		// Creates the login role ONE.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);
		// Creates the login role TWO.
		IdmRoleDto loginRoleTwo = helper.createRole();
		helper.createRoleSystem(loginRoleTwo, systemTwoDto);

		// Creates cross-domain no-login role ONE.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);
		
		// Creates cross-domain no-login role TWO.
		SysRoleSystemDto roleSystemTwo = helper.createRoleSystem(noLoginRole, systemTwoDto);
		SysRoleSystemFilter roleSystemFilterTwo = new SysRoleSystemFilter();
		roleSystemFilterTwo.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilterTwo.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilterTwo.setId(roleSystemTwo.getId());
		List<SysRoleSystemDto> roleSystemDtosTwo = roleSystemService.find(roleSystemFilterTwo, null).getContent();
		assertEquals(0, roleSystemDtosTwo.size());
		createOverriddenLdapGroupAttribute(ldapGroupsAttributeTwo, roleSystemTwo, "return 'TWO';");

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilterTwo, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);

		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack((system, uid, objectClass) -> {
			IcConnectorObjectImpl connectorObject = new IcConnectorObjectImpl(identity.getUsername(), null, null);
			connectorObject.getAttributes().add(new IcAttributeImpl(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE, "THREE"));
			connectorObject.getAttributes().add(new IcAttributeImpl(MockCrossDomainAdUserConnectorType.SID_ATTRIBUTE_KEY, "SID".getBytes(StandardCharsets.UTF_8)));
			return mockCrossDomainAdUserConnectorType.getCrossDomainConnectorObject(system, uid, objectClass, connectorObject);
		});
		
		// Assign login (ONE and TWO) and no-login roles.
		// But no-login role will be set only on system two!
		IdmRoleRequestDto roleRequestDto = getHelper().createRoleRequest(contract, noLoginRole, loginRole, loginRoleTwo);
		List<IdmConceptRoleRequestDto> concepts = conceptRoleRequestService.findAllByRoleRequest(roleRequestDto.getId());
		IdmConceptRoleRequestDto noLoginConcept = concepts.stream()
				.filter(concept -> noLoginRole.getId().equals(concept.getRole()))
				.findFirst()
				.get();
		assertNotNull(noLoginConcept);
		noLoginConcept.setRoleSystem(roleSystemTwo.getId());
		conceptRoleRequestService.save(noLoginConcept);
		roleRequestDto = getHelper().executeRequest(roleRequestDto, true);

		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());

		IdmIdentityRoleDto identityRoleWithRoleSystemDto = identityRoleService.findAllByIdentity(identity.getId()).stream()
				.filter(identityRole -> roleSystemTwo.getId().equals(identityRole.getRoleSystem()))
				.findFirst()
				.get();
		assertNotNull(identityRoleWithRoleSystemDto);

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		assertEquals(2, identityAccountService.find(identityAccountFilter, null).getContent().size());
		
		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		assertEquals(ProvisioningEventType.UPDATE, provisioningOperationDto.getOperationType());
		
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();
		
		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(0, ((List<?>) ldapGroupsValue).size());

		IcAttribute ldapGroups = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		IcAttribute ldapGroupsOld = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MessageFormat.format(MockCrossDomainAdUserConnectorType.OLD_ATTRIBUTE_PATTERN, MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE));
		assertNull(ldapGroups);
		assertNull(ldapGroupsOld);
		
		// Check if provisioning contains ldapGroups attribute with value ('TWO') from the role.
		provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemTwoDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		assertEquals(ProvisioningEventType.UPDATE, provisioningOperationDto.getOperationType());
		
		provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();
		
		assertNotNull(provisioningAttributeLdapGroupsDto);
		ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(1, ((List<?>) ldapGroupsValue).size());
		assertTrue(((List<?>) ldapGroupsValue).stream().anyMatch(value -> value.equals("TWO")));

		ldapGroups = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		ldapGroupsOld = provisioningOperationDto.getProvisioningContext().getConnectorObject()
				.getAttributeByName(MessageFormat.format(MockCrossDomainAdUserConnectorType.OLD_ATTRIBUTE_PATTERN, MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE));
		assertNotNull(ldapGroups);
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("TWO")));
		assertTrue(ldapGroups.getValues().stream().anyMatch(value -> value.equals("THREE")));
		assertNotNull(ldapGroupsOld);
		assertEquals(2, ldapGroupsOld.getValues().size());
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("THREE")));
		assertTrue(ldapGroupsOld.getValues().stream().anyMatch(value -> value.equals("EXTERNAL_ONE")));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		provisioningOperationService.deleteOperations(systemTwoDto.getId());
		systemGroupService.delete(groupSystemDto);
		getHelper().deleteIdentity(identity.getId());
		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack(null);
	}
	
	
	private void createOverriddenLdapGroupAttribute(SysSystemAttributeMappingDto ldapGroupsAttribute, SysRoleSystemDto roleSystem) {
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem, "return 'ONE';");
	}

	private void createOverriddenLdapGroupAttribute(SysSystemAttributeMappingDto ldapGroupsAttribute, SysRoleSystemDto roleSystem, String script) {
		// Creates overridden ldapGroup merge attribute.
		SysRoleSystemAttributeDto ldapGroupsRoleSystemAttribute = new SysRoleSystemAttributeDto();
		ldapGroupsRoleSystemAttribute.setSystemAttributeMapping(ldapGroupsAttribute.getId());
		ldapGroupsRoleSystemAttribute.setRoleSystem(roleSystem.getId());
		ldapGroupsRoleSystemAttribute.setEntityAttribute(ldapGroupsAttribute.isEntityAttribute());
		ldapGroupsRoleSystemAttribute.setSchemaAttribute(ldapGroupsAttribute.getSchemaAttribute());
		ldapGroupsRoleSystemAttribute.setExtendedAttribute(ldapGroupsAttribute.isExtendedAttribute());
		ldapGroupsRoleSystemAttribute.setName(ldapGroupsAttribute.getName());
		ldapGroupsRoleSystemAttribute.setStrategyType(ldapGroupsAttribute.getStrategyType());
		ldapGroupsRoleSystemAttribute.setIdmPropertyName(ldapGroupsAttribute.getIdmPropertyName());
		ldapGroupsRoleSystemAttribute.setUid(ldapGroupsAttribute.isUid());
		ldapGroupsRoleSystemAttribute.setTransformScript(script);
		roleSystemAttributeService.save(ldapGroupsRoleSystemAttribute);
	}

	@Test
	public void testGetConnectorObjectWithCrossDomainValues() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates cross-domain no-login role.
		IdmRoleDto roleInCrossDomainGroup = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(roleInCrossDomainGroup, systemDto);
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(roleInCrossDomainGroup.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);

		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack((system, uid, objectClass) -> {
			IcConnectorObjectImpl connectorObject = new IcConnectorObjectImpl(identity.getUsername(), null, null);
			connectorObject.getAttributes().add(new IcAttributeImpl(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE, "TWO"));
			return mockCrossDomainAdUserConnectorType.getCrossDomainConnectorObject(system, uid, objectClass, connectorObject);
		});

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, roleInCrossDomainGroup, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		List<AccIdentityAccountDto> identityAccountDtos = identityAccountService.find(identityAccountFilter, null).getContent();
		assertEquals(1, identityAccountDtos.size());

		UUID accountId = identityAccountDtos.get(0).getAccount();
		IcConnectorObject connectorObject = accountService.getConnectorObject(accountService.get(accountId));
		assertNotNull(connectorObject);
		IcAttribute ldapGroupsAtt = connectorObject.getAttributeByName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		assertEquals(1, ldapGroupsAtt.getValues().size());
		assertTrue(ldapGroupsAtt.getValues().stream().anyMatch(value -> value.equals("TWO")));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		systemGroupService.delete(groupSystemDto);
		getHelper().deleteIdentity(identity.getId());
		mockCrossDomainAdUserConnectorType.setReadConnectorObjectCallBack(null);
	}

	@Test
	public void testConnectorConfigurationLoginInformations() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		IcConnectorConfiguration connectorConfiguration = connectorType.getConnectorConfiguration(systemDto);
		Object otherSystemInGroupsObj = connectorConfiguration.getSystemOperationOptions().get(MockCrossDomainAdUserConnectorType.CROSS_DOMAIN_SYSTEM_IDS);
		// No others systems are in cross-domain groups.
		assertNotNull(otherSystemInGroupsObj);
		assertTrue(((String) otherSystemInGroupsObj).isEmpty());

		SysSystemDto otherSystemDto = initSystem(connectorType);
		filter.setSystemId(otherSystemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		ldapGroupsAttribute = attributes.stream().findFirst().get();

		SysSystemGroupSystemDto systemGroupSystemTwo = new SysSystemGroupSystemDto();
		systemGroupSystemTwo.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemTwo.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemTwo.setSystem(otherSystemDto.getId());
		systemGroupSystemTwo = systemGroupSystemService.save(systemGroupSystemTwo);

		connectorConfiguration = connectorType.getConnectorConfiguration(systemDto);
		otherSystemInGroupsObj = connectorConfiguration.getSystemOperationOptions().get(MockCrossDomainAdUserConnectorType.CROSS_DOMAIN_SYSTEM_IDS);
		// One other system is in cross-domain groups.
		assertNotNull(otherSystemInGroupsObj);
		String[] otherSystemInGroupsSplited = ((String) otherSystemInGroupsObj).split(",");
		assertEquals(1, otherSystemInGroupsSplited.length);
		assertEquals(otherSystemDto.getId().toString(), otherSystemInGroupsSplited[0]);

		IcConnectorConfiguration connectorConfigurationOtherSystem = systemService.getConnectorConfiguration(otherSystemDto);

		// Check host property.
		IcConfigurationProperty hostProperty = connectorConfigurationOtherSystem.getConfigurationProperties().getProperties()
				.stream()
				.filter(property -> MockCrossDomainAdUserConnectorType.HOST.equals(property.getName()))
				.findFirst()
				.get();
		String host = (String) connectorConfiguration.getSystemOperationOptions()
				.get(MessageFormat.format(MockCrossDomainAdUserConnectorType.CROSS_DOMAIN_HOST_PATTERN, otherSystemDto.getId().toString()));
		assertEquals(hostProperty.getValue(), host);

		// Check user property.
		IcConfigurationProperty userProperty = connectorConfigurationOtherSystem.getConfigurationProperties().getProperties()
				.stream()
				.filter(property -> MockCrossDomainAdUserConnectorType.PRINCIPAL.equals(property.getName()))
				.findFirst()
				.get();
		String user = (String) connectorConfiguration.getSystemOperationOptions()
				.get(MessageFormat.format(MockCrossDomainAdUserConnectorType.CROSS_DOMAIN_USER_PATTERN, otherSystemDto.getId().toString()));
		assertEquals(userProperty.getValue(), user);

		// Check password property.
		IcConfigurationProperty passwordProperty = connectorConfigurationOtherSystem.getConfigurationProperties().getProperties()
				.stream()
				.filter(property -> MockCrossDomainAdUserConnectorType.CREDENTIALS.equals(property.getName()))
				.findFirst()
				.get();
		GuardedString password = (GuardedString) connectorConfiguration.getSystemOperationOptions()
				.get(MessageFormat.format(MockCrossDomainAdUserConnectorType.CROSS_DOMAIN_PASSWORD_PATTERN, otherSystemDto.getId().toString()));
		assertEquals((SecurityUtil.decrypt((GuardedString) passwordProperty.getValue())), SecurityUtil.decrypt(password));

		// Clean
		systemGroupService.delete(groupSystemDto);
		assertNull(systemGroupSystemService.get(systemGroupSystemTwo));
		systemService.delete(systemDto);
	}

	@Test
	public void testDisableDefaultAccountCreation() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates no-login role.
		IdmRoleDto roleInCrossDomainGroup = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(roleInCrossDomainGroup, systemDto);
		roleSystem.setCreateAccountByDefault(false);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setCreateAccountByDefault(Boolean.FALSE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().createContract(identity);

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, roleInCrossDomainGroup);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNull(roleRequestDto.getSystemState());

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
	}

	@Test
	public void testDisableDefaultAccountCreationForAutomaticRole() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(false);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setCreateAccountByDefault(Boolean.FALSE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		String automaticRoleValue = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(noLoginRole.getId());
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(),
				null,
				automaticRoleValue);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		// Assign automatic role.
		identity.setDescription(automaticRoleValue);
		identityService.save(identity);

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		automaticRoleAttributeService.delete(automaticRole);
		getHelper().deleteRole(noLoginRole.getId());
	}

	@Test
	public void testRoleInCrossDomainGroupCannotCreateAccountForAutomaticRole() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(true);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		String automaticRoleValue = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(noLoginRole.getId());
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(),
				null,
				automaticRoleValue);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		// Assign automatic role.
		identity.setDescription(automaticRoleValue);
		identityService.save(identity);

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		automaticRoleAttributeService.delete(automaticRole);
		getHelper().deleteRole(noLoginRole.getId());
	}

	@Test
	public void testDisableDefaultAccountCreationForBusinessRole() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		IdmRoleDto parentNoLoginRole = helper.createRole();
		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(false);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setCreateAccountByDefault(Boolean.FALSE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		IdmRoleCompositionDto roleComposition = getHelper().createRoleComposition(parentNoLoginRole, noLoginRole);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		// Assign parent role.
		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, parentNoLoginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNull(roleRequestDto.getSystemState());

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		roleCompositionService.delete(roleComposition);
		getHelper().deleteRole(noLoginRole.getId());
		getHelper().deleteRole(parentNoLoginRole.getId());
	}

	@Test
	public void testRoleInCrossDomainGroupCannotCreateAccountForBusinessRole() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		IdmRoleDto parentNoLoginRole = helper.createRole();
		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(true);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmRoleCompositionDto roleComposition = getHelper().createRoleComposition(parentNoLoginRole, noLoginRole);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		// Assign parent role.
		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, parentNoLoginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNull(roleRequestDto.getSystemState());

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		roleCompositionService.delete(roleComposition);
		getHelper().deleteRole(noLoginRole.getId());
		getHelper().deleteRole(parentNoLoginRole.getId());
	}

	@Test
	public void testRoleInCrossDomainGroupProvisioningForBusinessRole() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		IdmRoleDto parentNoLoginRole = helper.createRole();
		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(true);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		IdmRoleCompositionDto roleComposition = getHelper().createRoleComposition(parentNoLoginRole, noLoginRole);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning NOT contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(0, ((List<?>) ldapGroupsValue).size());

		// Delete old provisioning.
		provisioningOperationService.delete(provisioningOperationDto);

		// Assign parent role.
		roleRequestDto = getHelper().assignRoles(contract, false, parentNoLoginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		roleCompositionService.delete(roleComposition);
		getHelper().deleteRole(noLoginRole.getId());
		getHelper().deleteRole(parentNoLoginRole.getId());
	}

	@Test
	public void testRoleInCrossDomainGroupProvisioningForAutomaticRole() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);

		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(true);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());

		String automaticRoleValue = getHelper().createName();
		IdmAutomaticRoleAttributeDto automaticRole = getHelper().createAutomaticRole(noLoginRole.getId());
		getHelper().createAutomaticRoleRule(
				automaticRole.getId(),
				AutomaticRoleAttributeRuleComparison.EQUALS,
				AutomaticRoleAttributeRuleType.IDENTITY,
				IdmIdentity_.description.getName(),
				null,
				automaticRoleValue);

		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning NOT contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(0, ((List<?>) ldapGroupsValue).size());

		// Delete old provisioning.
		provisioningOperationService.delete(provisioningOperationDto);

		// Assign automatic role.
		identity.setDescription(automaticRoleValue);
		identityService.save(identity);

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		// Two provisioning were made. First for save identity, second for assign automatic role.
		assertEquals(2, provisioningOperationDtos.size());
		provisioningOperationDto = provisioningOperationDtos
				.stream()
				.max(Comparator.comparing(SysProvisioningOperationDto::getCreated))
				.get();
		provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		automaticRoleAttributeService.delete(automaticRole);
		getHelper().deleteRole(noLoginRole.getId());
	}

	@Test
	public void testRoleInCrossDomainGroupProvisioning() {
		ConnectorType connectorType = connectorManager.getWizardType(MockCrossDomainAdUserConnectorType.NAME);
		SysSystemDto systemDto = initSystem(connectorType);

		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemDto.getId());
		filter.setName(MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE);
		List<SysSystemAttributeMappingDto> attributes = attributeMappingService.find(filter, null).getContent();
		assertEquals(1, attributes.size());
		SysSystemAttributeMappingDto ldapGroupsAttribute = attributes.stream().findFirst().get();

		// Creates cross-domain group.
		SysSystemGroupDto groupSystemDto = new SysSystemGroupDto();
		groupSystemDto.setCode(getHelper().createName());
		groupSystemDto.setType(SystemGroupType.CROSS_DOMAIN);
		groupSystemDto = systemGroupService.save(groupSystemDto);

		SysSystemGroupSystemDto systemGroupSystemOne = new SysSystemGroupSystemDto();
		systemGroupSystemOne.setSystemGroup(groupSystemDto.getId());
		systemGroupSystemOne.setMergeAttribute(ldapGroupsAttribute.getId());
		systemGroupSystemOne.setSystem(systemDto.getId());
		systemGroupSystemService.save(systemGroupSystemOne);

		// Creates the login role.
		IdmRoleDto loginRole = helper.createRole();
		helper.createRoleSystem(loginRole, systemDto);
		
		// Creates no-login role.
		IdmRoleDto noLoginRole = helper.createRole();
		SysRoleSystemDto roleSystem = helper.createRoleSystem(noLoginRole, systemDto);
		roleSystem.setCreateAccountByDefault(true);
		roleSystemService.save(roleSystem);

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setIsInCrossDomainGroupRoleId(noLoginRole.getId());
		roleSystemFilter.setCheckIfIsInCrossDomainGroup(Boolean.TRUE);
		roleSystemFilter.setId(roleSystem.getId());
		List<SysRoleSystemDto> roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(0, roleSystemDtos.size());

		// Creates overridden ldapGroup merge attribute.
		createOverriddenLdapGroupAttribute(ldapGroupsAttribute, roleSystem);

		// Role-system should be in cross-domain group now.
		roleSystemDtos = roleSystemService.find(roleSystemFilter, null).getContent();
		assertEquals(1, roleSystemDtos.size());
		SysRoleSystemDto roleSystemDto = roleSystemDtos.stream().findFirst().get();
		assertTrue(roleSystemDto.isInCrossDomainGroup());
		
		IdmIdentityDto identity = getHelper().createIdentity();
		IdmIdentityContractDto contract = getHelper().getPrimeContract(identity.getId());
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setIdentityId(identity.getId());
		identityRoleFilter.setRoleId(noLoginRole.getId());
		assertEquals(0, identityRoleService.count(identityRoleFilter));

		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(identity.getId());
		identityAccountFilter.setSystemId(systemDto.getId());
		assertEquals(0, identityAccountService.find(identityAccountFilter, null).getContent().size());

		IdmRoleRequestDto roleRequestDto = getHelper().assignRoles(contract, false, loginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());
		assertEquals(1, identityAccountService.find(identityAccountFilter, null).getContent().size());

		// Check if provisioning NOT contains ldapGroups attribute with value ('ONE') from the role.
		SysProvisioningOperationFilter provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		List<SysProvisioningOperationDto> provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		SysProvisioningOperationDto provisioningOperationDto = provisioningOperationDtos.stream().findFirst().get();
		ProvisioningAttributeDto provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		Object ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals(0, ((List<?>) ldapGroupsValue).size());

		// Delete old provisioning.
		provisioningOperationService.delete(provisioningOperationDto);

		// Assign no-login role.
		roleRequestDto = getHelper().assignRoles(contract, false, noLoginRole);
		assertEquals(RoleRequestState.EXECUTED, roleRequestDto.getState());
		assertNotNull(roleRequestDto.getSystemState());

		// Check if provisioning contains ldapGroups attribute with value ('ONE') from the role.
		provisioningOperationFilter = new SysProvisioningOperationFilter();
		provisioningOperationFilter.setSystemId(systemDto.getId());
		provisioningOperationFilter.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		provisioningOperationFilter.setEntityIdentifier(identity.getId());
		provisioningOperationDtos = provisioningOperationService.find(provisioningOperationFilter, null).getContent();
		assertEquals(1, provisioningOperationDtos.size());
		provisioningOperationDto = provisioningOperationDtos
				.stream()
				.max(Comparator.comparing(SysProvisioningOperationDto::getCreated))
				.get();
		provisioningAttributeLdapGroupsDto = provisioningOperationDto.getProvisioningContext().getAccountObject().keySet()
				.stream()
				.filter(provisioningAtt -> MockCrossDomainAdUserConnectorType.LDAP_GROUPS_ATTRIBUTE.equals(provisioningAtt.getSchemaAttributeName()))
				.findFirst()
				.get();

		assertNotNull(provisioningAttributeLdapGroupsDto);
		ldapGroupsValue = provisioningOperationDto.getProvisioningContext().getAccountObject().get(provisioningAttributeLdapGroupsDto);
		assertEquals("ONE", ((List<?>) ldapGroupsValue).get(0));

		assertEquals(1, identityRoleService.count(identityRoleFilter));

		// Clean
		provisioningOperationService.deleteOperations(systemDto.getId());
		getHelper().deleteIdentity(identity.getId());
		getHelper().deleteRole(noLoginRole.getId());
	}

	public interface GetConnectorObjectCallback {
		IcConnectorObject call(SysSystemDto system, String uid, IcObjectClass objectClass);
	}

	private SysSystemDto initSystem(ConnectorType connectorType) {
		ConnectorTypeDto connectorTypeDto = connectorManager.convertTypeToDto(connectorType);
		SysSystemDto systemDto = createSystem(connectorType.getId() + "_" + this.getHelper().createName(), connectorTypeDto);
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.SYSTEM_DTO_KEY, systemDto.getId().toString());

		String newUserContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.NEW_USER_CONTAINER_KEY, newUserContainerMock);
		String userContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.USER_SEARCH_CONTAINER_KEY, userContainerMock);
		String deletedUserContainerMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.DELETE_USER_CONTAINER_KEY, deletedUserContainerMock);
		String domainMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.DOMAIN_KEY, domainMock);
		String defaultRoleMock = this.getHelper().createName();
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.NEW_ROLE_WITH_SYSTEM_CODE, defaultRoleMock);
		connectorTypeDto.setWizardStepName(MockCrossDomainAdUserConnectorType.STEP_FOUR);
		// Activate pairing sync.
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.PAIRING_SYNC_SWITCH_KEY, "true");
		// Activate protected sync.
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.PROTECTED_MODE_SWITCH_KEY, "true");

		// Generate mock schema.
		generateMockSchema(systemDto);
		//  Execute step four.
		connectorManager.execute(connectorTypeDto);
		return systemDto;
	}

	private void generateMockSchema(SysSystemDto systemDto) {
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
	}

	private SysSystemDto createSystem(String systemName, ConnectorTypeDto connectorTypeDto) {

		connectorTypeDto.setReopened(false);
		connectorManager.load(connectorTypeDto);
		assertNotNull(connectorTypeDto);

		String fakeHost = this.getHelper().createName();

		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.HOST, fakeHost);
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.PORT, "636");
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.USER, fakeHost);
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.PASSWORD, fakeHost);
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.SSL_SWITCH, "false");
		connectorTypeDto.getMetadata().put(MockCrossDomainAdUserConnectorType.SYSTEM_NAME, systemName);
		connectorTypeDto.setWizardStepName(MockCrossDomainAdUserConnectorType.STEP_ONE);

		// Execute the first step.
		ConnectorTypeDto stepExecutedResult = connectorManager.execute(connectorTypeDto);
		BaseDto systemDto = stepExecutedResult.getEmbedded().get(MockCrossDomainAdUserConnectorType.SYSTEM_DTO_KEY);
		assertNotNull("System ID cannot be null!", systemDto);
		SysSystemDto system = systemService.get(systemDto.getId());
		assertNotNull(system);
		return system;
	}

}
