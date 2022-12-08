package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.List;

import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityRoleProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;

/**
 * Save identity role
 * 
 * @author Radek Tomiška
 * @author Vít Švanda
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
@Component(IdentityRoleSaveProcessor.PROCESSOR_NAME)
@Description("Persists identity role.")
public class IdentityRoleSaveProcessor 
		extends AbstractRoleAssignmentSaveProcessor<IdmIdentityRoleDto>
		implements IdentityRoleProcessor {

	public static final String PROCESSOR_NAME = "identity-role-save-processor";

	private final IdmIdentityRoleValidRequestService validRequestService;

	@Autowired
	public IdentityRoleSaveProcessor(
			IdmIdentityRoleService service,
			IdmIdentityRoleValidRequestService validRequestService) {
		super(service, AbstractRoleAssignmentEvent.RoleAssignmentEventType.CREATE, AbstractRoleAssignmentEvent.RoleAssignmentEventType.UPDATE);
		this.validRequestService = validRequestService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	protected void createRoleValidReqForInvalidRole(IdmIdentityRoleDto roleAssignment) {
		validRequestService.createByIdentityRoleId(roleAssignment.getId());
	}

}