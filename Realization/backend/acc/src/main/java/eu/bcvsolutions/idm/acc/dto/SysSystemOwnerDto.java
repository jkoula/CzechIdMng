package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;
import io.swagger.annotations.ApiModelProperty;

/**
 * Dto for system owner - identity
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Relation(collectionRelation = "systemOwners")
public class SysSystemOwnerDto extends AbstractDto implements ExternalIdentifiable, Requestable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@NotNull
	@Embedded(dtoClass = SysSystemDto.class)
	private UUID system;
	@NotNull
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID owner; // owner as identity
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity

	/**
	 * System
	 *
	 * @return UUID of system
	 */
	public UUID getSystem() {
		return system;
	}

	/**
	 * System
	 *
	 * @param system UUID of system
	 */
	public void setSystem(UUID system) {
		this.system = system;
	}

	/**
	 * Owner as identity
	 *
	 * @return UUID of identity
	 */
	public UUID getOwner() {
		return owner;
	}

	/**
	 * Owner as identity
	 *
	 * @param owner UUID of identity
	 */
	public void setOwner(UUID owner) {
		this.owner = owner;
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}

}
