package eu.bcvsolutions.idm.core.api.rest.lookup;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.GenericTypeResolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;

/**
 * Find {@link IdmFormProjectionDto} by uuid identifier or by {@link Codeable} identifier.
 * 
 * @author Radek Tomiška
 * @param <T> dto
 * @since 11.0.0
 */
public abstract class AbstractFormProjectionLookup<DTO extends BaseDto> implements FormProjectionLookup<DTO> {

	@Autowired private ObjectMapper mapper;
	//
	private final Class<?> domainType;

	/**
	 * Creates a new {@link AbstractFormProjectionLookup} instance discovering the supported type from the generics signature.
	 */
	public AbstractFormProjectionLookup() {
		this.domainType = GenericTypeResolver.resolveTypeArgument(getClass(), FormProjectionLookup.class);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
	 */
	@Override
	public boolean supports(Class<?> delimiter) {
		return domainType.equals(delimiter);
	}
	
	@Override
	public IdmFormDefinitionDto lookupBasicFieldsDefinition(DTO dto) {
		return getBasicFieldsDefinition(dto);
	}
	
	@Override
	public IdmFormDefinitionDto lookupFormDefinition(DTO dto, IdmFormDefinitionDto formDefinition) {
		return getFormDefinition(dto, formDefinition);
	}
	
	/**
	 * Construct basic fields form definition.
	 * 
	 * @param dto basic fields owner
	 * @return basic fields form definition
	 */
	protected IdmFormDefinitionDto getBasicFieldsDefinition(DTO dto) {
		return getFormDefinition(dto, null); // null ~ basicFileds ~ without form definition
	}
	
	/**
	 * Get overriden / configured form definition by projection.
	 * @param dto projection owner
	 * @param formDefinition form definition to load
	 * @return overriden form definition
	 * 
	 * @since 12.0.0
	 */
	protected IdmFormDefinitionDto getFormDefinition(DTO dto, IdmFormDefinitionDto formDefinition) {
		IdmFormProjectionDto formProjection = lookupProjection(dto);
		if (formProjection == null) {
			return null;
		}
		String formValidations = formProjection.getFormValidations();
		if (StringUtils.isEmpty(formValidations)) {
			return null;
		}
		//
		if (formDefinition == null) { // ~ basic fields
			formDefinition = new IdmFormDefinitionDto();
			formDefinition.setCode(FormService.FORM_DEFINITION_CODE_BASIC_FIELDS);
		}
		IdmFormDefinitionDto overridenDefinition = new IdmFormDefinitionDto(); // clone ~ prevent to change input (e.g. cache can be modified)
		overridenDefinition.setId(formDefinition.getId());
		overridenDefinition.setCode(formDefinition.getCode());
		// transform form attributes from json
		try {
			List<IdmFormAttributeDto> attributes = mapper.readValue(formValidations, new TypeReference<List<IdmFormAttributeDto>>() {});
			attributes
				.stream()
				.filter(attribute -> Objects.equals(attribute.getFormDefinition(), overridenDefinition.getId()))
				.forEach(attribute -> {
					if (attribute.getId() == null) {
						// we need artificial id to find attributes in definition / instance
						attribute.setId(UUID.randomUUID());
					}
					overridenDefinition.addFormAttribute(attribute);
				});
			//
			return overridenDefinition;
		} catch (IOException ex) {
			throw new ResultCodeException(
					CoreResultCode.FORM_PROJECTION_WRONG_VALIDATION_CONFIGURATION,
					ImmutableMap.of("formProjection", formProjection.getCode()),
					ex
			);
		}
	}
	
	/**
	 * Add value, if it's filled into filled values.
	 * 
	 * @param filledValues filled values
	 * @param formDefinition fields form definition (e.g. basic fields form definition)
	 * @param attributeCode attribute code
	 * @param attributeValue attribute value
	 */
	protected void appendAttributeValue(
			List<IdmFormValueDto> filledValues,
			IdmFormDefinitionDto formDefinition, 
			String attributeCode, 
			Serializable attributeValue) {
		if (attributeValue == null) {
			return;
		}
		IdmFormAttributeDto attribute = formDefinition.getMappedAttributeByCode(attributeCode);
		if (attribute == null) {
			return;
		}
		if (attribute.getPersistentType() == null) {
			if (attributeValue instanceof UUID) {
				attribute.setPersistentType(PersistentType.UUID);
			} else if (attributeValue instanceof LocalDate) {
				attribute.setPersistentType(PersistentType.DATE);
			} else {
				// TODO: support other persistent types (unused now)
				attribute.setPersistentType(PersistentType.TEXT);
			}
		}
		//
		IdmFormValueDto value = new IdmFormValueDto(attribute);
		value.setValue(attributeValue);
		filledValues.add(value);
	}
}