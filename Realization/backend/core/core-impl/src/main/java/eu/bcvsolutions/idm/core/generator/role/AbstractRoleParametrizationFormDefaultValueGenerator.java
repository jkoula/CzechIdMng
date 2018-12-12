package eu.bcvsolutions.idm.core.generator.role;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmGenerateValueDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.generator.AbstractValueGenerator;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;

/**
 * Abstract class for generators that use generating EAV values for role parameterization.
 *
 * @author Ondrej Kopr
 * @since 9.4.0
 */
public abstract class AbstractRoleParametrizationFormDefaultValueGenerator<DTO extends AbstractDto>
		extends AbstractValueGenerator<DTO> {

	public static String REGEX_MULTIPLE_VALUES = "regexMultipleValues";
	private static String REGEX_MULTIPLE_VALUES_DEFAULT_VALUE = ",";

	@Autowired
	protected IdmFormDefinitionService formDefinitionService;

	protected IdmFormInstanceDto getDefaultValuesByRoleDefinition(IdmRoleDto roleDto,
			IdmGenerateValueDto valueGenerator, IdmFormInstanceDto existingEavs, DTO dto) {
		UUID formDefinitionId = roleDto.getIdentityRoleAttributeDefinition();

		// If form definition doesn't exist continue as normal
		if (formDefinitionId == null) {
			return null;
		}

		// BIG TODO: form definition will not exist please support each attribute, or?
		IdmFormDefinitionDto definition = formDefinitionService.get(formDefinitionId);
		// Internal behavior #getValuesFromInstances wants default form value

		List<IdmFormValueDto> values = new ArrayList<>();
		for (IdmFormAttributeDto attribute : definition.getFormAttributes()) {
			// check if exists default values
			if (StringUtils.isEmpty((attribute.getDefaultValue()))) {

				// Check given values
				List<IdmFormValueDto> existingValues = existingEavs == null ? null : existingEavs.getValues().stream() //
						.filter(existingEav -> { //
							return existingEav.getFormAttribute().equals(attribute.getId());
						}).collect(Collectors.toList()); //
				if (existingValues != null && !existingValues.isEmpty()) {
					// Given value exist and default value doesn't exist
					values.addAll(existingValues);
				}
				continue;
			}

			if (valueGenerator.isRegenerateValue()) {
				// For regenerate just create values by default values
				String regex = this.getRegex(valueGenerator);
				String[] defaultValues = attribute.getDefaultValue().split(regex);
				for (String defaultValue : defaultValues) {
					IdmFormValueDto value = new IdmFormValueDto(attribute);
					value.setValue(defaultValue);
					values.add(value);
				}
			} else {
				if (existingEavs != null) {
					// With check existing values
					List<IdmFormValueDto> existingValues = existingEavs.getValues()
							.stream() //
							.filter(existingVal -> { //
								return existingVal.getFormAttribute().equals(attribute.getId()); //
							}) //
							.collect(Collectors.toList()); //
					if (!existingValues.isEmpty()) {
						// If existing value isn't empty use it (given values)
						existingValues.forEach(existingValue -> {
							IdmFormValueDto value = new IdmFormValueDto(attribute);
							value.setValue(existingValue.getValue(attribute.getPersistentType()));
							values.add(value);
						});
					} else {
						// If given values is empty use default
						if (attribute.isMultiple()) {
							// Default value may be multiple, just iterate over value splited by regex
							String regex = this.getRegex(valueGenerator);
							String[] defaultValues = attribute.getDefaultValue().split(regex);
							for (String defaultValue : defaultValues) {
								IdmFormValueDto value = new IdmFormValueDto(attribute);
								value.setValue(defaultValue);
								values.add(value);
							}
						} else {
							// If is value single valued use it as is it
							IdmFormValueDto value = new IdmFormValueDto(attribute);
							value.setValue(attribute.getDefaultValue());
							values.add(value);
						}
					}
				}
				
			}
		}

		return new IdmFormInstanceDto(dto, definition, values);
	}

	/**
	 * Replace all values in given form instance. Replace will be done for given
	 * definition and attribute
	 *
	 * @param replaceValues
	 * @param eavs
	 * @param definition
	 * @param attribute
	 * @return
	 */
	protected List<IdmFormInstanceDto> replaceValuesInFormInstance(List<IdmFormValueDto> replaceValues,
			List<IdmFormInstanceDto> eavs, IdmFormDefinitionDto definition, IdmFormAttributeDto attribute, DTO dto) {
		if (replaceValues.isEmpty()) {
			return eavs;
		}
		Optional<IdmFormInstanceDto> foundedInstance = eavs.stream()
				.filter(eav -> eav.getFormDefinition().getId().equals(definition.getId())).findFirst();
		if (foundedInstance.isPresent()) {
			IdmFormInstanceDto instanceDto = foundedInstance.get();
			eavs.remove(instanceDto);

			// remove all attributes that is equal to given attribute
			List<IdmFormValueDto> values = instanceDto.getValues();
			values.removeIf(value -> value.getFormAttribute().equals(attribute.getId()));
			values.addAll(replaceValues);
			eavs.add(instanceDto);
		} else {
			// create new instance
			eavs.add(new IdmFormInstanceDto(dto, definition, replaceValues));
		}
		return eavs;
	}

	/**
	 * Create {@link IdmFormValueDto} by given attribute and given value
	 *
	 * @param attribute
	 * @param value
	 * @return
	 */
	protected IdmFormValueDto createValue(IdmFormAttributeDto attribute, Serializable value) {
		IdmFormValueDto formValueDto = new IdmFormValueDto(attribute);
		formValueDto.setValue(value);
		return formValueDto;
	}

	/**
	 * Return all values founded in given eavs. In Eavs will be searched by given
	 * definition and attribute.
	 *
	 * @param eavs
	 * @param definition
	 * @param attribute
	 * @return
	 */
	protected List<IdmFormValueDto> getValuesFromInstances(List<IdmFormInstanceDto> eavs,
			IdmFormDefinitionDto definition, IdmFormAttributeDto attribute) {
		List<IdmFormValueDto> result = new ArrayList<>();
		if (eavs == null) {
			return result;
		}

		Optional<IdmFormInstanceDto> first = eavs.stream()
				.filter(eav -> eav.getFormDefinition().getCode().equals(definition.getCode())).findFirst();

		if (first.isPresent()) {
			IdmFormInstanceDto formInstanceDto = first.get();

			List<IdmFormValueDto> values = formInstanceDto.getValues();

			if (values == null || values.isEmpty()) {
				return result;
			}

			// find all values for given attribute
			result.addAll(values.stream().filter(value -> value.getFormAttribute().equals(attribute.getId()))
					.collect(Collectors.toList()));
		}

		// if doesn't exist return empty list
		return result;
	}

	/**
	 * Transform values to {@link List} of {@link IdmFormValueDto}
	 *
	 * @param valueGenerator
	 * @param value
	 * @param attribute
	 * @return
	 */
	protected List<IdmFormValueDto> getTransformedValues(IdmGenerateValueDto valueGenerator, String value,
			IdmFormAttributeDto attribute) {
		List<IdmFormValueDto> result = new ArrayList<>();
		if (attribute.isMultiple()) {
			String regex = getRegex(valueGenerator);
			for (String splitedValue : value.split(regex)) {
				result.add(createValue(attribute, splitedValue));
			}
		} else {
			result.add(createValue(attribute, value));
		}
		return result;
	}

	/**
	 * Get regex
	 *
	 * @param valueGenerator
	 * @return
	 */
	protected String getRegex(IdmGenerateValueDto valueGenerator) {
		String regex = valueGenerator.getGeneratorProperties().getString(REGEX_MULTIPLE_VALUES);
		if (StringUtils.isEmpty(regex)) {
			return REGEX_MULTIPLE_VALUES_DEFAULT_VALUE;
		}
		return regex;
	}

	@Override
	public List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>();
		properties.add(REGEX_MULTIPLE_VALUES);
		return properties;
	}
}
