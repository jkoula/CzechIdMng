package eu.bcvsolutions.idm.core.bulk.action.impl.rolerequest;

import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.RoleRequestException;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete given role requests.
 * 
 * @author Ondrej Husnik
 * 
 * @since 11.1.0
 *
 */
@Component(RoleRequestDeleteBulkAction.NAME)
@Description("Delete given role requests.")
public class RoleRequestDeleteBulkAction extends AbstractRemoveBulkAction<IdmRoleRequestDto, IdmRoleRequestFilter> {

	public static final String NAME = "role-request-delete-bulk-action";
	
	@Autowired
	private IdmRoleRequestService roleRequestService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLE_REQUEST_DELETE);
	}
	
	@Override
	protected OperationResult processDto(IdmRoleRequestDto dto) {
		// Check delete permission.
		getService().checkAccess(dto, IdmBasePermission.DELETE);
		// Request in Executed state can not be delete or change
		OperationResultDto systemState = dto.getSystemState();
		if (RoleRequestState.EXECUTED == dto.getState() && systemState != null
				&& OperationState.EXECUTED != systemState.getState()
				&& OperationState.CANCELED != systemState.getState()) {
			// Request was executed in IdM, but system state is not canceled -> we will change the system state to CANCELED.
			OperationResultDto systemResult = new OperationResultDto.Builder(OperationState.CANCELED)
					.setModel(new DefaultResultModel(CoreResultCode.ROLE_REQUEST_SYSTEM_STATE_CANCELED,
							ImmutableMap.of("state", systemState != null ? systemState.getState().name() : "")))
					.build();
			dto.setSystemState(systemResult);
			roleRequestService.save(dto);
			return new OperationResult.Builder(OperationState.EXECUTED).build();
		}
		
		// Request in Executed state can not be delete or change
		if (RoleRequestState.EXECUTED == dto.getState()) {
			throw new RoleRequestException(CoreResultCode.ROLE_REQUEST_EXECUTED_CANNOT_DELETE,
					ImmutableMap.of("request", dto));
		}
		
		// Only request in Concept state, can be deleted. In others states, will be
		// Request set to Canceled state and save.
		if (RoleRequestState.CONCEPT == dto.getState()) {
			roleRequestService.delete(dto);
		} else {
			roleRequestService.cancel(dto);
		}
		return new OperationResult.Builder(OperationState.EXECUTED).build(); 
	}
	
	@Override
	public ReadWriteDtoService<IdmRoleRequestDto, IdmRoleRequestFilter> getService() {
		return roleRequestService;
	}
}
