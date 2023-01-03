package eu.bcvsolutions.idm.acc.service.impl.adapter;

import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.service.impl.adapter.AbstractRoleAssignmentConceptCompilingAdapter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

import java.util.List;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class DefaultAccountConceptRoleAdapter extends AbstractRoleAssignmentConceptCompilingAdapter<AccAccountRoleAssignmentDto, AccAccountConceptRoleRequestDto, AccAccountConceptRoleRequestFilter> {

    private final AccAccountRoleAssignmentService accAccountRoleAssignmentService;

    public DefaultAccountConceptRoleAdapter(IdmRequestIdentityRoleFilter originalFilter, BasePermission permission,
            AccAccountConceptRoleRequestService conceptRoleRequestService,
            AccAccountRoleAssignmentService accAccountRoleAssignmentService) {
        super(originalFilter, permission, conceptRoleRequestService);
        this.accAccountRoleAssignmentService = accAccountRoleAssignmentService;
    }


    @Override
    protected IdmFormInstanceDto getFormInstance(AccAccountRoleAssignmentDto roleAssignment) {
        return accAccountRoleAssignmentService.getRoleAttributeValues(roleAssignment);
    }

    @Override
    protected IdmRequestIdentityRoleDto roleAssignmentToReqIdentityRole(AccAccountRoleAssignmentDto roleAssignment) {
        IdmRequestIdentityRoleDto result = new IdmRequestIdentityRoleDto();
        result.setOwnerUuid(roleAssignment.getAccount());
        result.setAssignmentType(AccAccountConceptRoleRequestDto.class);
        result.setOwnerType(AccAccountDto.class);
        result.getEmbedded().putAll(roleAssignment.getEmbedded());
        return result;
    }

    @Override
    protected List<InvalidFormAttributeDto> validateFormAttributes(AccAccountRoleAssignmentDto roleAssignment) {
        return accAccountRoleAssignmentService.validateFormAttributes(roleAssignment);
    }
}
