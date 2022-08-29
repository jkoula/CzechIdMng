package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest_;
import eu.bcvsolutions.idm.acc.event.AccAccountRoleEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.entity.AbstractConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.model.entity.IdmConceptRoleRequest_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractConceptRoleRequestService;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service("accountConceptRoleService")
public class DefaultAccAccountConceptRoleRequestService extends AbstractConceptRoleRequestService<
        AccAccountRoleDto,
        AccAccountConceptRoleRequestDto,
        AccAccountConceptRoleRequest,
        AccAccountConceptRoleRequestFilter> implements AccAccountConceptRoleRequestService {


    private final AccAccountRoleService accRoleAccountService;
    private final FormService formService;
    private final IdmRoleService roleService;
    private final AccAccountService accountService;
    private final AccIdentityAccountService identityAccountService;

    @Autowired
    public DefaultAccAccountConceptRoleRequestService(AbstractEntityRepository<AccAccountConceptRoleRequest> repository, WorkflowProcessInstanceService workflowProcessInstanceService, LookupService lookupService, IdmAutomaticRoleRepository automaticRoleRepository, AccAccountRoleService accRoleAccountService, FormService formService, IdmRoleCompositionService roleCompositionService, IdmRoleService roleService, AccAccountService accountService, AccIdentityAccountService identityAccountService) {
        super(repository, workflowProcessInstanceService, roleCompositionService, lookupService, automaticRoleRepository);
        this.accRoleAccountService = accRoleAccountService;
        this.formService = formService;
        this.roleService = roleService;
        this.accountService = accountService;
        this.identityAccountService = identityAccountService;
    }

    @Override
    protected List<Predicate> toPredicates(Root<AccAccountConceptRoleRequest> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccAccountConceptRoleRequestFilter filter) {
        List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
        if (filter.getAccountRole() != null) {
            predicates.add(builder.equal(root.get(AccAccountConceptRoleRequest_.accountRole).get(AbstractEntity_.id),
                    filter.getAccountRole()));
        }
        if (filter.getAccountuuid() != null) {
            predicates.add(builder.equal(root.get(AccAccountConceptRoleRequest_.accAccount).get(AbstractEntity_.id),
                    filter.getAccountuuid()));
        }

        Set<UUID> ids = filter.getAccountRoleUuids();
        if (ids != null && !ids.isEmpty()) {
            predicates.add(root.get(AccAccountConceptRoleRequest_.accountRole).get(AbstractEntity_.id).in(ids));
        }

        if (filter.isAccountRoleIsNull()) {
            predicates.add(builder.isNull(root.get(AccAccountConceptRoleRequest_.accountRole)));
        }
        //
        return predicates;
    }

    @Override
    protected IdmFormValueDto getCurrentFormValue(UUID identityRoleId, IdmFormValueDto value, IdmFormAttributeDto formAttributeDto) {
        return null;
    }

    @Override
    protected AccAccountRoleDto getRoleAssignmentDtoInternal(AccAccountConceptRoleRequestDto concept, IdmRoleDto subRole) {
        return null;
    }

    @Override
    protected String cancelInvalidConceptInternal(List<AccAccountRoleDto> automaticRoles, AccAccountConceptRoleRequestDto concept, IdmRoleRequestDto request) {
        return null;
    }

    @Override
    protected AccAccountRoleDto createAssignmentFromConceptInternal(AccAccountConceptRoleRequestDto concept) {
        return null;
    }

    @Override
    protected IdmFormInstanceDto getFormInstance(AccAccountConceptRoleRequestDto dto, IdmFormDefinitionDto definition) {
        AccAccountRoleDto identityRoleDto = DtoUtils.getEmbedded(dto, AccAccountConceptRoleRequest_.accountRole,
                AccAccountRoleDto.class, null);
        if(identityRoleDto == null) {
            identityRoleDto = accRoleAccountService.get(dto.getAccountRole());
        }
        return formService.getFormInstance(
                new IdmIdentityRoleDto(identityRoleDto.getId()),
                definition);
    }

    @Override
    protected boolean shouldProcessChanges(AccAccountConceptRoleRequestDto dto, ConceptRoleRequestOperation operation) {
        return dto.getAccountRole() != null && ConceptRoleRequestOperation.UPDATE == operation;
    }

    @Override
    protected AccAccountConceptRoleRequestFilter getFilter() {
        return new AccAccountConceptRoleRequestFilter();
    }

    @Override
    protected UUID getIdentityRoleId(AccAccountConceptRoleRequestDto concept) {
        UUID accountRoleId = null;
        AccAccountConceptRoleRequestDto accountRoleDto = DtoUtils.getEmbedded(concept, AccAccountConceptRoleRequest_.accountRole,
                AccAccountConceptRoleRequestDto.class, null);
        if(accountRoleDto == null) {
            accountRoleId = concept.getAccountRole();
        } else {
            accountRoleId = accountRoleDto.getId();
        }
        return accountRoleId;
    }

    @Override
    public IdmRoleDto determineRoleFromConcept(AccAccountConceptRoleRequestDto concept) {
        IdmRoleDto role = null;
        if(concept.getRole() != null) {
            role = DtoUtils.getEmbedded(concept, AbstractConceptRoleRequest_.role, IdmRoleDto.class, null);
            if (role == null) {
                role = roleService.get(concept.getRole());
            }
        } else {
            AccAccountRoleDto accountRole = DtoUtils.getEmbedded(concept, AccAccountConceptRoleRequest_.accountRole, AccAccountRoleDto.class, null);
            if (accountRole == null) {
                accountRole = accRoleAccountService.get(concept.getAccountRole());
            }
            if (accountRole != null) {
                role = DtoUtils.getEmbedded(concept, AbstractRoleAssignment_.role, IdmRoleDto.class);
            }
        }
        return role;
    }

    @Override
    public boolean validOwnership(AccAccountConceptRoleRequestDto concept, UUID applicantId) {
        // concept for account is valid only if applicant is the same as the account owner
        AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
        identityAccountFilter.setAccountId(concept.getAccAccount());
        return identityAccountService.find(identityAccountFilter, null).stream()
                .allMatch(accIdentityAccountDto -> accIdentityAccountDto.getIdentity().equals(applicantId));
    }

    @Override
    public List<AccAccountConceptRoleRequestDto> findAllByRoleRequest(UUID requestId, Pageable pa, IdmBasePermission... permissions) {
        AccAccountConceptRoleRequestFilter filter = new AccAccountConceptRoleRequestFilter();
        filter.setRoleRequestId(requestId);
        return find(filter, pa, permissions).getContent();
    }

    @Override
    public CoreEvent<AccAccountRoleDto> removeRelatedRoleAssignment(AccAccountConceptRoleRequestDto concept, EntityEvent<IdmRoleRequestDto> requestEvent) {
        AccAccountRoleDto accountRole = DtoUtils.getEmbedded(concept, AccAccountConceptRoleRequest_.accountRole.getName(),
                AccAccountRoleDto.class, null);
        if (accountRole == null) {
            accountRole = accRoleAccountService.get(concept.getAccountRole());
        }

        if (accountRole != null) {
            concept.setState(RoleRequestState.EXECUTED);
            concept.setAccountRole(null); // we have to remove relation on
            // deleted accountRole
            String message = MessageFormat.format(
                    "IdentityRole [{0}] (reqested in concept [{1}]) was deleted (from this role request).",
                    accountRole.getId(), concept.getId());
            addToLog(concept, message);
            addToLog(requestEvent.getContent(), message);
            save(concept);

            AccAccountRoleEvent event = new AccAccountRoleEvent(AccAccountRoleEvent.AccountRoleEventType.DELETE, accountRole,
                    Map.of(IdmAccountDto.SKIP_PROPAGATE, Boolean.TRUE));

            accRoleAccountService.publish(event, requestEvent);
            return event;
        }
        return null;
    }

    @Override
    public void updateAssignedRole(List<AbstractConceptRoleRequestDto> allApprovedConcepts, AccAccountConceptRoleRequestDto concept, EntityEvent<IdmRoleRequestDto> requestEvent) {

    }

    @Override
    public void createAssignedRole(List<AbstractConceptRoleRequestDto> allApprovedConcepts, AccAccountConceptRoleRequestDto concept, EntityEvent<IdmRoleRequestDto> requestEvent) {

    }

    @Override
    public Collection<AccAccountConceptRoleRequestDto> getConceptsToRemoveDuplicates(AbstractRoleAssignmentDto tempIdentityRoleSub, List<AbstractRoleAssignmentDto> allByIdentity) {
        return null;
    }

    @Override
    public <E extends AbstractConceptRoleRequestDto> E createConceptToRemoveIdentityRole(E concept, AccAccountRoleDto identityRoleAssignment) {
        return null;
    }

    @Override
    public Class<AccAccountConceptRoleRequestDto> getType() {
        return AccAccountConceptRoleRequestDto.class;
    }
}
