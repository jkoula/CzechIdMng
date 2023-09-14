package eu.bcvsolutions.idm.doc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.PropertyModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.notification.api.dto.NotificationConfigurationDto;
import eu.bcvsolutions.idm.core.security.api.domain.GroupPermission;
import eu.bcvsolutions.idm.doc.domain.DocGroupPermission;
import eu.bcvsolutions.idm.doc.domain.DocResultCode;

/**
 * Doc module descriptor
 *
 * @author Jirka Koula
 */
@Component
@PropertySource("classpath:module-" + DocModuleDescriptor.MODULE_ID + ".properties")
@ConfigurationProperties(prefix = "module." + DocModuleDescriptor.MODULE_ID + ".build", ignoreUnknownFields = true, ignoreInvalidFields = true)
public class DocModuleDescriptor extends PropertyModuleDescriptor {

	public static final String MODULE_ID = "doc";

	@Override
	public String getId() {
		return MODULE_ID;
	}

	/**
	 * Enables links to swagger documentation
	 */
	@Override
	public boolean isDocumentationAvailable() {
		return true;
	}

	@Override
	public List<NotificationConfigurationDto> getDefaultNotificationConfigurations() {
		List<NotificationConfigurationDto> configs = new ArrayList<>();
		return configs;
	}

	@Override
	public List<GroupPermission> getPermissions() {
		return Arrays.asList(DocGroupPermission.values());
	}

	@Override
	public List<ResultCode> getResultCodes() {
		return Arrays.asList(DocResultCode.values());
	}
}
