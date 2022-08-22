package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface AccAccountRoleService extends IdmRoleAssignmentService<AccAccountRoleDto, AccAccountRoleFilter>,
        ScriptEnabled {
    List<AccAccountRoleDto> findByAccountId(UUID id);
}
