package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for identity (private ~ sec).
 * 
 * @author Radek Tomiška
 * @see IdentityConfiguration
 */
public interface PrivateIdentityConfiguration extends Configurable {

	/**
	 * Profile image max file size in readable string format (e.g. 200KB).
	 * 
	 * @since 11.2.0
	 */
	String PROPERTY_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.identity.profile.image.max-file-size";
	String DEFAULT_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE = "512KB";
	
	/**
	 * Creates default identity's contract, when new identity is created.
	 * 
	 * @since 11.2.0
	 */
	String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.identity.create.defaultContract.enabled";
	boolean DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED = true;
	
	/**
	 * Creates default identity's contract with configured position name.
	 * 
	 * @since 11.2.0
	 */
	String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_POSITION = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.identity.create.defaultContract.position";
	String DEFAULT_IDENTITY_CREATE_DEFAULT_CONTRACT_POSITION = "Default";
	
	/**
	 * Creates default identity's contract with configured state.
	 * 
	 * @since 11.2.0
	 */
	String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_STATE = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.identity.create.defaultContract.state";
	
	/**
	 * Number of days related to current date - will be used for set contract valid till date (current date + expiration in days = valid till).
	 * Contact valid till will not be set by default (~ contract expiration is not configured by default).
	 * 
	 * @since 11.2.0
	 */
	String PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_EXPIRATION = 
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.identity.create.defaultContract.expiration";
	
	@Override
	default String getConfigurableType() {
		return "identity";
	}
	
	@Override
	default boolean isDisableable() {
		return false;
	}
	
	@Override
	default public boolean isSecured() {
		return true;
	}
	
	@Override
	default List<String> getPropertyNames() {
		List<String> properties = new ArrayList<>(); // we are not using superclass properties - enable and order does not make a sense here
		properties.add(PROPERTY_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE);
		properties.add(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_ENABLED);
		properties.add(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_POSITION);
		properties.add(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_STATE);
		properties.add(PROPERTY_IDENTITY_CREATE_DEFAULT_CONTRACT_EXPIRATION);
		return properties;
	}
	
	/**
	 * Returns public configuration.
	 * 
	 * @return
	 */
	IdentityConfiguration getPublicConfiguration();
	
	/**
	 * Max file size for uploaded profile images.
	 * 
	 * @return file size in bytes.
	 * @since 11.2.0
	 */
	long getProfileImageMaxFileSize();
	
	/**
	 * Default contract will be created with new identity.
	 * 
	 * @return true, when default contract will be created with new identity.
	 * @since 11.2.0
	 */
	boolean isCreateDefaultContractEnabled();
	
	/**
	 * Default contract will be created with configured position name.
	 * 
	 * @return default contract position 
	 * @since 11.2.0
	 */
	String getCreateDefaultContractPosition();
	
	/**
	 * Number of days related to current date - will be used for set contract valid till date (current date + expiration in days = valid till).
	 * Contact valid till will not be set by default (~ contract expiration is not configured by default).
	 * 
	 * @return expiration in days
	 */
	Long getCreateDefaultContractExpiration();
	
	/**
	 * Default contract will be created with given state.
	 * 
	 * @return configured state or {@code null} (by default) 
	 */
	ContractState getCreateDefaultContractState();
}
