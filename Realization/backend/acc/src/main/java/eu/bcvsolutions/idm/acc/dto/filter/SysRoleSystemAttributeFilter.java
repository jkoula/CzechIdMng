package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for role system attribute mapping
 * 
 * @author svandav
 *
 */
public class SysRoleSystemAttributeFilter implements BaseFilter {

	private UUID roleSystemId;
	private Boolean isUid;
	private UUID systemMappingId;
	private String schemaAttributeName;
	private UUID systemAttributeMappingId;
	private UUID identityId;
	private UUID accountId;
	private UUID systemId;
	private UUID roleSystemRelationForIdentityId;
	/**
	 * Get role-system-attributes with cross domains groups (using same merge attribute)
	 * or attributes where default account creation is disabled (no-login roles).
	 * Only TRUE and NULL is implemented!
	 */
	private Boolean isInCrossDomainGroupOrIsNoLogin;
	private Boolean inCrossDomainGroup;

	public Boolean getIsUid() {
		return isUid;
	}

	public void setIsUid(Boolean isUid) {
		this.isUid = isUid;
	}

	public UUID getRoleSystemId() {
		return roleSystemId;
	}

	public void setRoleSystemId(UUID roleSystemId) {
		this.roleSystemId = roleSystemId;
	}

	public UUID getSystemMappingId() {
		return systemMappingId;
	}

	public void setSystemMappingId(UUID systemMappingId) {
		this.systemMappingId = systemMappingId;
	}

	public String getSchemaAttributeName() {
		return schemaAttributeName;
	}

	public void setSchemaAttributeName(String schemaAttributeName) {
		this.schemaAttributeName = schemaAttributeName;
	}

	public UUID getSystemAttributeMappingId() {
		return systemAttributeMappingId;
	}

	public void setSystemAttributeMappingId(UUID systemAttributeMappingId) {
		this.systemAttributeMappingId = systemAttributeMappingId;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public UUID getRoleSystemRelationForIdentityId() {
		return roleSystemRelationForIdentityId;
	}

	/**
	 * We want to find all override attribute for identity, where relation between
	 * identity-role and role-system is null or if relation is not null, then return
	 * override attributes where same role-systems are used.
	 */
	public void setRoleSystemRelationForIdentityId(UUID roleSystemRelationForIdentityId) {
		this.roleSystemRelationForIdentityId = roleSystemRelationForIdentityId;
	}

	public Boolean getInCrossDomainGroupOrIsNoLogin() {
		return isInCrossDomainGroupOrIsNoLogin;
	}

	/**
	 * Get role-system-attributes with cross domains groups (using same merge attribute) or attributes where default account creation is disabled.
	 * Only TRUE and NULL is implemented!
	 */
	public void setInCrossDomainGroupOrIsNoLogin(Boolean inCrossDomainGroup) {
		isInCrossDomainGroupOrIsNoLogin = inCrossDomainGroup;
	}

	public Boolean getInCrossDomainGroup() {
		return inCrossDomainGroup;
	}

	public void setInCrossDomainGroup(Boolean inCrossDomainGroup) {
		this.inCrossDomainGroup = inCrossDomainGroup;
	}
}
