package eu.bcvsolutions.idm.core.monitoring.api.dto;

import javax.validation.constraints.NotEmpty;

import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import eu.bcvsolutions.idm.core.api.dto.AbstractComponentDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Monitoring evaluator dto - definition only.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@JsonInclude(Include.NON_NULL)
@Relation(collectionRelation = "monitoringEvaluators")
public class MonitoringEvaluatorDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	@NotEmpty
	private String evaluatorType;
	private IdmFormDefinitionDto formDefinition;
	
	public String getEvaluatorType() {
		return evaluatorType;
	}
	
	public void setEvaluatorType(String evaluatorType) {
		this.evaluatorType = evaluatorType;
	}
	
	public void setFormDefinition(IdmFormDefinitionDto formDefinition) {
		this.formDefinition = formDefinition;
	}
	
	public IdmFormDefinitionDto getFormDefinition() {
		return formDefinition;
	}
}
