package eu.bcvsolutions.idm.acc.service.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerFilter;
import eu.bcvsolutions.idm.core.api.service.EventableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with system owners - by identity
 *
 * @author Roman Kucera
 * @since 12.3.0
 */
public interface SysSystemOwnerService extends EventableDtoService<SysSystemOwnerDto, SysSystemOwnerFilter>,
		AuthorizableService<SysSystemOwnerDto> {

	/**
	 * System owner by system
	 *
	 * @param systemId   id of system
	 * @param pageable   options
	 * @param permission permissions
	 * @return Page of owners
	 */
	Page<SysSystemOwnerDto> findBySystem(UUID systemId, Pageable pageable, BasePermission... permission);

}
