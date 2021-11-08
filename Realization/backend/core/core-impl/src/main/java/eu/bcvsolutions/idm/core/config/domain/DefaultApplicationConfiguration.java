package eu.bcvsolutions.idm.core.config.domain;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.config.domain.AbstractConfiguration;
import eu.bcvsolutions.idm.core.api.config.domain.ApplicationConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.dto.theme.ThemeDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmConfigurationService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.ecm.api.dto.IdmAttachmentDto;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.core.ecm.api.service.AttachmentManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Common application configuration.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public class DefaultApplicationConfiguration extends AbstractConfiguration implements ApplicationConfiguration, Identifiable {	

	private static final long serialVersionUID = 1L;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultApplicationConfiguration.class);
	//
	private String backendUrl = null;
	//
	@Autowired private ObjectMapper mapper;
	@Autowired private AttachmentManager attachmentManager;
	@Autowired private IdmConfigurationService configurationService;
	
	@Override
	public Serializable getId() {
		return null; // UUID identifiable is not supported, but interface is required for upload attachments.
	}
	
	@Override
	public String getStage() {
		String stage = getConfigurationService().getValue(PROPERTY_STAGE);
		if (StringUtils.isBlank(stage)) {
			LOG.debug("Stage by property [{}] is not configured, return default [{}]", PROPERTY_STAGE, DEFAULT_STAGE);
			return DEFAULT_STAGE;
		}
		//
		return stage;
	}
	
	@Override
	public boolean isDevelopment() {
		return STAGE_DEVELOPMENT.equalsIgnoreCase(getStage());
	}
	
	@Override
	public boolean isProduction() {
		return STAGE_PRODUCTION.equalsIgnoreCase(getStage());
	}
	
	@Override
	public String getFrontendUrl() {
		return getConfigurationService().getFrontendUrl("");
	}
	
	@Override
	public String getBackendUrl(HttpServletRequest request) {
		String configuredBackendUrl = getConfigurationService().getValue(PROPERTY_BACKEND_URL);
		if (StringUtils.isNotBlank(configuredBackendUrl)) {
			return configuredBackendUrl;
		}
		// from cache
		if (StringUtils.isNotBlank(backendUrl)) {
			return backendUrl;
		}
		// try to resolve from given request
		if (request == null) {
			return null;
		}
		//
		backendUrl = ServletUriComponentsBuilder
				.fromRequest(request)
				.replacePath(request.getContextPath())
				.replaceQuery(null)
				.build()
				.toUriString();
		//
		return backendUrl;
	}
	
	@Override
	public UUID getApplicationLogoId() {
		String applicationLogoId = getConfigurationService().getValue(PROPERTY_APPLICATION_LOGO);
		if (StringUtils.isBlank(applicationLogoId)) {
			return null;
		}
		//
		return DtoUtils.toUuid(applicationLogoId);
	}
	
	@Override
	@Transactional
	public UUID uploadApplicationLogo(MultipartFile data, String fileName, BasePermission... permission) {
		// check required image content type
		String contentType = data.getContentType();
		if (StringUtils.isBlank(contentType) || !contentType.toLowerCase().startsWith("image/")) {
			throw new ResultCodeException(
					CoreResultCode.IDENTITY_PROFILE_IMAGE_WRONG_CONTENT_TYPE, 
					ImmutableMap.of("contentType", String.valueOf(contentType))
			);
		}
		//
		// check access
		IdmConfigurationDto configuration = configurationService.getByCode(PROPERTY_APPLICATION_LOGO);
		if (configuration == null) { // new configuration property
			configuration = new IdmConfigurationDto();
			configuration.setName(PROPERTY_APPLICATION_LOGO);
			//
			if (!PermissionUtils.isEmpty(permission) 
					&& Sets.newHashSet(PermissionUtils.trimNull(permission)).contains(IdmBasePermission.CREATE)) {
				configurationService.checkAccess(configuration, IdmBasePermission.CREATE);
			}
		} else if (!PermissionUtils.isEmpty(permission) // update property
				&& Sets.newHashSet(PermissionUtils.trimNull(permission)).contains(IdmBasePermission.UPDATE)) {
			configurationService.checkAccess(configuration, IdmBasePermission.UPDATE);
		}
		//
		IdmAttachmentDto attachment = new IdmAttachmentDto();
		attachment.setName(fileName);
		attachment.setOwnerType(ApplicationConfiguration.class.getCanonicalName());
		attachment.setOwnerId(UUID.randomUUID()); // ~ application configuration cannot have uuid
		attachment.setMimetype(StringUtils.isBlank(data.getContentType()) ? AttachableEntity.DEFAULT_MIMETYPE : data.getContentType());
		//
		UUID applicationLogoId = getApplicationLogoId();
		IdmAttachmentDto previousAttachment = null;
		if (applicationLogoId != null) {
			previousAttachment = attachmentManager.get(applicationLogoId);
		}
		//
		try {
			attachment.setInputData(data.getInputStream());
			if (previousAttachment == null) {
				attachment = attachmentManager.saveAttachment(this, attachment);
			} else {
				attachment = attachmentManager.saveAttachmentVersion(this, attachment, previousAttachment);
			}
			UUID attachmentId = attachment.getId();
			configuration.setValue(attachmentId.toString());
			//
			configurationService.save(configuration); // permissions are evaluated before attachment is saved
			//
			return attachmentId;
		} catch (IOException ex) {
			throw new ResultCodeException(CoreResultCode.ATTACHMENT_CREATE_FAILED, ImmutableMap.of(
					"attachmentName", attachment.getName(),
					"ownerType", attachment.getOwnerType(),
					"ownerId", "null") // ~ application configuration cannot have uuid
					, ex);
		}
	}
	
	@Override
	@Transactional
	public void deleteApplicationLogo(BasePermission... permission) {
		UUID attachmentId = getApplicationLogoId();
		if (attachmentId == null) {
			// nothing to delete
			return;
		}
		IdmConfigurationDto configuration = configurationService.getByCode(PROPERTY_APPLICATION_LOGO);
		//
		configurationService.delete(configuration, permission);
		// delete attachment after configuration is deleted => permissions are evaluated
		attachmentManager.deleteAttachment(attachmentId);
	}
	
	@Override
	public ThemeDto getApplicationTheme() {
		String themeJson = getConfigurationService().getValue(PROPERTY_APPLICATION_THEME);
		if (StringUtils.isBlank(themeJson)) {
			return null; // ~ not configured
		}
		//
		try {
			return mapper.readValue(themeJson, ThemeDto.class);
		} catch (IOException ex) {
			LOG.warn("Application theme is wrongly configured. Fix configured application theme [{}], default theme will be used till then.",
					PROPERTY_APPLICATION_THEME, ex);
			return null;
		}
	}
}
