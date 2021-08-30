package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.acc.domain.SystemGroupType;
import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import org.springframework.hateoas.core.Relation;

/**
 * System groups (cross-domain)
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Relation(collectionRelation = "systemGroups")
public class SysSystemGroupDto extends AbstractDto implements Codeable, Disableable {

	private static final long serialVersionUID = 1L;

	private String code;
	private String description;
	private boolean disabled;
	private SystemGroupType type;
	
	@Override
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean isDisabled() {
		return disabled;
	}

	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public SystemGroupType getType() {
		return type;
	}

	public void setType(SystemGroupType type) {
		this.type = type;
	}
}
