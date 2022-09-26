package eu.bcvsolutions.idm.acc.dto;

import java.util.List;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;

/**
 * Dto for work with account in wizard
 * @author Roman Kucera
 */
@Relation(collectionRelation = "accountWizards")
public class AccountWizardDto extends AbstractWizardDto {

	private IdmFormDefinitionDto formDefinition;
	private List<IdmFormValueDto> values;

	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}

	public List<IdmFormValueDto> getValues() {
		return values;
	}

	public void setValues(List<IdmFormValueDto> values) {
		this.values = values;
	}
}
