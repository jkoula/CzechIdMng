package eu.bcvsolutions.idm.acc.service.impl.adapter;

import com.mchange.util.AssertException;
import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmGeneralConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.model.service.impl.adapter.DefaultRequestRoleConceptAdapter;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import org.modelmapper.ModelMapper;
import org.springframework.util.Assert;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class AccAccountConceptRoleRequestAdapter extends DefaultRequestRoleConceptAdapter<AccAccountConceptRoleRequestDto, AccAccountRoleAssignmentDto> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(AccAccountConceptRoleRequestAdapter.class);
    private final IdmRoleAssignmentService<AccAccountRoleAssignmentDto, ?> roleAssignmentService;
    IdmRequestIdentityRoleFilter originalFilter;

    public AccAccountConceptRoleRequestAdapter(IdmRoleAssignmentService<AccAccountRoleAssignmentDto, ?> roleAssignmentService,
            IdmGeneralConceptRoleRequestService<AccAccountRoleAssignmentDto, AccAccountConceptRoleRequestDto, ?> conceptRoleRequestService, IdmRoleSystemService roleSystemService,
            IdmRequestIdentityRoleFilter originalFilter, WorkflowProcessInstanceService workflowProcessInstanceService, ModelMapper modelMapper, IdmRoleRequestService roleRequestService) {
        super(roleAssignmentService, conceptRoleRequestService, roleRequestService, roleSystemService, originalFilter, workflowProcessInstanceService, modelMapper);
        this.originalFilter = originalFilter;
        this.roleAssignmentService = roleAssignmentService;
    }

    @Override
    protected Collection<AccAccountRoleAssignmentDto> getAssignments() {
        final UUID accountId = originalFilter.getParameterConverter().toUuid(originalFilter.getData(), AccAccountRoleAssignmentFilter.PARAMETER_ACCOUNT_ID);
        final UUID identityId = originalFilter.getIdentityId();
        if (Objects.nonNull(accountId)) {
            LOG.debug(MessageFormat.format("Start searching duplicates for account [{1}].", accountId));
            return roleAssignmentService.findAllByOwnerId(accountId);

        } else if (Objects.nonNull(identityId)) {
            LOG.debug(MessageFormat.format("Start searching duplicates for identity [{1}].", identityId));
            return roleAssignmentService.findAllByIdentity(identityId);
        }
        throw new AssertException("Account identifier or Identity identifier is required.");
    }
}
