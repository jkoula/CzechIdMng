package eu.bcvsolutions.idm.core.eav.rest.lookup;

import org.junit.Assert;
import org.junit.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.rest.lookup.DefaultDtoMapper;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.eav.IdmIdentityFormValue;
import eu.bcvsolutions.idm.core.model.repository.eav.IdmIdentityFormValueRepository;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Test convert form value entity to common form value dto.
 * 
 * @author Radek Tomiška
 */
@Transactional
public class IdmFormValueDtoMapperIntegrationTest extends AbstractIntegrationTest {
	
	@Autowired private LookupService lookupService;
	@Autowired private FormService formService;
	@Autowired private IdmIdentityFormValueRepository identityFormValueRepository;
	@Autowired private ModelMapper modelMapper;

	@Test
	public void testMapWithOwner() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		// create definition one
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setCode(getHelper().createName());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attributeDefinitionOne));
		attributeDefinitionOne = formDefinitionOne.getMappedAttributeByCode(attributeDefinitionOne.getCode());
		//
		IdmFormValueDto value = new IdmFormValueDto(attributeDefinitionOne);
		value.setValue(getHelper().createName());
		//
		formService.saveValues(identity, formDefinitionOne, Lists.newArrayList(value));
		//
		IdmFormValueDto savedValue = formService.getValues(identity, formDefinitionOne).get(0);
		Assert.assertEquals(value.getStringValue(), savedValue.getStringValue());
		//
		IdmIdentityFormValue savedEntity = identityFormValueRepository.findById(savedValue.getId()).get();
		//
		IdmFormValueDto valueDto = lookupService.toDto(savedEntity, null, null);
		//
		Assert.assertEquals(value.getStringValue(), valueDto.getStringValue());
		BaseDto owner = valueDto.getEmbedded().get(FormValueService.PROPERTY_OWNER);
		Assert.assertNotNull(owner);
		Assert.assertEquals(identity.getId(), owner.getId());
	}
	
	@Test
	public void testMapWithDefaultMapper() {
		IdmIdentityDto identity = getHelper().createIdentity((GuardedString) null);
		//
		// create definition one
		IdmFormAttributeDto attributeDefinitionOne = new IdmFormAttributeDto();
		attributeDefinitionOne.setCode(getHelper().createName());
		attributeDefinitionOne.setName(attributeDefinitionOne.getCode());
		attributeDefinitionOne.setPersistentType(PersistentType.TEXT);
		IdmFormDefinitionDto formDefinitionOne = formService.createDefinition(IdmIdentity.class, getHelper().createName(), Lists.newArrayList(attributeDefinitionOne));
		attributeDefinitionOne = formDefinitionOne.getMappedAttributeByCode(attributeDefinitionOne.getCode());
		//
		IdmFormValueDto value = new IdmFormValueDto(attributeDefinitionOne);
		value.setValue(getHelper().createName());
		//
		formService.saveValues(identity, formDefinitionOne, Lists.newArrayList(value));
		//
		IdmFormValueDto savedValue = formService.getValues(identity, formDefinitionOne).get(0);
		Assert.assertEquals(value.getStringValue(), savedValue.getStringValue());
		//
		IdmIdentityFormValue savedEntity = identityFormValueRepository.findById(savedValue.getId()).get();
		//
		DefaultDtoMapper mapper = new DefaultDtoMapper(modelMapper, IdmFormValueDto.class);
		IdmFormValueDto valueDto = (IdmFormValueDto) mapper.map(savedEntity, null, null);
		//
		Assert.assertEquals(value.getStringValue(), valueDto.getStringValue());
		BaseDto owner = valueDto.getEmbedded().get(FormValueService.PROPERTY_OWNER);
		Assert.assertNull(owner);
		//
		valueDto = (IdmFormValueDto) mapper.map(savedEntity, new IdmFormValueDto(), null);
		//
		Assert.assertEquals(value.getStringValue(), valueDto.getStringValue());
		owner = valueDto.getEmbedded().get(FormValueService.PROPERTY_OWNER);
		Assert.assertNull(owner);
		//
		Assert.assertNull(mapper.map(null, null, null));
	}
}
