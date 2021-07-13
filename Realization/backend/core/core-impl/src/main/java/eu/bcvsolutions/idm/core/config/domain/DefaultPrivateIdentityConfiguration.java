package eu.bcvsolutions.idm.core.config.domain;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;

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
}
