package eu.bcvsolutions.idm.core.eav.rest.impl;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoController;
import eu.bcvsolutions.idm.core.api.rest.AbstractReadWriteDtoControllerRestTest;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;

/**
 * For values agenda tests.
 * 
 * @author Roman Kučera
 * @author Radek Tomiška
 */
public class IdmFormValueControllerRestTest extends AbstractReadWriteDtoControllerRestTest<IdmFormValueDto> {

	@Autowired private FormService formService;
	@Autowired private IdmFormValueController controller;
	
	@Override
	protected AbstractReadWriteDtoController<IdmFormValueDto, ?> getController() {
		return controller;
	}

	@Override
	protected IdmFormValueDto prepareDto() {
		return prepareDto(PersistentType.SHORTTEXT);
	}
	
	protected IdmFormValueDto prepareDto(PersistentType peristentType) {
		// create definition one
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setCode(getHelper().createName());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(peristentType);
		attributeDefinitionOne = formService.saveAttribute(IdmIdentityDto.class, attributeDefinitionOne);
		//
		IdmFormValueDto value = new IdmFormValueDto(attributeDefinitionOne);
		value.setShortTextValue(getHelper().createName());
		//
		return value;
	}
	
	
	@Override
	protected boolean isReadOnly() {
		return true;
	}
	
	@Override
	public void testGet() throws Exception {
		// ~ get is not available
	}
	
	@Override
	public void testPermissions() {
		// ~ get is not available
	}
	
	@Override
	protected boolean supportsBulkActions() {
		return true;
	}

	@Override
	protected IdmFormValueDto createDto(IdmFormValueDto value) {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		return formService.saveValues(identity, formService.getDefinition(IdmIdentityDto.class), Lists.newArrayList(value)).get(0);
	}
	
	protected List<IdmFormValueDto> find(String searchName, MultiValueMap<String, String> parameters, Authentication authentication) {
		// we need form defition in parameters
		parameters.set(IdmFormValueFilter.PARAMETER_DEFINITION_ID, formService.getDefinition(IdmIdentityDto.class).getId().toString());
		//
		return super.find(searchName, parameters, authentication);
	}
	
	@Override
	protected long count(MultiValueMap<String, String> parameters) {
		// we need form defition in parameters
		parameters.set(IdmFormValueFilter.PARAMETER_DEFINITION_ID, formService.getDefinition(IdmIdentityDto.class).getId().toString());
		//
		return super.count(parameters);
	}
	
