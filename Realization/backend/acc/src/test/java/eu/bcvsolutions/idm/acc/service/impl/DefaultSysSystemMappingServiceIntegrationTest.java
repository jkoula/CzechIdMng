package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmTreeTypeService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Searching entities, using filters.
 *
 * @author Petr Hanák
 * @author Vít Švanda
 * @author Roman Kucera
 */
@Transactional
public class DefaultSysSystemMappingServiceIntegrationTest extends AbstractIntegrationTest {

	@Autowired
	private SysSystemMappingService mappingService;
	@Autowired
	private SysSystemAttributeMappingService mappingAttributeService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private IdmTreeTypeService treeTypeService;
	@Autowired
	private TestHelper testHelper;

	@Test
	public void textFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		mappingSystem1.setName("SomeName01");
		mappingService.save(mappingSystem1);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		mappingSystem2.setName("SomeName02");
		mappingService.save(mappingSystem2);
		SysSystemMappingDto mappingSystem3 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		mappingSystem3.setName("SomeName22");
		mappingService.save(mappingSystem3);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setText("SomeName0");

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertTrue(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void typeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto mappingSystem3 = testHelper.createMappingSystem(TreeSynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		filter.setEntityType(ContractSynchronizationExecutor.SYSTEM_ENTITY_TYPE);

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertTrue(result.getContent().contains(mappingSystem2));
		assertFalse(result.getContent().contains(mappingSystem3));
	}

	@Test
	public void operationTypeFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		createProvisioningMappingSystem(TreeSynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto mappingSystem3 = createProvisioningMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setOperationType(SystemOperationType.PROVISIONING);
		filter.setSystemId(system.getId());

		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(2, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem3));
		assertFalse(result.getContent().contains(mappingSystem1));
	}

