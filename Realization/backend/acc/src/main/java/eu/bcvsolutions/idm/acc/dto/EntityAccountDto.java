package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Interface for all relations between entity and account (for synchronization purpose)
 * 
 * @author svandav
 *
 */
public interface EntityAccountDto extends BaseDto {

	String PROPERTY_ACCOUNT = "account";

    UUID getAccount();

	void setAccount(UUID account);

	boolean isOwnership();

	void setOwnership(boolean ownership);

	UUID getEntity();

	void setEntity(UUID entity);

}