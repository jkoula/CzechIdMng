package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;


/**
 * Filter for {@link eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto} - roles
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
public class SysSystemOwnerRoleFilter extends DataFilter implements ExternalIdentifiableFilter {

	/**
	 * Owner role
	 */
	public static final String PARAMETER_SYSTEM = SysSystemOwnerFilter.PARAMETER_SYSTEM;
	/**
	 * guarantee as role
	 */
	public static final String PARAMETER_OWNER_ROLE = "ownerRole";

	public SysSystemOwnerRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public SysSystemOwnerRoleFilter(MultiValueMap<String, Object> data) {
		super(SysSystemOwnerRoleDto.class, data);
	}

	public UUID getSystem() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_SYSTEM));
	}

	public void setSystem(UUID system) {
		data.set(PARAMETER_SYSTEM, system);
	}

	public UUID getOwnerRole() {
		return DtoUtils.toUuid(data.getFirst(PARAMETER_OWNER_ROLE));
	}

	public void setOwnerRole(UUID ownerRole) {
		data.set(PARAMETER_OWNER_ROLE, ownerRole);
	}

}
