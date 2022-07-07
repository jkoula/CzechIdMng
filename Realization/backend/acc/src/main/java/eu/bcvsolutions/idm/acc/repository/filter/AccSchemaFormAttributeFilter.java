package eu.bcvsolutions.idm.acc.repository.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.AccSchemaFormAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

public class AccSchemaFormAttributeFilter extends DataFilter {

	private UUID schema;
	private UUID formDefinition;
	private UUID formAttribute;
	
	public AccSchemaFormAttributeFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public AccSchemaFormAttributeFilter(MultiValueMap<String, Object> data) {
		super(AccSchemaFormAttributeDto.class, data);
	}

	public UUID getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(UUID formDefinition) {
		this.formDefinition = formDefinition;
	}

	public UUID getSchema() {
		return schema;
	}

	public void setSchema(UUID schema) {
		this.schema = schema;
	}

	public UUID getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(UUID formAttribute) {
		this.formAttribute = formAttribute;
	}
}
