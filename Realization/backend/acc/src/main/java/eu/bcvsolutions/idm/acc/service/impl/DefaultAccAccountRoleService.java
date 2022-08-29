package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.dto.AccAccountRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccAccountRole;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount;
import eu.bcvsolutions.idm.acc.entity.AccRoleAccount_;
import eu.bcvsolutions.idm.acc.repository.AccAccountRoleRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountRoleService;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
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
public class DefaultAccAccountRoleService extends AbstractRoleAssignmentService<AccAccountRoleDto, AccAccountRole, AccAccountRoleFilter> implements AccAccountRoleService {

    private final AccAccountRoleRepository accAccountRoleRepository;

    @Autowired
    public DefaultAccAccountRoleService(AccAccountRoleRepository repository, EntityEventManager entityEventManager,
                                        FormService formService, IdmRoleService roleService, IdmAutomaticRoleRepository automaticRoleRepository,
                                        LookupService lookupService, FilterManager filterManager) {
        super(repository, entityEventManager, formService, roleService, automaticRoleRepository, lookupService, filterManager);
        this.accAccountRoleRepository = repository;
    }

    @Override
    public AuthorizableType getAuthorizableType() {
        //TODO
        return null;
    }

    @Override
    protected AccAccountRoleFilter getFilter() {
        return new AccAccountRoleFilter();
    }

    @Override
    protected LocalDate getDateForValidTill(AccAccountRoleDto one) {
        return one.getValidTill();
    }

    @Override
    public List<AccAccountRoleDto> findByAccountId(UUID id) {
        return accAccountRoleRepository.findByAccount_Id(id);
    }

    @Override
    protected List<Predicate> toPredicates(Root<AccAccountRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, AccAccountRoleFilter filter) {
        final List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
        //
        Boolean valid = filter.getValid();
        if (valid != null) {
            // Only valid account-role
            if (valid) {
                final LocalDate today = LocalDate.now();
                predicates.add(
                        RepositoryUtils.getValidPredicate(root, builder, today)
                );
            }
            // Only invalid account-role
            if (!valid) {
                final LocalDate today = LocalDate.now();
                predicates.add(
                        builder.not(RepositoryUtils.getValidPredicate(root, builder, today))

                );
            }
        }

        final UUID identityId = filter.getIdentityUuid();
        if (identityId != null) {
            final Subquery<AccIdentityAccount> identityAccountSubquery = query.subquery(AccIdentityAccount.class);
            final Root<AccIdentityAccount> identityAccountRoot = identityAccountSubquery.from(AccIdentityAccount.class);
            identityAccountSubquery.select(identityAccountRoot.get(AccIdentityAccount_.IDENTITY).get(AbstractEntity_.ID));
            identityAccountSubquery.where(
                    builder.equal(
                            identityAccountRoot.get(AccIdentityAccount_.ACCOUNT), root.get(AccRoleAccount_.ACCOUNT)
                    )
            );
            predicates.add(builder.exists(identityAccountSubquery));
        }

        //
        return predicates;
    }

    @Override
    public Class<AccAccountRoleDto> getType() {
        return AccAccountRoleDto.class;
    }

    @Override
    public Collection<? extends AbstractRoleAssignmentDto> findAllByIdentity(UUID id) {
        AccAccountRoleFilter filter = new AccAccountRoleFilter();
        filter.setIdentityId(id);
        return find(filter, null).getContent();
    }
}
