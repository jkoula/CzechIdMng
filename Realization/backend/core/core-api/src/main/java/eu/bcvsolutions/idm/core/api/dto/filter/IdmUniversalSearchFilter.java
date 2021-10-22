package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import java.util.UUID;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Filter for a universal search
 *
 * @since 12.0.0
 * @author Vít Švanda
 *
 */
public class IdmUniversalSearchFilter extends DataFilter {

	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_INCLUDE_OWNER = "includeOwner"; // Context property - if true, then entity owns this request will be load and setts to a request DTO.

	public IdmUniversalSearchFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmUniversalSearchFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}

	public IdmUniversalSearchFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmDelegationDto.class, data, parameterConverter);
	}

	public UUID getOwnerId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_OWNER_ID);
	}

	public void setOwnerId(UUID ownerId) {
		set(PARAMETER_OWNER_ID, ownerId);
	}

	public String getOwnerType() {
		return getParameterConverter().toString(getData(), PARAMETER_OWNER_TYPE);
	}

	public void setOwnerType(String ownerType) {
		set(PARAMETER_OWNER_TYPE, ownerType);
	}

	public boolean isIncludeOwner() {
		return getParameterConverter().toBoolean(getData(), PARAMETER_INCLUDE_OWNER, false);
	}

	public void setIncludeOwner(boolean includeOwner) {
		set(PARAMETER_INCLUDE_OWNER, includeOwner);
	}
}
