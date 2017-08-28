package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.core.api.service.CloneableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * System entity handling service
 * @author svandav
 *
 */
public interface SysSystemMappingService extends ReadWriteDtoService<SysSystemMappingDto, SystemMappingFilter>, CloneableService<SysSystemMappingDto> {

	public List<SysSystemMappingDto> findBySystem(SysSystem system, SystemOperationType operation, SystemEntityType entityType);
	
	public List<SysSystemMappingDto> findByObjectClass(SysSchemaObjectClassDto objectClass, SystemOperationType operation, SystemEntityType entityType);

	/**
	 * Is enabled protection of account against delete
	 * @param account
	 * @return
	 */
	boolean isEnabledProtection(AccAccount account);

	/**
	 * Interval of protection against account delete
	 * @param account
	 * @return
	 */
	Integer getProtectionInterval(AccAccount account);
}
