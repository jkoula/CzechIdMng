package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Loggable;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmBaseConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Service for concept role request
 * 
 * @author svandav
 *
 */
public interface IdmConceptRoleRequestService
		extends IdmGeneralConceptRoleRequestService<IdmConceptRoleRequestDto, IdmConceptRoleRequestFilter> {

	String IDENTITY_CONTRACT_FIELD = "identityContract";
	String ROLE_REQUEST_FIELD = "roleRequest";

}
