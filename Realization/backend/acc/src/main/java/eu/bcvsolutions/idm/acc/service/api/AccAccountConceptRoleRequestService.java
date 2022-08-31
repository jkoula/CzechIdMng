package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;

/**
 * Service for concept role request
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@Deprecated
public interface AccAccountConceptRoleRequestService
		extends IdmGeneralConceptRoleRequestService<AccAccountRoleAssignmentDto, AccAccountConceptRoleRequestDto, AccAccountConceptRoleRequestFilter> {

}
