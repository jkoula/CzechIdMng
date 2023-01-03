package eu.bcvsolutions.idm.acc.repository;

import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignmentValidRequest;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRoleValidRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * 
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public interface AccAccountRoleAssignmentValidRequestRepository extends AbstractEntityRepository<AccAccountRoleAssignmentValidRequest> {
	
	/**
	 * Find all {@link IdmIdentityRoleValidRequest} thats is valid from given in parameter.
	 * @param from
	 * @return
	 */
	@Query(value = "SELECT e FROM #{#entityName} e "
	        + "WHERE "
	        + "e.accountRoleAssignment.validFrom <= :from")
	List<AccAccountRoleAssignmentValidRequest> findAllValidFrom(@Param("from") LocalDate from);
	
	/**
	 * Find all {@link AccAccountRoleAssignmentValidRequest} for account
	 * @param accAccountId
	 * @return
	 */
	List<AccAccountRoleAssignmentValidRequest> findByAccountRoleAssignment_Account(@Param("accAccountId") UUID accAccountId);
	
	/**
	 * Find all {@link AccAccountRoleAssignmentValidRequest} for role
	 * @param role
	 * @return
	 */
	List<AccAccountRoleAssignmentValidRequest> findAllByAccountRoleAssignment_Role_Id(@Param("roleId") UUID roleId);
	
	/**
	 * Find all {@link AccAccountRoleAssignmentValidRequest} for identityRole
	 * @param role
	 * @return
	 */
	List<AccAccountRoleAssignmentValidRequest> findAllByAccountRoleAssignment_Id(@Param("accountRoleAssignmentId") UUID accountRoleAssignmentId);


	AccAccountRoleAssignmentValidRequest findOneByAccountRoleAssignment_Id(@Param("accountRoleAssignmentId") UUID identityRoleId);
}
