package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

/**
 * Default sync configuration DTO.
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "synchronizationConfigs")
public class SysSyncConfigDto extends AbstractSysSyncConfigDto {

	private static final long serialVersionUID = 1L;
	
	public SysSyncConfigDto() {
	}
	
	public SysSyncConfigDto(UUID id) {
		super(id);
	}
}