	@Test
	public void systemIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSystemDto system2 = createSystem();
		SysSchemaObjectClassDto objectClass2 = createObjectClass(system2);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass2);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertFalse(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void objectClassFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		SysSystemDto system = createSystem();
		SysSystemDto system2 = createSystem();

		SysSchemaObjectClassDto objectClass = createObjectClass(system);
		SysSchemaObjectClassDto objectClass2 = createObjectClass(system2);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass2);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setObjectClassId(mappingSystem1.getObjectClass());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertFalse(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void treeTypeIdFilterTest() {
		IdmBasePermission permission = IdmBasePermission.ADMIN;

		IdmTreeTypeDto treeType = new IdmTreeTypeDto();
		treeType.setName("SomeTreeTypeName");
		treeType.setCode("CodeCodeCodeCode");
		treeType = treeTypeService.save(treeType);

		IdmTreeTypeDto treeType2 = new IdmTreeTypeDto();
		treeType2.setName("SomeTreeTypeName2");
		treeType2.setCode("CodeCodeCodeCode2");
		treeType2 = treeTypeService.save(treeType2);

		SysSystemDto system = createSystem();
		SysSchemaObjectClassDto objectClass = createObjectClass(system);

		SysSystemMappingDto mappingSystem1 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		mappingSystem1.setTreeType(treeType.getId());
		mappingSystem1 = mappingService.save(mappingSystem1);
		SysSystemMappingDto mappingSystem2 = testHelper.createMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, objectClass);
		mappingSystem2.setTreeType(treeType2.getId());
		mappingSystem2 = mappingService.save(mappingSystem2);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setTreeTypeId(mappingSystem1.getTreeType());
		Page<SysSystemMappingDto> result = mappingService.find(filter, null, permission);
		assertEquals(1, result.getTotalElements());
		assertTrue(result.getContent().contains(mappingSystem1));
		assertFalse(result.getContent().contains(mappingSystem2));
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributesDisabled() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("first_name", schema);
		createSchemaAttribute("surname", schema); // redundant to lastname
		createSchemaAttribute("lastname", schema);
		createSchemaAttribute("__UID__", schema); // redundant to __NAME__
		createSchemaAttribute("email", schema);
		createSchemaAttribute("titleBefore", schema);
		createSchemaAttribute("title_after", schema);


		SysSystemMappingDto mappingDto = this.createProvisioningMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, schema);
		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is disabled by default.
		assertEquals(0, mappingAttributes.size());
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributes() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("first_name", schema);
		createSchemaAttribute("surname", schema); // redundant to lastname
		createSchemaAttribute("lastname", schema);
		createSchemaAttribute("__UID__", schema); // redundant to __NAME__
		createSchemaAttribute("email", schema);
		createSchemaAttribute("titleBefore", schema);
		createSchemaAttribute("title_after", schema);
		createSchemaAttribute("not_exist", schema);
		createPasswordSchemaAttribute("__PASSWORD__", schema);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName(testHelper.createName());
		mappingDto.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingDto.setObjectClass(schema.getId());
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);
		mappingDto.setAccountType(AccountType.PERSONAL);
		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is enabled.
		assertEquals(7, mappingAttributes.size());

		SysSystemAttributeMappingDto usernameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__NAME__"))
				.findFirst()
				.orElse(null);

		assertNotNull(usernameAttribute);
		assertTrue(usernameAttribute.isUid());
		assertEquals(IdmIdentity_.username.getName(), usernameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto lastnameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("lastname"))
				.findFirst()
				.orElse(null);

		assertNotNull(lastnameAttribute);
		assertFalse(lastnameAttribute.isUid());
		assertEquals(IdmIdentity_.lastName.getName(), lastnameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto firstNameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("first_name"))
				.findFirst()
				.orElse(null);

		assertNotNull(firstNameAttribute);
		assertFalse(firstNameAttribute.isUid());
		assertEquals(IdmIdentity_.firstName.getName(), firstNameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto titleBeforeAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("titleBefore"))
				.findFirst()
				.orElse(null);

		assertNotNull(titleBeforeAttribute);
		assertFalse(titleBeforeAttribute.isUid());
		assertEquals(IdmIdentity_.titleBefore.getName(), titleBeforeAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto titleAfterAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("title_after"))
				.findFirst()
				.orElse(null);

		assertNotNull(titleAfterAttribute);
		assertFalse(titleAfterAttribute.isUid());
		assertEquals(IdmIdentity_.titleAfter.getName(), titleAfterAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto emailAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("email"))
				.findFirst()
				.orElse(null);

		assertNotNull(emailAttribute);
		assertFalse(emailAttribute.isUid());
		assertEquals(IdmIdentity_.email.getName(), emailAttribute.getIdmPropertyName());
		
		SysSystemAttributeMappingDto passwordAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__PASSWORD__"))
				.findFirst()
				.orElse(null);
		assertNotNull(passwordAttribute);
		assertFalse(passwordAttribute.isUid());
		assertTrue(passwordAttribute.isPasswordAttribute());
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributesTree() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("parent", schema);
		createSchemaAttribute("name", schema);
		createSchemaAttribute("code", schema); // redundant to __NAME__
		createSchemaAttribute("description", schema);
		createSchemaAttribute("not_exist", schema);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName(testHelper.createName());
		mappingDto.setEntityType(TreeSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingDto.setObjectClass(schema.getId());
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);
		mappingDto.setAccountType(AccountType.PERSONAL);
		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is enabled.
		assertEquals(3, mappingAttributes.size());

		SysSystemAttributeMappingDto primaryAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__NAME__"))
				.findFirst()
				.orElse(null);

		assertNotNull(primaryAttribute);
		assertTrue(primaryAttribute.isUid());
		assertEquals(IdmTreeNode_.code.getName(), primaryAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto nameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("name"))
				.findFirst()
				.orElse(null);

		assertNotNull(nameAttribute);
		assertFalse(nameAttribute.isUid());
		assertEquals(IdmTreeNode_.name.getName(), nameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto parentAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("parent"))
				.findFirst()
				.orElse(null);

		assertNotNull(parentAttribute);
		assertFalse(parentAttribute.isUid());
		assertEquals(IdmTreeNode_.parent.getName(), parentAttribute.getIdmPropertyName());
	}

	@Test
	public void testAutomaticGenerateOfMappedAttributesRoleCatalogue() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		createSchemaAttribute("__NAME__", schema);
		createSchemaAttribute("parent", schema);
		createSchemaAttribute("name", schema);
		createSchemaAttribute("code", schema); // redundant to __NAME__
		createSchemaAttribute("description", schema);
		createSchemaAttribute("not_exist", schema);

		SysSystemMappingDto mappingDto = new SysSystemMappingDto();
		mappingDto.setName(testHelper.createName());
		mappingDto.setEntityType(RoleCatalogueSynchronizationExecutor.SYSTEM_ENTITY_TYPE);
		mappingDto.setObjectClass(schema.getId());
		mappingDto.setOperationType(SystemOperationType.PROVISIONING);
		mappingDto.setAccountType(AccountType.PERSONAL);
		mappingDto = mappingService.publish(
				new SystemMappingEvent(
						SystemMappingEvent.SystemMappingEventType.CREATE,
						mappingDto,
						ImmutableMap.of(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING, true)))
				.getContent();

		SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
		attributeMappingFilter.setSystemMappingId(mappingDto.getId());

		List<SysSystemAttributeMappingDto> mappingAttributes = mappingAttributeService.find(attributeMappingFilter, null).getContent();
		// Automatic attribute generating is enabled.
		assertEquals(4, mappingAttributes.size());

		SysSystemAttributeMappingDto primaryAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("__NAME__"))
				.findFirst()
				.orElse(null);

		assertNotNull(primaryAttribute);
		assertTrue(primaryAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.code.getName(), primaryAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto nameAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("name"))
				.findFirst()
				.orElse(null);

		assertNotNull(nameAttribute);
		assertFalse(nameAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.name.getName(), nameAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto parentAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("parent"))
				.findFirst()
				.orElse(null);

		assertNotNull(parentAttribute);
		assertFalse(parentAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.parent.getName(), parentAttribute.getIdmPropertyName());

		SysSystemAttributeMappingDto descriptionAttribute = mappingAttributes
				.stream()
				.filter(attribute -> attribute.getName().equals("description"))
				.findFirst()
				.orElse(null);

		assertNotNull(descriptionAttribute);
		assertFalse(descriptionAttribute.isUid());
		assertEquals(IdmRoleCatalogue_.description.getName(), descriptionAttribute.getIdmPropertyName());
	}
	
	@Test
	public void testIdentityStateAndDisabledWArning() {
		SysSystemDto system = testHelper.createSystem(testHelper.createName());
		SysSchemaObjectClassDto schema = this.createObjectClass(system);

		SysSchemaAttributeDto nameSchemaAttr = createSchemaAttribute("name", schema);
		SysSchemaAttributeDto stateSchemaAttr = createSchemaAttribute("state", schema);
		SysSchemaAttributeDto disabledSchemaAttr = createSchemaAttribute("disabled", schema);
		SysSystemMappingDto mappingDto = this.createProvisioningMappingSystem(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, schema);

		SysSystemAttributeMappingDto nameAttrMap = new SysSystemAttributeMappingDto();
		nameAttrMap.setUid(true);
		nameAttrMap.setEntityAttribute(true);
		nameAttrMap.setSystemMapping(mappingDto.getId());
		nameAttrMap.setIdmPropertyName("username");
		nameAttrMap.setName(nameSchemaAttr.getName());
		nameAttrMap.setSchemaAttribute(nameSchemaAttr.getId());
		nameAttrMap = mappingAttributeService.save(nameAttrMap);

		SysSystemAttributeMappingDto stateAttrMap = new SysSystemAttributeMappingDto();
		stateAttrMap.setUid(false);
		stateAttrMap.setEntityAttribute(true);
		stateAttrMap.setSystemMapping(mappingDto.getId());
		stateAttrMap.setIdmPropertyName("state");
		stateAttrMap.setName(stateSchemaAttr.getName());
		stateAttrMap.setSchemaAttribute(stateSchemaAttr.getId());
		stateAttrMap = mappingAttributeService.save(stateAttrMap);
		// SHOULD NOT THROW only state attribute mapped
		mappingService.validate(mappingDto.getId());

		SysSystemAttributeMappingDto disabledAttrMap = new SysSystemAttributeMappingDto();
		disabledAttrMap.setUid(false);
		disabledAttrMap.setEntityAttribute(true);
		disabledAttrMap.setSystemMapping(mappingDto.getId());
		disabledAttrMap.setIdmPropertyName("disabled");
		disabledAttrMap.setName(disabledSchemaAttr.getName());
		disabledAttrMap.setSchemaAttribute(disabledSchemaAttr.getId());
		disabledAttrMap = mappingAttributeService.save(disabledAttrMap);

		// both state and disabled attributes are mapped SHOULD THROW
		try {
			mappingService.validate(mappingDto.getId());
			fail("Should throw");
		} catch (ResultCodeException e) {
			Assert.assertTrue(e.getError().getError().getParameters()
					.containsKey(DefaultSysSystemMappingService.IDENTITY_STATE_USED_WITH_DISABLED));
		}

		mappingAttributeService.delete(stateAttrMap);
		// SHOULD NOT THROW only disabled attribute mapped
		mappingService.validate(mappingDto.getId());
	}

	@Test
	public void testConnectedMapping() {
		SysSystemDto system = testHelper.createTestResourceSystem(false, testHelper.createName());
		SysSystemMappingDto syncMapping = testHelper.createMapping(system);
		SysSystemMappingDto provMapping = testHelper.createMapping(system);
		SysSystemMappingDto provMapping1 = testHelper.createMapping(system);

		syncMapping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncMapping = mappingService.save(syncMapping);
		assertEquals(SystemOperationType.SYNCHRONIZATION , syncMapping.getOperationType());

		// connected mapping is saved
		provMapping.setConnectedSystemMappingId(syncMapping.getId());
		provMapping = mappingService.save(provMapping);
		assertEquals(syncMapping.getId(), provMapping.getConnectedSystemMappingId());

		// connected mapping is saved from the other side
		syncMapping = mappingService.get(syncMapping.getId());
		assertEquals(provMapping.getId(), syncMapping.getConnectedSystemMappingId());

		// other mapping is unchanged
		provMapping1 = mappingService.get(provMapping1.getId());
		assertNull(provMapping1.getConnectedSystemMappingId());

		// change connected mapping to other provisioning mapping
		syncMapping.setConnectedSystemMappingId(provMapping1.getId());
		syncMapping = mappingService.save(syncMapping);
		assertEquals(provMapping1.getId(), syncMapping.getConnectedSystemMappingId());

		// previous connected mapping is now null
		provMapping = mappingService.get(provMapping.getId());
		assertNull(provMapping.getConnectedSystemMappingId());

		// new connected mapping has the relation saved
		provMapping1 = mappingService.get(provMapping1.getId());
		assertEquals(syncMapping.getId(), provMapping1.getConnectedSystemMappingId());

		// delete mapping
		mappingService.delete(syncMapping);
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setId(syncMapping.getId());
		long count = mappingService.count(mappingFilter);
		assertEquals(0, count);

		// connected mapping has null relation now
		provMapping1 = mappingService.get(provMapping1.getId());
		assertNull(provMapping1.getConnectedSystemMappingId());

		// previous connected mapping is still null
		provMapping = mappingService.get(provMapping.getId());
		assertNull(provMapping.getConnectedSystemMappingId());
	}

	@Test(expected = ResultCodeException.class)
	public void testConnectedMappingAlreadyMapped() {
		SysSystemDto system = testHelper.createTestResourceSystem(false, testHelper.createName());
		SysSystemMappingDto syncMappping = testHelper.createMapping(system);
		SysSystemMappingDto provMapping = testHelper.createMapping(system);
		SysSystemMappingDto provMapping1 = testHelper.createMapping(system);

		syncMappping.setOperationType(SystemOperationType.SYNCHRONIZATION);
		syncMappping = mappingService.save(syncMappping);
		assertEquals(SystemOperationType.SYNCHRONIZATION , syncMappping.getOperationType());

		// connected mapping is saved
		provMapping.setConnectedSystemMappingId(syncMappping.getId());
		provMapping = mappingService.save(provMapping);
		assertEquals(syncMappping.getId(), provMapping.getConnectedSystemMappingId());

		// save same connected mapping to other mapping should throw exception
		provMapping1.setConnectedSystemMappingId(syncMappping.getId());
		mappingService.save(provMapping1);
	}

	private SysSchemaAttributeDto createSchemaAttribute(String name, SysSchemaObjectClassDto schema) {
		SysSchemaAttributeDto attributeDto = new SysSchemaAttributeDto();
		attributeDto.setObjectClass(schema.getId());
		attributeDto.setName(name);
		attributeDto.setNativeName(name);
		attributeDto.setClassType(String.class.getCanonicalName());
		attributeDto.setMultivalued(false);

		return schemaAttributeService.save(attributeDto);
	}
	
	private SysSchemaAttributeDto createPasswordSchemaAttribute (String name, SysSchemaObjectClassDto schema) {
		SysSchemaAttributeDto attributeDto = createSchemaAttribute(name, schema);
		attributeDto.setClassType(GuardedString.class.getCanonicalName());
		return schemaAttributeService.save(attributeDto);
	}

	private SysSystemDto createSystem() {
		SysSystemDto system = new SysSystemDto();
		system.setName("system_" + UUID.randomUUID());
		return systemService.save(system);
	}

	private SysSchemaObjectClassDto createObjectClass(SysSystemDto system) {
		SysSchemaObjectClassDto objectClass = new SysSchemaObjectClassDto();
		objectClass.setSystem(system.getId());
		objectClass.setObjectClassName("__ACCOUNT__");
		return schemaObjectClassService.save(objectClass);
	}

	private SysSystemMappingDto createProvisioningMappingSystem(String type, SysSchemaObjectClassDto objectClass) {
		// system mapping
		SysSystemMappingDto mapping = new SysSystemMappingDto();
		mapping.setName("Name" + UUID.randomUUID());
		mapping.setEntityType(type);
		mapping.setObjectClass(objectClass.getId());
		mapping.setOperationType(SystemOperationType.PROVISIONING);
		mapping.setAccountType(AccountType.PERSONAL);
		return mappingService.save(mapping);
	}
}
