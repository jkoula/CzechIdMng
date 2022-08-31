package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleAssignmentDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment_;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.event.AccAccountRoleAssignmentEvent;
import eu.bcvsolutions.idm.acc.repository.AccAccountRoleRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleAssignmentService;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.service.impl.AbstractRoleAssignmentService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Service("accountRoleService")
public class DefaultAccAccountRoleAssignmentService extends AbstractRoleAssignmentService<AccAccountRoleAssignmentDto, AccAccountRoleAssignment, AccAccountRoleAssignmentFilter> implements AccAccountRoleAssignmentService {

    private final AccAccountRoleRepository accAccountRoleRepository;

    @Autowired
    public DefaultAccAccountRoleAssignmentService(AccAccountRoleRepository repository, EntityEventManager entityEventManager, FormService formService, IdmRoleService roleService,
            IdmAutomaticRoleRepository automaticRoleRepository, LookupService lookupService, FilterManager filterManager) {
        super(repository, entityEventManager, formService, roleService, automaticRoleRepository, lookupService, filterManager);
        this.accAccountRoleRepository = repository;
    }

    @Override
    public AuthorizableType getAuthorizableType() {
        //TODO
        return null;
    }

    @Override
    protected AccAccountRoleAssignmentFilter getFilter() {
        return new AccAccountRoleAssignmentFilter();
    }

    @Override
    protected LocalDate getDateForValidTill(AccAccountRoleAssignmentDto one) {
        return one.getValidTill();
    }

    @Override
    public List<AccAccountRoleAssignmentDto> findByAccountId(UUID id) {
        return toDtos(accAccountRoleRepository.findByAccount_Id(id), false);
    }

    @Override
    protected List<Predicate> toPredicates(Root<AccAccountRoleAssignment> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccAccountRoleAssignmentFilter filter) {
        final List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
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
        //
        return predicates;
    }

    @Override
    public Class<AccAccountRoleAssignmentDto> getType() {
        return AccAccountRoleAssignmentDto.class;
    }

    @Override
    public Collection<? extends AbstractRoleAssignmentDto> findAllByIdentity(UUID id) {
        AccAccountRoleAssignmentFilter filter = new AccAccountRoleAssignmentFilter();
        filter.setIdentityId(id);
        final List<AccAccountRoleAssignmentDto> content = find(filter, null).getContent();
        final List<AccAccountRoleAssignmentDto> accAccountRoleAssignmentDtos = find(new AccAccountRoleAssignmentFilter(), null).getContent();
        return content;
    }

    @Override
    public AbstractRoleAssignmentEvent<AccAccountRoleAssignmentDto> getEventForAssignment(AccAccountRoleAssignmentDto assignment, AbstractRoleAssignmentEvent.RoleAssignmentEventType eventType,
            String... flags) {
        @SuppressWarnings("deprecation") AccAccountRoleAssignmentEvent event = new AccAccountRoleAssignmentEvent(eventType, assignment, setupFlags(flags));
        event.setPriority(PriorityType.IMMEDIATE);
        return event;
    }
}
