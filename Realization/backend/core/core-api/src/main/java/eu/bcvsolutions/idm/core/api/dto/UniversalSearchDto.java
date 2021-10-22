package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;
import org.springframework.hateoas.core.Relation;

/**
 * Universal search DTO.
 *
 * @since 12.0.0
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "universalSearches")
public class UniversalSearchDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	private UUID ownerId;
	private String ownerType;
	private BaseDto ownerDto;
	private String niceLabel;
	private UniversalSearchTypeDto type;

	public UniversalSearchDto() {
		super();
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public BaseDto getOwnerDto() {
		return ownerDto;
	}

	public void setOwnerDto(BaseDto ownerDto) {
		this.ownerDto = ownerDto;
	}

	public String getNiceLabel() {
		return niceLabel;
	}

	public void setNiceLabel(String niceLabel) {
		this.niceLabel = niceLabel;
	}

	public UniversalSearchTypeDto getType() {
		return type;
	}

	public void setType(UniversalSearchTypeDto type) {
		this.type = type;
	}
}
