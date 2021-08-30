package eu.bcvsolutions.idm.core.api.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleSystemDto;
import java.util.Set;
import java.util.UUID;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Filter for role system mapping - It is parent for SysRoleSystemFilter in Acc module (we need to work with role-system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public class IdmRoleSystemFilter extends DataFilter {

	public IdmRoleSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmRoleSystemFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleSystemDto.class, data);
	}

	private UUID roleId;
	private UUID systemId;
	private UUID systemMappingId;
	private UUID attributeMappingId;
	private Set<UUID> roleIds;
	private Boolean createAccountByDefault;
	private Boolean checkIfIsInCrossDomainGroup;
	private UUID isInCrossDomainGroupRoleId;

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public UUID getSystemMappingId() {
		return systemMappingId;
	}

	public void setSystemMappingId(UUID systemMappingId) {
		this.systemMappingId = systemMappingId;
	}

	public UUID getAttributeMappingId() {
		return attributeMappingId;
	}

	public void setAttributeMappingId(UUID attributeMappingId) {
		this.attributeMappingId = attributeMappingId;
	}

	public Set<UUID> getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(Set<UUID> roleIds) {
		this.roleIds = roleIds;
	}

	public Boolean getCreateAccountByDefault() {
		return createAccountByDefault;
	}

	public void setCreateAccountByDefault(Boolean createAccountByDefault) {
		this.createAccountByDefault = createAccountByDefault;
	}

	public Boolean getCheckIfIsInCrossDomainGroup() {
		return checkIfIsInCrossDomainGroup;
	}

	/**
	 * Is not filter! Only for fill true to the result.
	 */
	public void setCheckIfIsInCrossDomainGroup(Boolean checkIfIsInCrossDomainGroup) {
		this.checkIfIsInCrossDomainGroup = checkIfIsInCrossDomainGroup;
	}

	public UUID getIsInCrossDomainGroupRoleId() {
		return isInCrossDomainGroupRoleId;
	}

	public void setIsInCrossDomainGroupRoleId(UUID isInCrossDomainGroupRoleId) {
		this.isInCrossDomainGroupRoleId = isInCrossDomainGroupRoleId;
	}
}
