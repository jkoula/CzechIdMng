package eu.bcvsolutions.idm.core.api.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;

/**
 * Operations with identity roles
 * 
 * @author svanda
 * @author Radek Tomiška
 * @author Ondrej Kopr
 *
 */
public interface IdmIdentityRoleService extends
		IdmRoleAssignmentService<IdmIdentityRoleDto, IdmIdentityRoleFilter>,
		ScriptEnabled {
	
	String SKIP_CHECK_AUTHORITIES = "skipCheckAuthorities";
	
	/**
	 * Returns all identity's roles
	 * 
	 * @param identityId
	 * @return
	 */
	List<IdmIdentityRoleDto> findAllByIdentity(UUID identityId);
	
	/**
	 * Returns all roles related to given {@link IdmIdentityContractDto}
	 * 
	 * @param identityContractId
	 * @return
	 */
	List<IdmIdentityRoleDto> findAllByContract(UUID identityContractId);
	
	/**
	 * Returns all roles related to given {@link IdmContractPositionDto}
	 * 
	 * @param contractPositionId
	 * @return
	 */
	List<IdmIdentityRoleDto> findAllByContractPosition(UUID contractPositionId);

	/**
	 * Find valid identity-roles in this moment. Includes check on contract validity. 
	 * 
	 * @param identityId
	 * @param pageable
	 * @return
	 */
	Page<IdmIdentityRoleDto> findValidRoles(UUID identityId, Pageable pageable);


}
