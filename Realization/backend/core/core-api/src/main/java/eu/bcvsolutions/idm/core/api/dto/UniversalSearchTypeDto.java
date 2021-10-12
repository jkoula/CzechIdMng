package eu.bcvsolutions.idm.core.api.dto;

import javax.validation.constraints.NotEmpty;
import org.springframework.hateoas.core.Relation;

/**
 * Universal search type DTO.
 *
 * @since 11.3.0
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "universalSearchTypes")
public class UniversalSearchTypeDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;

	private String type;
	private long count;
	@NotEmpty
	private String ownerType;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}
}
