package eu.bcvsolutions.idm.core.config.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;

/**
 * Configuration for identity (private - sec).
 * 
 * @author Radek Tomiška
 *
 */
@Component("privateIdentityConfiguration")
public class DefaultPrivateIdentityConfiguration extends AbstractConfiguration implements PrivateIdentityConfiguration {	
	
	@Autowired private IdentityConfiguration publicConfiguration;
	
	@Override
	public IdentityConfiguration getPublicConfiguration() {
		return publicConfiguration;
	}
	
	@Override
	public long getProfileImageMaxFileSize() {
		String maxFileSize = getConfigurationService().getValue(PROPERTY_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE);
		if (StringUtils.isBlank(maxFileSize)) {
			return DataSize.parse(DEFAULT_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE).toBytes();
		}
		//
		return DataSize.parse(maxFileSize, DataUnit.BYTES).toBytes();
	}
	
	@Override
	public boolean isCreateDefaultContractEnabled() {
		// new configuration - higher priority
		String newConfiguration = getConfigurationService().getValue(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		if (StringUtils.isNotBlank(newConfiguration)) {
			return Boolean.parseBoolean(newConfiguration);
		}
		// backward compatible
		@SuppressWarnings("deprecation")
		String previousConfiguration = getConfigurationService().getValue(IdentityConfiguration.PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT);
		if (StringUtils.isNotBlank(previousConfiguration)) {
			return Boolean.parseBoolean(previousConfiguration);
		}
		// default (same as previous)
		return DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED;
	}
	
	@Override
	public String getCreateDefaultContractPosition() {
		return getConfigurationService().getValue(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_POSITION, DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT_POSITION);
	}
	
	@Override
	public Long getCreateDefaultContractExpiration() {
		return getConfigurationService().getLongValue(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_EXPIRATION);
	}
	
	@Override
	public ContractState getCreateDefaultContractState() {
		String contractState = getConfigurationService().getValue(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_STATE);
		if (StringUtils.isBlank(contractState)) {
			return null;
		}
		//
		return ContractState.valueOf(contractState.trim().toUpperCase());
	}
}
