package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormAttributeFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete form attributes integration test.
 * 
 * @author Radek Tomiška
 *
 */
public class FormAttributeDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {

	@Autowired private IdmFormAttributeService service;
	@Autowired private FormService formService;
	
	@Before
	public void login() {
		loginAsAdmin();
	}
	
	@After
	public void logout() {
		super.logout();
	}
	
	@Test
	public void testProcessBulkActionByIds() {
		List<IdmFormAttributeDto> attributes = createAttributes(5);
		
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormAttribute.class, FormAttributeDeleteBulkAction.NAME);
		
		Set<UUID> ids = this.getIdFromList(attributes);
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		
		checkResultLrt(processAction, 5l, null, null);
		
		for (UUID id : ids) {
			Assert.assertNull(service.get(id));
		}
	}
	
	@Test
	public void testDeleteWithFilledValues() {
		IdmIdentityDto owner = getHelper().createIdentity((GuardedString) null);
		//
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setCode(getHelper().createName());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(
				IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attributeDefinitionOne)
		);
		attributeDefinitionOne = formDefinitionOne.getMappedAttributeByCode(attributeDefinitionOne.getCode());
		//
		IdmFormValueDto value = new IdmFormValueDto(attributeDefinitionOne);
		value.setValue(getHelper().createName());
		formService.saveValues(owner, formDefinitionOne, Lists.newArrayList(value));
		Assert.assertEquals(value.getStringValue(), formService.getValues(owner, formDefinitionOne).get(0).getStringValue());
		//
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormAttribute.class, FormAttributeDeleteBulkAction.NAME);
		Set<UUID> ids = Sets.newHashSet(attributeDefinitionOne.getId());
		bulkAction.setIdentifiers(ids);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		// without force
		checkResultLrt(processAction, null, 1l, null);
		Assert.assertEquals(value.getStringValue(), formService.getValues(owner, formDefinitionOne).get(0).getStringValue());
		// with force
		Map<String, Object> properties = new HashMap<>();
		properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
		// delete by bulk action
		bulkAction = findBulkAction(IdmFormAttribute.class, FormAttributeDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(ids);
		bulkAction.setProperties(properties);
		//
		processAction = bulkActionManager.processAction(bulkAction);
		// with force
		checkResultLrt(processAction, 1l, null, null);
		Assert.assertTrue(formService.getValues(owner, formDefinitionOne).isEmpty());
		//
		formService.deleteDefinition(formDefinitionOne);
		getHelper().deleteIdentity(owner.getId());
	}
	
	@Test
	public void testProcessBulkActionByFilter() {
		List<IdmFormAttributeDto> attributes = createAttributes(5);
		
		IdmFormAttributeFilter filter = new IdmFormAttributeFilter();
		filter.setId(attributes.get(2).getId());

		List<IdmFormAttributeDto> checkAttributes = service.find(filter, null).getContent();
		Assert.assertEquals(1, checkAttributes.size());

		IdmBulkActionDto bulkAction = findBulkAction(IdmFormAttribute.class, FormAttributeDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, 1l, null, null);
	
		Assert.assertNull(service.get(attributes.get(2)));
		Assert.assertNotNull(service.get(attributes.get(1)));
		Assert.assertNotNull(service.get(attributes.get(3)));
	}
	
	private List<IdmFormAttributeDto> createAttributes(int count) {
		List<IdmFormAttributeDto> results = new ArrayList<>();
		IdmFormDefinitionDto formDefinition = formService.createDefinition(IdmRoleDto.class, getHelper().createName(), null);
		//
		for (int i = 0; i < count; i++) {
			IdmFormAttributeDto attribute = new IdmFormAttributeDto(getHelper().createName());
			attribute.setFormDefinition(formDefinition.getId());
			results.add(service.save(attribute));
		}
		//
		return results;
	}
}
