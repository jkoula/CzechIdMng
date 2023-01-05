package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysSystemOwnerRole;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * System owner repository - by roles
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
public interface SysSystemOwnerRoleRepository extends AbstractEntityRepository<SysSystemOwnerRole> {

	/**
	 * Find system owner role by role
	 *
	 * @param roleId of role owner
	 * @return entity of SysSystemOwnerRole
	 */
	List<SysSystemOwnerRole> findAllByOwnerRole_Id(UUID roleId);

}
