package eu.bcvsolutions.idm.core.ecm.api.dto.filter;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;

/**
 * Filter for attachments.
 * 
 * @author Radek Tomiška
 * @since 7.6.0
 *
 */
public class IdmAttachmentFilter extends DataFilter {

	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_CREATED_BEFORE= "createdBefore";
	public static final String PARAMETER_CREATED_AFTER = "createdAfter";
	public static final String PARAMETER_ATTACHMENT_TYPE = "attachmentType";
	//
	private String name;
	private Boolean lastVersionOnly; // true - last version only
	private UUID versionsFor; // attachment id - all versions for attachment
	private String attachmentType;
	
	public IdmAttachmentFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAttachmentFilter(MultiValueMap<String, Object> data) {
		super(IdmAttachmentDto.class, data);
	}

	public UUID getOwnerId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_OWNER_ID);
	}

	public void setOwnerId(UUID ownerId) {
		set(PARAMETER_OWNER_ID, ownerId);
	}

	public String getOwnerType() {
		return (String) data.getFirst(PARAMETER_OWNER_TYPE);
	}

	public void setOwnerType(String ownerType) {
		set(PARAMETER_OWNER_TYPE, ownerType);
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLastVersionOnly(Boolean lastVersionOnly) {
		this.lastVersionOnly = lastVersionOnly;
	}
	
	public Boolean getLastVersionOnly() {
		return lastVersionOnly;
	}
	
	public UUID getVersionsFor() {
		return versionsFor;
	}
	
	public void setVersionsFor(UUID versionsFor) {
		this.versionsFor = versionsFor;
	}

	public ZonedDateTime getCreatedAfter() {
		return getParameterConverter().toDateTime(data, PARAMETER_CREATED_AFTER);
	}

	public void setCreatedAfter(ZonedDateTime createdAfter) {
		set(PARAMETER_CREATED_AFTER, createdAfter);
	}

	public ZonedDateTime getCreatedBefore() {
		// FIXME: parameter converter
		IdmFormValueDto value = new IdmFormValueDto();
		value.setPersistentType(PersistentType.DATETIME);
		value.setValue((Serializable) data.getFirst(PARAMETER_CREATED_BEFORE));
		//
		return value.getDateValue();
	}

	public void setCreatedBefore(ZonedDateTime createdBefore) {
		set(PARAMETER_CREATED_BEFORE, createdBefore);
	}
	
	/**
	 * Filter by attachment type.
	 * 
	 * @return attachment type
	 * @since 11.2.0
	 */
	public String getAttachmentType() {
		return attachmentType;
	}
	
	/**
	 * Filter by attachment type.
	 * @param attachmentType attachment type
	 * @since 11.2.0
	 */
	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}
}