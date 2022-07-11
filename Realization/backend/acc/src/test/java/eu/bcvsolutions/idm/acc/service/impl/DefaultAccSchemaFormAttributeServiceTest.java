package eu.bcvsolutions.idm.acc.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.acc.TestHelper;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

public class DefaultAccSchemaFormAttributeServiceTest extends AbstractIntegrationTest {

	@Autowired
	private TestHelper helper;
	@Autowired
	private IdmFormDefinitionService formDefinitionService;
	@Autowired
	private IdmFormAttributeService formAttributeService;
	@Autowired
	private SysSystemService systemService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private AccAccountService accountService;
	@Autowired
	private FormService formService;
	@Autowired
	private AccSchemaFormAttributeService schemaFormAttributeService;
	@Autowired
	private SysProvisioningOperationService provisioningService;
	
	@Test
	public void testFormDefinitionCreationOnSchemaCreation() {
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setType(AccAccount.class.getCanonicalName());
		List<IdmFormDefinitionDto> formDefinitions = formDefinitionService.find(formDefinitionFilter, null).getContent();
		assertTrue(formDefinitions.isEmpty());
		
		SysSystemDto system = helper.createSystem("test_resource");
		// we're generating the schema now so the definition should be created
		helper.createMapping(system);
		formDefinitions = formDefinitionService.find(formDefinitionFilter, null).getContent();
		assertFalse(formDefinitions.isEmpty());
		assertEquals(1, formDefinitions.size());
		formDefinitionService.delete(formDefinitions.get(0));
		systemService.delete(system);
	}
	
	@Test
	public void testFormAttributeCreationOnSchemaCreation() {
		SysSystemDto system = helper.createSystem("test_resource");
		// we're generating the schema now so the definition should be created
		helper.createMapping(system);
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setType(AccAccount.class.getCanonicalName());
		IdmFormDefinitionDto formDefinition = formDefinitionService.find(formDefinitionFilter, null).stream().findFirst().orElse(null);
		IdmFormAttributeFilter formAttributeFilter = new IdmFormAttributeFilter();
		formAttributeFilter.setDefinitionId(formDefinition.getId());
		List<IdmFormAttributeDto> formAttributes = formAttributeService.find(formAttributeFilter, null).getContent();
		assertFalse(formAttributes.isEmpty());
		// check each type of attributes
		IdmFormAttributeDto firstName = formAttributes.stream().filter(attr -> attr.getCode().equals("FIRSTNAME")).findFirst().orElse(null);
		assertNotNull(firstName);
		assertEquals(PersistentType.SHORTTEXT, firstName.getPersistentType());
		assertFalse(firstName.isConfidential());
		//
		IdmFormAttributeDto password = formAttributes.stream().filter(attr -> attr.getCode().equals("__PASSWORD__")).findFirst().orElse(null);
		assertNotNull(password);
		assertEquals(PersistentType.SHORTTEXT, firstName.getPersistentType());
		assertTrue(password.isConfidential());
		//
		IdmFormAttributeDto enable = formAttributes.stream().filter(attr -> attr.getCode().equals("__ENABLE__")).findFirst().orElse(null);
		assertNotNull(enable);
		assertEquals(PersistentType.SHORTTEXT, enable.getPersistentType()); // this is shorttext in the schema
		//
		formDefinitionService.delete(formDefinition);
		systemService.delete(system);
	}
	
	@Test
	public void testUpdateFormAttributeOnSchemaChange() {
		SysSystemDto system = helper.createSystem("test_resource");
		// we're generating the schema now so the definition should be created
		helper.createMapping(system);
		IdmFormDefinitionFilter formDefinitionFilter = new IdmFormDefinitionFilter();
		formDefinitionFilter.setType(AccAccount.class.getCanonicalName());
		IdmFormDefinitionDto formDefinition = formDefinitionService.find(formDefinitionFilter, null).stream().findFirst().orElse(null);
		IdmFormAttributeFilter formAttributeFilter = new IdmFormAttributeFilter();
		formAttributeFilter.setDefinitionId(formDefinition.getId());
		List<IdmFormAttributeDto> formAttributes = formAttributeService.find(formAttributeFilter, null).getContent();
		assertFalse(formAttributes.isEmpty());
		//
		IdmFormAttributeDto enableFormAttribute = formAttributes.stream().filter(attr -> attr.getCode().equals("__ENABLE__")).findFirst().orElse(null);
		assertNotNull(enableFormAttribute);
		assertEquals(PersistentType.SHORTTEXT, enableFormAttribute.getPersistentType()); // this is shorttext in the schema
		//
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setSystemId(system.getId());
		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
		SysSchemaAttributeDto enableSchemaAttribute = schemaAttributes.stream().filter(sa -> sa.getName().equals("__ENABLE__")).findFirst().orElse(null);
		enableSchemaAttribute.setClassType(Boolean.class.getCanonicalName());
		schemaAttributeService.save(enableSchemaAttribute);
		//
		formAttributes = formAttributeService.find(formAttributeFilter, null).getContent();
		assertFalse(formAttributes.isEmpty());
		//
		enableFormAttribute = formAttributes.stream().filter(attr -> attr.getCode().equals("__ENABLE__")).findFirst().orElse(null);
		assertNotNull(enableFormAttribute);
		assertEquals(PersistentType.BOOLEAN, enableFormAttribute.getPersistentType()); // this should now be boolean
		//
		formDefinitionService.delete(formDefinition);
		systemService.delete(system);
	}
	
	@Test
	public void testCreateAccountFormValue() {
		// create system
		SysSystemDto system = helper.createSystem("test_resource");
		helper.createMapping(system);
		IdmRoleDto role = helper.createRole();
		helper.createRoleSystem(role, system);
		//
		// create identity and its account
		IdmIdentityDto identity = helper.createIdentity();
		helper.createIdentityRole(identity, role);
		AccAccountFilter accountFilter = new AccAccountFilter();
		accountFilter.setIdentityId(identity.getId());
		accountFilter.setSystemId(system.getId());
		AccAccountDto account = accountService.find(accountFilter, null).stream().findFirst().orElse(null);
		//
		// find the attribute
		IdmFormDefinitionDto formDefinition = DtoUtils.getEmbedded(account, AccAccount_.formDefinition, IdmFormDefinitionDto.class);
		IdmFormAttributeFilter formAttributeFilter = new IdmFormAttributeFilter();
		formAttributeFilter.setDefinitionId(formDefinition.getId());
		List<IdmFormAttributeDto> formAttributes = formAttributeService.find(formAttributeFilter, null).getContent();
		IdmFormAttributeDto firstNameAttribute = formAttributes.stream().filter(attr -> attr.getCode().equals("FIRSTNAME")).findFirst().orElse(null);
		//
		// set EAV value
		helper.setEavValue(account, firstNameAttribute, AccAccount.class, "test", PersistentType.SHORTTEXT, formDefinition);

		List<IdmFormValueDto> values = formService.getValues(account, formDefinition);
		assertEquals(1, values.size());
		IdmFormValueDto formValue = values.get(0);
		assertEquals(firstNameAttribute.getId(), formValue.getFormAttribute());
		assertEquals("test", formValue.getShortTextValue());
		//
		formService.deleteValue(formValue);
		formDefinitionService.delete(formDefinition);
//		systemService.delete(system);
	}
}
