package eu.bcvsolutions.idm.acc.dto;

import java.math.BigDecimal;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

@Relation(collectionRelation = "schemaFormAttributes")
public class AccSchemaFormAttributeDto extends AbstractDto {
	private static final long serialVersionUID = 1L;
	//
	@NotNull
	@Embedded(dtoClass = IdmFormAttributeDto.class)
	private UUID formAttribute;
	@Embedded(dtoClass = SysSchemaObjectClassDto.class)
	private UUID schema;
	private String defaultValue;
	private boolean unique;
	private BigDecimal max;
	private BigDecimal min;
	private String regex;
	private boolean required;
	private String validationMessage;

	public AccSchemaFormAttributeDto() {
	}

	public AccSchemaFormAttributeDto(UUID id) {
		super(id);
	}

	public UUID getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(UUID formAttribute) {
		this.formAttribute = formAttribute;
	}

	public UUID getSchema() {
		return schema;
	}

	public void setSchema(UUID schema) {
		this.schema = schema;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @return
	 */
	public String getValidationMessage() {
		return validationMessage;
	}
	
	/**
	 * Custom message, when validation fails - localization key can be used.
	 * 
	 * @param validationMessage
	 */
	public void setValidationMessage(String validationMessage) {
		this.validationMessage = validationMessage;
	}
}
