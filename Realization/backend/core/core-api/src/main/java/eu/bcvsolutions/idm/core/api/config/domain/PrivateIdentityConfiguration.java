package eu.bcvsolutions.idm.core.api.config.domain;

import java.util.ArrayList;
import java.util.List;

import eu.bcvsolutions.idm.core.api.service.Configurable;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;

/**
 * Configuration for identity (private - sec).
 * 
 * TODO: #813
 * 
 * @author Radek Tomiška
 *
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
		properties.add(DEFAULT_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE);
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
	
}
