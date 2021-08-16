package eu.bcvsolutions.idm.core.bulk.action.impl.eav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormDefinitionFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractBulkActionTest;

/**
 * Delete form definition
 * - by id / filter
 *
 * 
 * @author Ondrej Husnik
 *
 */
public class FormDefinitionDeleteBulkActionIntegrationTest extends AbstractBulkActionTest {
	
	private final int TEST_COUNT = 10;
	
	@Autowired private IdmFormDefinitionService formDefService;
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
	public void processBulkActionByIds() {
		String type = getHelper().createName();
		getHelper().createFormDefinition(type, true); // creates Main definition which won't be deleted
		List<IdmFormDefinitionDto> defDtos = new ArrayList<IdmFormDefinitionDto>();
		for (int i = 0; i < TEST_COUNT; ++i) {
			getHelper().createFormDefinition(type);
		}

		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setType(type);
		filter.setMain(false);
		defDtos = formDefService.find(filter, null).getContent();

		Set<UUID> defIds = defDtos.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		Assert.assertTrue(defIds.size() == TEST_COUNT);

		IdmBulkActionDto bulkAction = findBulkAction(IdmFormDefinition.class, FormDefinitionDeleteBulkAction.NAME);
		bulkAction.setIdentifiers(defIds);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, Long.valueOf(TEST_COUNT), null, null);

		filter.setMain(null);
		List<IdmFormDefinitionDto> defsRemain = formDefService.find(filter, null).getContent();
		Assert.assertEquals(1, defsRemain.size());

		Set<UUID> result = defsRemain.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		result.retainAll(defIds);
		Assert.assertTrue(result.isEmpty());
	}
	
	@Test
	public void processBulkActionByFilter() {
		String type = getHelper().createName();
		getHelper().createFormDefinition(type, true); // creates Main definition which won't be deleted
		List<IdmFormDefinitionDto> defDtos = new ArrayList<IdmFormDefinitionDto>();
		for (int i = 0; i < TEST_COUNT; ++i) {
			getHelper().createFormDefinition(type);
		}

		IdmFormDefinitionFilter filter = new IdmFormDefinitionFilter();
		filter.setType(type);
		filter.setMain(false);
		defDtos = formDefService.find(filter, null).getContent();

		Set<UUID> defIds = defDtos.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		Assert.assertTrue(defIds.size() == TEST_COUNT);

		IdmBulkActionDto bulkAction = findBulkAction(IdmFormDefinition.class, FormDefinitionDeleteBulkAction.NAME);
		bulkAction.setTransformedFilter(filter);
		bulkAction.setFilter(toMap(filter));
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		checkResultLrt(processAction, Long.valueOf(TEST_COUNT), null, null);

		filter.setMain(null);
		List<IdmFormDefinitionDto> defsRemain = formDefService.find(filter, null).getContent();
		Assert.assertEquals(1, defsRemain.size());

		Set<UUID> result = defsRemain.stream().map(IdmFormDefinitionDto::getId).collect(Collectors.toSet());
		result.retainAll(defIds);
		Assert.assertTrue(result.isEmpty());
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
		Map<String, Object> properties = new HashMap<>();
		properties.put(EntityEventProcessor.PROPERTY_FORCE_DELETE, Boolean.TRUE);
		IdmBulkActionDto bulkAction = findBulkAction(IdmFormDefinition.class, FormDefinitionDeleteBulkAction.NAME);
		Set<UUID> ids = Sets.newHashSet(formDefinitionOne.getId());
		bulkAction.setIdentifiers(ids);
		bulkAction.setProperties(properties);
		IdmBulkActionDto processAction = bulkActionManager.processAction(bulkAction);
		//
		// with force
		checkResultLrt(processAction, 1l, null, null);
		Assert.assertTrue(formService.getValues(owner, formDefinitionOne).isEmpty());
		//
		getHelper().deleteIdentity(owner.getId());
	}
}
