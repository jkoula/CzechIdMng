package eu.bcvsolutions.idm.core.model.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Identity roles.
 * 
 * @author Radek Tomiška
 */
@NoRepositoryBean
public interface IdmRoleAssignmentRepository<E extends AbstractRoleAssignment> extends AbstractEntityRepository<E> {
	

	/**
	 * @deprecated @since 11.1.0 use count method in service.
	 */
	@Deprecated
	Long countByRole_Id(UUID roleId);

	/**
	 * Assigned roles by role identifier.
	 *
	 * @param role
	 * @param pageable
	 * @return
	 * @deprecated @since 10.0.0 use find method in service.
	 */
	@Deprecated
	Page<E> findByRole(@Param("role") E role, Pageable pageable);

	/**
	 * Returns assigned roles by given automatic role.
	 *
	 * @param automaticRoleId
	 * @param pageable
	 * @return
	 */
	Page<E> findByAutomaticRole_Id(@Param("automaticRoleId") UUID automaticRoleId, Pageable pageable);

	/**
	 * Returns all roles with date lower than given expiration date.
	 *
	 * @param expirationDate valid till < expirationDate
	 * @param page add sort if needed
	 * @return all expired roles
	 * @see #findDirectExpiredRoles(LocalDate, Pageable)
	 */
	@Query(value = "select e from #{#entityName} e"
			+ " where e.validTill is not null and e.validTill < :expirationDate")
	Page<E> findExpiredRoles(@Param("expirationDate") LocalDate expirationDate, Pageable page);

	/**
	 * Returns all direct roles with date lower than given expiration date. Automatic roles are included, sub roles not.
	 *
	 * @param expirationDate valid till < expirationDate
	 * @param page add sort if needed
	 * @return expired roles without sub roles
	 * @since 10.2.0
	 */
	@Query(value = "select e from #{#entityName} e"
			+ " where e.directRole is null and e.validTill is not null and e.validTill < :expirationDate")
	Page<E> findDirectExpiredRoles(@Param("expirationDate") LocalDate expirationDate, Pageable page);

	/**
	 * Returns all direct roles with date lower than given expiration date. Automatic roles are included, sub roles not.
	 *
	 * @param expirationDate valid till < expirationDate
	 * @return expired role identifiers (without sub roles)
	 * @since 10.6.5, 10.7.2
	 */
	@Query(value = "select e.id from #{#entityName} e"
			+ " where e.directRole is null and e.validTill is not null and e.validTill < :expirationDate")
	List<UUID> findDirectExpiredRoleIds(@Param("expirationDate") LocalDate expirationDate);
}
