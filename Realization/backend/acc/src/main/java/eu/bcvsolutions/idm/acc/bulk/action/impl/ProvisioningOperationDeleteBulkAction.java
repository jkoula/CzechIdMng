package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysProvisioningOperationFilter;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningOperationService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Delete given provisioning operation.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(ProvisioningOperationDeleteBulkAction.NAME)
@Description("Delete given remote server.")
public class ProvisioningOperationDeleteBulkAction extends AbstractRemoveBulkAction<SysProvisioningOperationDto, SysProvisioningOperationFilter> {

	public static final String NAME = "acc-provisioning-operation-delete-bulk-action";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProvisioningOperationDeleteBulkAction.class);
	//
	@Autowired private SysProvisioningOperationService provisioningOperationService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.PROVISIONING_OPERATION_DELETE);
	}
	
	@Override
	protected SysProvisioningOperationDto getDtoById(UUID id) {
		SysProvisioningOperationDto dto = super.getDtoById(id);
		//
		if (dto == null) {
			// try to find provisioning operation by id => NotFound(Ignore) annotation will be effective
			SysProvisioningOperationFilter filter = new SysProvisioningOperationFilter();
			filter.setId(id);
			List<SysProvisioningOperationDto> operations = getService().find(filter, PageRequest.of(0, 1)).getContent();
			if (operations.size() == 1) {
				LOG.warn("Invalid provisioning operation [{}] will be deleted.", id);
				//
				return operations.get(0);
			}
		}
		//
		return dto;
	}
	
	@Override
	protected OperationResult processDto(SysProvisioningOperationDto dto) {
		if (dto.getSystemEntity() != null) {
			return super.processDto(dto);
		}
		//
		// delete invalid provisioning operation by internal method => leads to raw delete from database
		try {
			getService().deleteInternal(dto);
		} catch (Exception ex) {
			return new OperationResult.Builder(OperationState.EXCEPTION).setCause(ex).build(); // cannot be result code exception
		}
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	protected boolean checkPermissionForEntity(BaseDto entity) {
		// FIXME: AbstractBulkAction - DTO template should be used ... works only thx to decorator in service layer (id or dto can be used).
		return PermissionUtils.hasPermission(getService().getPermissions((SysProvisioningOperationDto) entity),
				getPermissionForEntity());
	}

	@Override
	public ReadWriteDtoService<SysProvisioningOperationDto, SysProvisioningOperationFilter> getService() {
		return provisioningOperationService;
	}
}
