package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccRoleAssignmentValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.RoleAssignmentValidRequestService;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for create and read account role valid requests.
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */

public interface AccAccountRoleAssignmentValidRequestService extends ReadWriteDtoService<AccRoleAssignmentValidRequestDto, EmptyFilter>, RoleAssignmentValidRequestService<AccRoleAssignmentValidRequestDto> {

	AccRoleAssignmentValidRequestDto createByAccountRoleId(UUID identityRole);
	
	/**
	 * Method find all {@link AccRoleAssignmentValidRequestDto} that can be process from {@value from} given in parameter.
	 * @param from
	 * @return
	 */
	List<AccRoleAssignmentValidRequestDto> findAllValidFrom(ZonedDateTime from);
	
	/**
	 * Find all {@link AccRoleAssignmentValidRequestDto} for role
	 * @param role
	 * @return
	 */
	List<AccRoleAssignmentValidRequestDto> findAllValidRequestForRoleId(UUID role);
	
	/**
	 * Find all {@link AccRoleAssignmentValidRequestDto} for identity
	 * @param account
	 * @return
	 */
	List<AccRoleAssignmentValidRequestDto> findAllValidRequestForAccountId(UUID account);
	
	/**
	 * Find all {@link AccRoleAssignmentValidRequestDto} for identityRole
	 * @param roleAssignmentId
	 * @return
	 */
	List<AccRoleAssignmentValidRequestDto> findAllValidRequestForRoleAssignmentId(UUID roleAssignmentId);

	
	/**
	 * Remove all entities {@link AccRoleAssignmentValidRequestDto} check for null and empty list.
	 * @param entities
	 */
	void deleteAll(List<AccRoleAssignmentValidRequestDto> entities);
}
