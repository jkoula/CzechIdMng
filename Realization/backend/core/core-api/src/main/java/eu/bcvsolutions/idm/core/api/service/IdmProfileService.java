package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import eu.bcvsolutions.idm.core.api.config.domain.PrivateIdentityConfiguration;
import eu.bcvsolutions.idm.core.api.dto.IdmProfileDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmProfileFilter;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with profiles.
 * 
 * @author Radek Tomiška
 * @since 9.0.0
 */
public interface IdmProfileService extends 
		EventableDtoService<IdmProfileDto, IdmProfileFilter>,
		AuthorizableService<IdmProfileDto>,
		ScriptEnabled {
	
	/**
	 * Return profile for given identity username / id.
	 *
	 * @param identityIdentifier identity username / id
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto findOneByIdentity(Serializable identityIdentifier, BasePermission... permission);
	
	/**
	 * Return profile for given identifier (id/ username), if profile doesn't exist
	 * create new one.
	 *
	 * @param identityIdentifier identity username / id
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto findOrCreateByIdentity(Serializable identityIdentifier, BasePermission... permission);
	
	/**
	 * Upload new image version for the given profile.
	 * 
	 * @param profile persisted profile
	 * @param data - one of image/* content type is required.
	 * @param fileName Original filer name 
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 * @throws ResultCodeException if content type is different than image (one of  image/*)
	 * @throws ResultCodeException if content size is higher than configured maximum (by {@link PrivateIdentityConfiguration#PROPERTY_IDENTITY_PROFILE_IMAGE_MAX_FILE_SIZE})
	 */
	IdmProfileDto uploadImage(IdmProfileDto profile, MultipartFile data, String fileName, BasePermission... permission);
	
	/**
	 * Delete profile image (all versions).
	 * 
	 * @param profile persisted profile
	 * @param permission permissions to evaluate (AND)
	 * @return
	 * @throws ForbiddenEntityException if authorization policies doesn't met
	 */
	IdmProfileDto deleteImage(IdmProfileDto profile, BasePermission... permission);
	
	/**
	 * Collapse given panel for given identity profile.
	 * 
	 * @param identityIdentifier identity username / id
	 * @param panelIdentifier panel identifier ~ uiKey
	 * @param permission permissions to evaluate (AND)
	 * @return updated identity profile
	 * @since 11.2.0
	 */
	IdmProfileDto collapsePanel(Serializable identityIdentifier, String panelIdentifier, BasePermission... permission);
	
	/**
	 * Expand given panel for given identity profile.
	 * 
	 * @param identityIdentifier identity username / id
	 * @param panelIdentifier panel identifier ~ uiKey
	 * @param permission permissions to evaluate (AND)
	 * @return updated identity profile
	 * @since 11.2.0
	 */
	IdmProfileDto expandPanel(Serializable identityIdentifier, String panelIdentifier, BasePermission... permission);
	
}
