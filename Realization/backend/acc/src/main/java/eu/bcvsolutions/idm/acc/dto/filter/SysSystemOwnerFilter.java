package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;


/**
 * Filter for {@link eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto}
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
public class SysSystemOwnerFilter extends DataFilter implements ExternalIdentifiableFilter {

	/**
	 * System
	 */
	public static final String PARAMETER_SYSTEM = "system";
	/**
	 * Owner as identity
	 */
	public static final String PARAMETER_OWNER = "owner";


	public SysSystemOwnerFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public SysSystemOwnerFilter(MultiValueMap<String, Object> data) {
		super(SysSystemOwnerDto.class, data);
	}

	public UUID getSystem() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SYSTEM));
	}

	public void setSystem(UUID system) {
		data.set(PARAMETER_SYSTEM, system);
	}

	public UUID getOwner() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_OWNER));
	}

	public void setOwner(UUID owner) {
		data.set(PARAMETER_OWNER, owner);
	}

}
