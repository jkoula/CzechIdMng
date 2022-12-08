package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.InvalidFormAttributeDto;
import eu.bcvsolutions.idm.core.model.service.impl.adapter.AbstractRoleAssignmentConceptCompilingAdapter;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

import java.util.List;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class IdentityRoleConceptCompilingAdapter extends AbstractRoleAssignmentConceptCompilingAdapter<IdmIdentityRoleDto, IdmConceptRoleRequestDto, IdmConceptRoleRequestFilter> {

    private final IdmIdentityRoleService identityRoleService;

    public IdentityRoleConceptCompilingAdapter(IdmRequestIdentityRoleFilter originalFilter, BasePermission permission,
            IdmConceptRoleRequestService conceptService, IdmIdentityRoleService identityRoleService) {
        super(originalFilter, permission, conceptService);
        this.identityRoleService = identityRoleService;
    }

    @Override
    protected IdmFormInstanceDto getFormInstance(IdmIdentityRoleDto identityRole) {
        return identityRoleService.getRoleAttributeValues(identityRole);
    }

    @Override
    protected IdmRequestIdentityRoleDto roleAssignmentToReqIdentityRole(IdmIdentityRoleDto identityRole) {
        IdmRequestIdentityRoleDto request = new IdmRequestIdentityRoleDto();
        request.setOwnerUuid(identityRole.getIdentityContract());
        return request;
    }

    @Override
    protected List<InvalidFormAttributeDto> validateFormAttributes(IdmIdentityRoleDto identityRole) {
        return identityRoleService.validateFormAttributes(identityRole);
    }
}
