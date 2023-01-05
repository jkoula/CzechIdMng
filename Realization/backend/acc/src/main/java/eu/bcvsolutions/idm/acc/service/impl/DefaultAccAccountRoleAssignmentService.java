package eu.bcvsolutions.idm.acc.service.impl;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.AccAccountConceptRoleRequestDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment_;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.event.AccAccountRoleAssignmentEvent;
import eu.bcvsolutions.idm.acc.repository.AccAccountRoleRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountConceptRoleRequestService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.impl.adapter.DefaultAccountConceptRoleAdapter;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.AbstractConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractRoleAssignmentService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service("accountRoleService")
public class DefaultAccAccountRoleAssignmentService extends AbstractRoleAssignmentService<AccAccountRoleAssignmentDto, AccAccountRoleAssignment, AccAccountRoleAssignmentFilter> implements AccAccountRoleAssignmentService {

    private final AccAccountRoleRepository accAccountRoleRepository;

    private final AccIdentityAccountService identityAccountService;

    @Autowired
    public DefaultAccAccountRoleAssignmentService(AccAccountRoleRepository repository, EntityEventManager entityEventManager, FormService formService, IdmRoleService roleService,
            IdmAutomaticRoleRepository automaticRoleRepository, LookupService lookupService, FilterManager filterManager, AccIdentityAccountService identityAccountService) {
        super(repository, entityEventManager, formService, roleService, automaticRoleRepository, lookupService, filterManager);
        this.accAccountRoleRepository = repository;
        this.identityAccountService = identityAccountService;
    }

    @Override
    public AuthorizableType getAuthorizableType() {
        return new AuthorizableType(AccGroupPermission.ACCOUNTROLEASSIGNMENT, getEntityClass());
    }

    @Override
    public AccAccountRoleAssignmentFilter getFilter() {
        return new AccAccountRoleAssignmentFilter();
    }

    @Override
    public List<AccAccountRoleAssignmentDto> findAllByOwnerId(UUID ownerUuid) {
        return findByAccountId(ownerUuid);
    }

    @Override
    public IdmIdentityDto getRelatedIdentity(AccAccountRoleAssignmentDto roleAssignment) {
        AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
        filter.setAccountId(roleAssignment.getAccount());
        return identityAccountService.find(filter, null).stream()
                .map(accIdentityAccountDto -> DtoUtils.getEmbedded(accIdentityAccountDto, AccIdentityAccount_.identity,
                IdmIdentityDto.class)).findFirst().orElse(null);
    }

    @Override
    public Class<? extends AbstractConceptRoleRequestDto> getRelatedConceptType() {
        return AccAccountConceptRoleRequestDto.class;
    }

    @Override
    public List<AccAccountRoleAssignmentDto> findByAccountId(UUID id) {
        return toDtos(accAccountRoleRepository.findByAccount_Id(id), false);
    }

    @Override
    protected List<Predicate> toPredicates(Root<AccAccountRoleAssignment> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccAccountRoleAssignmentFilter filter) {
        final List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
        String text = filter.getText();
        if (StringUtils.isNotEmpty(text)) {
            text = text.toLowerCase();
            predicates.add(
                    builder.like(
                            builder.lower(root.get(AccAccountRoleAssignment_.account).get(AccAccount_.uid)),
                            "%" + text + "%")
            );
        }

        if (filter.getAccountId() != null) {
            predicates.add(builder.equal(root.get(AccAccountRoleAssignment_.account).get(AbstractEntity_.id), filter.getAccountId()));
        }

        Boolean valid = filter.getValid();
        if (valid != null) {
            // Only valid account-role
            if (valid) {
                final LocalDate today = LocalDate.now();
                predicates.add(RepositoryUtils.getValidPredicate(root, builder, today));
            }
            // Only invalid account-role
            if (!valid) {
                final LocalDate today = LocalDate.now();
                predicates.add(builder.not(RepositoryUtils.getValidPredicate(root, builder, today))

                );
            }
        }

        final UUID identityId = filter.getIdentityId();
        if (identityId != null) {
            final Subquery<AccIdentityAccount> identityAccountSubquery = query.subquery(AccIdentityAccount.class);
            final Root<AccIdentityAccount> identityAccountRoot = identityAccountSubquery.from(AccIdentityAccount.class);
            identityAccountSubquery.select(identityAccountRoot);
            identityAccountSubquery.where(
                    builder.and(
                            builder.equal(identityAccountRoot.get(AccIdentityAccount_.account), root.get(AccAccountRoleAssignment_.account)),
                            builder.equal(identityAccountRoot.get(AccIdentityAccount_.identity).get(AbstractEntity_.id), identityId)
                    )
            );
            predicates.add(builder.exists(identityAccountSubquery));
        }
        
        final UUID accountId = filter.getAccountId();
        if (accountId != null) {
        	predicates.add(builder.equal(root.get(AccAccountRoleAssignment_.account).get(AbstractEntity_.id), accountId));
        }

        UUID directRoleId = filter.getDirectRoleId();
        if (directRoleId != null) {
            predicates.add(builder.equal(root.get(AccAccountRoleAssignment_.directRole).get(AbstractEntity_.id), directRoleId));
        }
        // is direct role
        Boolean directRole = filter.getDirectRole();
        if (directRole != null) {
            if (directRole) {
                predicates.add(builder.isNull(root.get(AccAccountRoleAssignment_.directRole)));
            } else {
                predicates.add(builder.isNotNull(root.get("directRole")));
            }
        }

        //
        return predicates;
    }

    @Override
    public Class<AccAccountRoleAssignmentDto> getType() {
        return AccAccountRoleAssignmentDto.class;
    }

    @Override
    public Collection<AccAccountRoleAssignmentDto> findAllByIdentity(UUID id) {
        AccAccountRoleAssignmentFilter filter = new AccAccountRoleAssignmentFilter();
        filter.setIdentityId(id);
        return find(filter, null).getContent();
    }

    @Override
    public AbstractRoleAssignmentEvent<AccAccountRoleAssignmentDto> getEventForAssignment(AccAccountRoleAssignmentDto assignment, AbstractRoleAssignmentEvent.RoleAssignmentEventType eventType,
            String... flags) {
        @SuppressWarnings("deprecation") AccAccountRoleAssignmentEvent event = new AccAccountRoleAssignmentEvent(eventType, assignment, setupFlags(flags));
        event.setPriority(PriorityType.IMMEDIATE);
        return event;
    }


    @Override
    public <F2 extends BaseFilter> DtoAdapter<AccAccountRoleAssignmentDto, IdmRequestIdentityRoleDto> getAdapter(F2 originalFilter) {
        IdmRequestIdentityRoleFilter translatedFilter = modelMapper.map(originalFilter, IdmRequestIdentityRoleFilter.class);
        return new DefaultAccountConceptRoleAdapter(translatedFilter, null, applicationContext.getBean(AccAccountConceptRoleRequestService.class), this);
    }

    @Override
    public Class<?> getOwnerType() {
        return AccAccountDto.class;
    }
}
