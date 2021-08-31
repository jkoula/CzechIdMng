package eu.bcvsolutions.idm.acc.dto;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.acc.domain.SysValueChangeType;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO for attribute value with marked type of change
 * 
 * @author Svanda
 * @author Ondrej Husnik
 *
 */
@Relation(collectionRelation = "accounts")
@ApiModel(description = "Attribute value with marked type of change")
public class SysAttributeDifferenceValueDto extends AbstractDto {
	private static final long serialVersionUID = 1L;

	private Object value;
	private Object oldValue;
	@ApiModelProperty(required = false, notes = "Type of value change")
	private SysValueChangeType change;

	public SysAttributeDifferenceValueDto() {
	}
	
	public SysAttributeDifferenceValueDto(Object value, Object oldValue, SysValueChangeType type) {
		this.value = value;
		this.oldValue = oldValue;
		this.change = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public SysValueChangeType getChange() {
		return change;
	}

	public void setChange(SysValueChangeType change) {
		this.change = change;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}
}
