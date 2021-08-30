package eu.bcvsolutions.idm.core.api.service.thin;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.thin.IdmRoleThinDto;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Operations with roles - thin variant:
 * - supports get method only.
 * 
 * @author Vít Švanda
 * @since 11.2.0
 */
public interface IdmRoleThinService extends
	ReadDtoService<IdmRoleThinDto, IdmRoleFilter>,
	AuthorizableService<IdmRoleThinDto>,
	ScriptEnabled {
	
}
