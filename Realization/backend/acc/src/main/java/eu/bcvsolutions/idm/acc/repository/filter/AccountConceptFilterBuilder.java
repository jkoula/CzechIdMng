package eu.bcvsolutions.idm.acc.repository.filter;

import eu.bcvsolutions.idm.acc.dto.filter.AccAccountConceptRoleRequestFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountRoleAssignmentFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest;
import eu.bcvsolutions.idm.acc.entity.AccAccountConceptRoleRequest_;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment;
import eu.bcvsolutions.idm.acc.entity.AccAccountRoleAssignment_;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.repository.BaseEntityRepository;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
@Component
public class AccountConceptFilterBuilder extends AbstractFilterBuilder<AccAccountConceptRoleRequest, AccAccountConceptRoleRequestFilter> {

    public static final String ACCOUNT_ID = "accountId";

    public AccountConceptFilterBuilder(BaseEntityRepository<AccAccountConceptRoleRequest, ?> repository) {
        super(repository);
    }

    @Override
    public String getName() {
        return ACCOUNT_ID;
    }

    @Override
    public Predicate getPredicate(Root<AccAccountConceptRoleRequest> root, AbstractQuery<?> query, CriteriaBuilder builder, AccAccountConceptRoleRequestFilter filter) {
        if (filter.getOwnerUuid() != null) {
            return builder.equal(root.get(AccAccountConceptRoleRequest_.account).get(AbstractEntity_.id), filter.getOwnerUuid());
        }
        return null;
    }
}
