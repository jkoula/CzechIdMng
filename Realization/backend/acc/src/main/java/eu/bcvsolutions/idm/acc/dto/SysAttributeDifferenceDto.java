package eu.bcvsolutions.idm.acc.dto;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


/**
 * DTO containing attribute values and difference type
 * 
 * @author Svanda
 * @author Ondrej Husnik
 *
 */
@Relation(collectionRelation = "attributes")
@ApiModel(description = "System attribute differences")
public class SysAttributeDifferenceDto extends AbstractDto {
	private static final long serialVersionUID = 1L;
	
	@NotEmpty
	@Size(min = 1, max = DefaultFieldLengths.NAME)
	@ApiModelProperty(required = true, notes = "Name of attribute")
	private String name;
	private boolean multivalue;
	private boolean changed = false;
	private SysAttributeDifferenceValueDto value;
	private List<SysAttributeDifferenceValueDto> values;

	public SysAttributeDifferenceDto() {
	}
	
	public SysAttributeDifferenceDto(String name, boolean multiValue, boolean changed) {
		this.name = name;
		this.multivalue = multiValue;
		this.changed = changed;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isMultivalue() {
		return multivalue;
	}

	public void setMultivalue(boolean multivalue) {
		this.multivalue = multivalue;
	}

	public SysAttributeDifferenceValueDto getValue() {
		return value;
	}

	public void setValue(SysAttributeDifferenceValueDto value) {
		this.value = value;
	}

	public List<SysAttributeDifferenceValueDto> getValues() {
		if(values == null){
			this.values = new ArrayList<>();
		}
		return values;
	}

	public void setValues(List<SysAttributeDifferenceValueDto> values) {
		this.values = values;
	}

	public boolean isChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}
	
}
