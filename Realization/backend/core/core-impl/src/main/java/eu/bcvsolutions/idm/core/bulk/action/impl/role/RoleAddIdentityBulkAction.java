package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk operation for assign role to identity from role side.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(RoleAddIdentityBulkAction.NAME)
@Description("Assign role to identity.")
public class RoleAddIdentityBulkAction extends AbstractAssignRoleBulkAction<IdmRoleDto, IdmRoleFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleAddIdentityBulkAction.class);
	//
	public static final String NAME = "core-role-add-identity-bulk-action";
	//
	@Autowired private IdmRoleService roleService;

	@Override
	public String getName() {
		return RoleAddIdentityBulkAction.NAME;
	}

	@Override
	protected OperationResult processDto(IdmRoleDto role) {
		assignRoles(getIdentities(), Lists.newArrayList(role.getId()));
		//
		return null;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(
				CoreGroupPermission.ROLE_READ, 
				CoreGroupPermission.ROLE_CANBEREQUESTED // ~ role can be assigned 
		);
	}
	
	@Override
	public <DTO extends AbstractDto> IdmProcessedTaskItemDto logItemProcessed(DTO item, OperationResult opResult) {
		if (item instanceof IdmRoleDto && opResult == null) {
			// we don't want to log roles, which are iterated only
			LOG.debug("Role [{}] was processed by bulk action.", item.getId());
			//
			return null;
		}
		return super.logItemProcessed(item, opResult);
	}

	@Override
	public ReadWriteDtoService<IdmRoleDto, IdmRoleFilter> getService() {
		return roleService;
	}
}
