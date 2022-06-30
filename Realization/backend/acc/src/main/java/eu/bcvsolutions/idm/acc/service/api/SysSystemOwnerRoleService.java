package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerRoleFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with system owners - roles
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
public interface SysSystemOwnerRoleService extends EventableDtoService<SysSystemOwnerRoleDto, SysSystemOwnerRoleFilter>,
		AuthorizableService<SysSystemOwnerRoleDto> {
}
