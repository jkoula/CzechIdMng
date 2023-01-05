package eu.bcvsolutions.idm.acc.repository;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwner;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;

/**
 * System owner repository
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
public interface SysSystemOwnerRepository extends AbstractEntityRepository<SysSystemOwner> {

	List<SysSystemOwner> findAllBySystem(SysSystem system);

	/**
	 * Find system owner by system id
	 *
	 * @param systemId of system
	 * @return entity of system owner
	 */
	List<SysSystemOwner> findAllBySystem_Id(UUID systemId);
}
