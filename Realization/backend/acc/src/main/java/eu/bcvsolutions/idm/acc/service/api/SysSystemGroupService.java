package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSystemGroupDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupFilter;
import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * System groups service (cross-domain)
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
public interface SysSystemGroupService extends
		EventableDtoService<SysSystemGroupDto, SysSystemGroupFilter>,
		AuthorizableService<SysSystemGroupDto>,
		CodeableService<SysSystemGroupDto> {
	
}





