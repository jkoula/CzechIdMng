package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;

import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface AccAccountRoleAssignmentService extends IdmRoleAssignmentService<AccAccountRoleAssignmentDto, AccAccountRoleAssignmentFilter>,
        ScriptEnabled {
    List<AccAccountRoleAssignmentDto> findByAccountId(UUID id);
}
