package eu.bcvsolutions.idm.core.bulk.action.impl.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.bulk.action.impl.role.AbstractAssignRoleBulkAction;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk operation for assign role to identity from role detail side.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(IdentityAddRoleWithoutSelectionBulkAction.NAME)
@Description("Assign role to identity.")
public class IdentityAddRoleWithoutSelectionBulkAction extends AbstractAssignRoleBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityAddRoleWithoutSelectionBulkAction.class);
	//
	public static final String NAME = "core-identity-add-role-without-selection-bulk-action";
	//
	@Autowired private IdmIdentityService identityService;

	@Override
	public String getName() {
		return IdentityAddRoleWithoutSelectionBulkAction.NAME;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		//
		formAttributes.add(0, getRoleAttribute());
		//
		return formAttributes;
	}
	
	@Override
	public boolean showWithSelection() {
		return false;
	}
	
	@Override
	public boolean showWithoutSelection() {
		return true;
	}
	
	@Override
	protected List<UUID> getAllEntities(IdmBulkActionDto action, StringBuilder description) {
		return getIdentities();
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		assignRoles(Lists.newArrayList(identity.getId()), getRoles());
		//
		return null;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(
				CoreGroupPermission.IDENTITY_READ
		);
	}
	
	@Override
	public <DTO extends AbstractDto> IdmProcessedTaskItemDto logItemProcessed(DTO item, OperationResult opResult) {
		if (item instanceof IdmIdentityDto && opResult == null) {
			// we don't want to log roles, which are iterated only
			LOG.debug("Role [{}] was processed by bulk action.", item.getId());
			//
			return null;
		}
		return super.logItemProcessed(item, opResult);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