	@Test
	public void testFindByText() {
		IdmFormValueDto valueOne = createDto();
		createDto(); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setText(((IdmFormAttributeDto) valueOne.getEmbedded().get(IdmFormValueDto.PROPERTY_FORM_ATTRIBUTE)).getCode());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByPersistentType() {
		IdmFormValueDto value = prepareDto(PersistentType.TEXT);
		String text = getHelper().createName();
		value.setValue(text);
		IdmFormValueDto valueOne = createDto(value);
		createDto(); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setStringValue(text);
		filter.setPersistentType(PersistentType.TEXT);
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByShortTextValue() {
		IdmFormValueDto value = prepareDto(PersistentType.SHORTTEXT);
		String text = getHelper().createName();
		value.setValue(text);
		IdmFormValueDto valueOne = createDto(value);
		createDto(); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setShortTextValue(text);
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByShortTextValueLike() {
		IdmFormValueDto value = prepareDto(PersistentType.SHORTTEXT);
		String suffix = getHelper().createName();
		value.setValue(getHelper().createName() + suffix);
		IdmFormValueDto valueOne = createDto(value);
		createDto(); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setShortTextValueLike(suffix);
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByStringValue() {
		IdmFormValueDto value = prepareDto(PersistentType.TEXT);
		String text = getHelper().createName();
		value.setValue(text);
		IdmFormValueDto valueOne = createDto(value);
		createDto(); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setStringValue(text);
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByStringValueLike() {
		IdmFormValueDto value = prepareDto(PersistentType.TEXT);
		String suffix = getHelper().createName();
		value.setValue(getHelper().createName() + suffix);
		IdmFormValueDto valueOne = createDto(value);
		createDto(); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setStringValueLike(suffix);
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByBooleanValue() {		
		IdmFormValueDto value = prepareDto(PersistentType.BOOLEAN);
		value.setBooleanValue(true);
		IdmFormValueDto valueOne = createDto(value);
		value = prepareDto(PersistentType.BOOLEAN);
		value.setBooleanValue(false);
		createDto(value); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setBooleanValue(true);
		filter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByLongValue() {		
		IdmFormValueDto value = prepareDto(PersistentType.LONG);
		value.setLongValue(1L);
		IdmFormValueDto valueOne = createDto(value);
		value = prepareDto(PersistentType.LONG);
		value.setLongValue(2L);
		createDto(value); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setLongValue(1L);
		filter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByDoubleValue() {		
		IdmFormValueDto value = prepareDto(PersistentType.DOUBLE);
		value.setDoubleValue(BigDecimal.valueOf(1L));
		IdmFormValueDto valueOne = createDto(value);
		value = prepareDto(PersistentType.DOUBLE);
		value.setDoubleValue(BigDecimal.valueOf(2L));
		createDto(value); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setDoubleValue(BigDecimal.valueOf(1L));
		filter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByDateValue() {		
		IdmFormValueDto value = prepareDto(PersistentType.DATETIME);
		ZonedDateTime now = ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		value.setDateValue(now);
		IdmFormValueDto valueOne = createDto(value);
		value = prepareDto(PersistentType.DATETIME);
		value.setDateValue(now.plusDays(2));
		createDto(value); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setDateValue(now);
		filter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByDateValueFrom() {		
		IdmFormValueDto value = prepareDto(PersistentType.DATETIME);
		ZonedDateTime now = ZonedDateTime.now();
		value.setDateValue(now);
		createDto(value);
		value = prepareDto(PersistentType.DATETIME);
		value.setDateValue(now.plusDays(2));
		IdmFormValueDto valueOne = createDto(value);
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setDateValueFrom(now.plusDays(1));
		filter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByDateValueTill() {		
		IdmFormValueDto value = prepareDto(PersistentType.DATETIME);
		ZonedDateTime now = ZonedDateTime.now();
		value.setDateValue(now);
		IdmFormValueDto valueOne = createDto(value);
		value = prepareDto(PersistentType.DATETIME);
		value.setDateValue(now.plusDays(2));
		createDto(value);
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setDateValueTill(now.plusDays(1));
		filter.setTransactionId(TransactionContextHolder.getContext().getTransactionId());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}
	
	@Test
	public void testFindByUuidValue() {		
		IdmFormValueDto value = prepareDto(PersistentType.UUID);
		value.setUuidValue(UUID.randomUUID());
		IdmFormValueDto valueOne = createDto(value);
		value = prepareDto(PersistentType.UUID);
		value.setUuidValue(UUID.randomUUID());
		createDto(value); // other
		//
		IdmFormValueFilter<?> filter = new IdmFormValueFilter<>();
		filter.setUuidValue(valueOne.getUuidValue());
		List<IdmFormValueDto> results = find(filter);
		//
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(results.stream().anyMatch(r -> r.getId().equals(valueOne.getId())));
	}

	@Test
	public void findValues() throws Exception {
		IdmIdentityDto ownerIdentity = getHelper().createIdentity((GuardedString) null);
		IdmRoleDto ownerRole = getHelper().createRole();
		IdmIdentityContractDto ownerIdentityContract = getHelper().createContract(ownerIdentity);
		IdmTreeNodeDto ownerTreeNode = null;
		try {
			getHelper().loginAdmin();
			ownerTreeNode = getHelper().createTreeNode();
		} finally {
			logout();
		}
		//
		Assert.assertEquals(1, prepareDataAndSearch(IdmIdentity.class, ownerIdentity));
		Assert.assertEquals(1, prepareDataAndSearch(IdmRole.class, ownerRole));
		Assert.assertEquals(1, prepareDataAndSearch(IdmTreeNode.class, ownerTreeNode));
		Assert.assertEquals(1, prepareDataAndSearch(IdmIdentityContract.class, ownerIdentityContract));
	}

	private int prepareDataAndSearch(Class<? extends AbstractEntity> type, AbstractDto owner) throws Exception {
		// create attribute
		IdmFormAttributeDto attribute = new IdmFormAttributeDto();
		String attributeName = getHelper().createName();
		attribute.setCode(attributeName);
		attribute.setName(attribute.getCode());
		attribute.setPersistentType(PersistentType.SHORTTEXT);

		// create definition
		IdmFormDefinitionDto definition = formService.createDefinition(type, getHelper().createName(), Lists.newArrayList(attribute));
		attribute = definition.getMappedAttributeByCode(attribute.getCode());

		// save value
		formService.saveValues(owner.getId(), type, attribute, Lists.newArrayList("one"));
		String response = getJsonAsString("/form-values", "definitionId=" + definition.getId(), 20l, 0l, null, null, getAdminAuthentication());
		return getEmbeddedList("formValues", response).size();
	}
}
