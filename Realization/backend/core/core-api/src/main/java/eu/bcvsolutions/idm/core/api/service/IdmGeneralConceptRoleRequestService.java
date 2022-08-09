package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

import java.util.List;
import java.util.UUID;

/**
 * Service for concept role request
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public interface IdmGeneralConceptRoleRequestService<D extends AbstractConceptRoleRequestDto,F extends IdmBaseConceptRoleRequestFilter>
		extends ReadWriteDtoService<D, F>,
		AuthorizableService<D> {

	void addToLog(Loggable logItem, String text);

	/**
	 * Finds all concepts for this request
	 * 
	 * @param roleRequestId
	 * @return
	 */
	List<D> findAllByRoleRequest(UUID roleRequestId);

	/**
	 * Set concept state to CANCELED and stop workflow process (connected to this
	 * concept)
	 * 
	 * @param dto
	 */
	D cancel(D dto);

	/**
	 * Return form instance for given concept. Values contains changes evaluated
	 * against the identity-role form values.
	 * 
	 * @param dto
	 * @param checkChanges If true, then changes against the identity role will be evaluated.
	 * @return
	 */
	IdmFormInstanceDto getRoleAttributeValues(D dto, boolean checkChanges);

	/**
	 * Validate form attributes for given concept
	 * @param concept
	 * @return
	 */
	List<InvalidFormAttributeDto> validateFormAttributes(D concept);

}
