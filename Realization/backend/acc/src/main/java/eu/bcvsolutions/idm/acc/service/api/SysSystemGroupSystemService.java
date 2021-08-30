package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import java.util.List;

/**
 * System groups system - relation between a system and a group of systems.
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public interface SysSystemGroupSystemService extends
		EventableDtoService<SysSystemGroupSystemDto, SysSystemGroupSystemFilter>,
		AuthorizableService<SysSystemGroupSystemDto> {

	/**
	 * Find all systems in enabled cross-domains groups where is used given system.
	 */
	List<SysSystemGroupSystemDto> getSystemsInCrossDomainGroup(SysSystemDto system);
}





