package eu.bcvsolutions.idm.core.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import io.swagger.annotations.ApiModelProperty;
import java.util.UUID;

/**
 * IdM system DTO - It is parent for SysSystemDto in Acc module (we need to work with system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public class IdmSystemDto extends FormableDto implements Codeable, Disableable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private String description;
	private boolean readonly;
	@ApiModelProperty(notes = "Just write operation is disabled on the system, ACM and wish is constructed, provisioning operation is available in queue.")
	private boolean disabled;
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private boolean virtual;

	public IdmSystemDto() {
	}

	public IdmSystemDto(UUID id) {
		super(id);
	}

	public String getName() {
		return name;
	}

	@Override
	@JsonIgnore
	public String getCode() {
		return getName();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isVirtual() {
		return virtual;
	}

	public void setVirtual(boolean virtual) {
		this.virtual = virtual;
	}
}
