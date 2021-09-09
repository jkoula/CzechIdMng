package eu.bcvsolutions.idm.acc.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import java.util.UUID;
import org.springframework.hateoas.core.Relation;

/**
 * System groups system - relation between a system and a group of systems.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Relation(collectionRelation = "systemGroupSystems")
public class SysSystemGroupSystemDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = SysSystemGroupDto.class)
	private UUID systemGroup;
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@Embedded(dtoClass = SysSystemAttributeMappingDto.class)
	private UUID mergeAttribute;

	public UUID getSystemGroup() {
		return systemGroup;
	}

	public void setSystemGroup(UUID systemGroup) {
		this.systemGroup = systemGroup;
	}

	public UUID getSystem() {
		return system;
	}

	public void setSystem(UUID system) {
		this.system = system;
	}

	public UUID getMergeAttribute() {
		return mergeAttribute;
	}

	public void setMergeAttribute(UUID mergeAttribute) {
		this.mergeAttribute = mergeAttribute;
	}
}
