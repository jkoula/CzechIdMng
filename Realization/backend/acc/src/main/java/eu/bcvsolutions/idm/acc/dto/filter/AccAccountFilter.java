package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.AccountType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.ExternalIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.FormableFilter;

/**
 * Filter for accounts
 * 
 * @author Radek Tomiška
 * @author Roman Kucera
 * @author Tomáš Doischer
 *
 */
public class AccAccountFilter extends DataFilter implements ExternalIdentifiableFilter, FormableFilter {
	
	public static final String PARAMETER_IDENTITY_ID = "identity";
	public static final String PARAMETER_IDENTITY_IDS = "identities";
	public static final String PARAMETER_ROLE_IDS = "roles";
	public static final String PARAMETER_SYSTEM_IDS = "systems";
	//
	private UUID systemEntityId;	
	private UUID systemId;	
	private UUID identityId;	
	private String uid;
	private AccountType accountType;
	private Boolean ownership;
	private Boolean supportChangePassword;
	private String entityType;
	private Boolean inProtection;
	private UUID uniformPasswordId; // Used for unite password change and validate
	private Boolean supportPasswordFilter;
	private Boolean includeEcho; // Returned account will contains echo record in embedded
	private UUID systemMapping;
	private UUID formDefinitionId;
	private String externalId;
	private Boolean hasFormDefinition;
	
	public AccAccountFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public AccAccountFilter(MultiValueMap<String, Object> data) {
		super(AccAccountDto.class, data);
	}
	
	public UUID getSystemEntityId() {
		return systemEntityId;
	}
	
	public void setSystemEntityId(UUID systemEntityId) {
		this.systemEntityId = systemEntityId;
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}
	
	public List<UUID> getSystems() {
		return getParameterConverter().toUuids(getData(), PARAMETER_SYSTEM_IDS);
	}

	public void setSystems(List<UUID> systemIds) {
		put(PARAMETER_SYSTEM_IDS, systemIds);
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Boolean getOwnership() {
		return ownership;
	}

	public void setOwnership(Boolean ownership) {
		this.ownership = ownership;
	}

	public Boolean getSupportChangePassword() {
		return supportChangePassword;
	}

	public void setSupportChangePassword(Boolean supportChangePassword) {
		this.supportChangePassword = supportChangePassword;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getEntityType() {
		return entityType;
	}

	public Boolean getInProtection() {
		return inProtection;
	}

	public void setInProtection(Boolean inProtection) {
		this.inProtection = inProtection;
	}

	public UUID getUniformPasswordId() {
		return uniformPasswordId;
	}

	public void setUniformPasswordId(UUID uniformPasswordId) {
		this.uniformPasswordId = uniformPasswordId;
	}

	public Boolean getSupportPasswordFilter() {
		return supportPasswordFilter;
	}

	public void setSupportPasswordFilter(Boolean supportPasswordFilter) {
		this.supportPasswordFilter = supportPasswordFilter;
	}

	public Boolean getIncludeEcho() {
		return includeEcho;
	}

	public void setIncludeEcho(Boolean includeEcho) {
		this.includeEcho = includeEcho;
	}

	public UUID getSystemMapping() {
		return systemMapping;
	}

	public void setSystemMapping(UUID systemMapping) {
		this.systemMapping = systemMapping;
	}

	public UUID getFormDefinitionId() {
		return formDefinitionId;
	}

	public void setFormDefinitionId(UUID formDefinitionId) {
		this.formDefinitionId = formDefinitionId;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public Boolean getHasFormDefinition() {
		return hasFormDefinition;
	}

	public void setHasFormDefinition(Boolean hasFormDefinition) {
		this.hasFormDefinition = hasFormDefinition;
	}
	
	public List<UUID> getRoleIds() {
		return getParameterConverter().toUuids(getData(), PARAMETER_ROLE_IDS);
	}

	public void setRoleIds(List<UUID> roleIds) {
		put(PARAMETER_ROLE_IDS, roleIds);
	}
	
	public List<UUID> getIdentities() {
		return getParameterConverter().toUuids(getData(), PARAMETER_IDENTITY_IDS);
	}

	public void setIdentities(List<UUID> identityIds) {
		put(PARAMETER_IDENTITY_IDS, identityIds);
	}
}
