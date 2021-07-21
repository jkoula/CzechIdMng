package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState_;
import eu.bcvsolutions.idm.core.monitoring.api.dto.filter.MonitoringIgnorableFilter;

/**
 * Filter for filtering entities which are (not) ignored from monitoring.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Component
public class MonitoringIgnorableFilterBuilder<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	@Override
	public String getName() {
		return MonitoringIgnorableFilter.PARAMETER_MONITORING_IGNORED;
	}

	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		Boolean monitoringIgnored = getParameterConverter().toBoolean(filter.getData(), MonitoringIgnorableFilter.PARAMETER_MONITORING_IGNORED);
		if (monitoringIgnored == null) {
			return null;
		}
		
		Subquery<IdmEntityState> subquery = query.subquery(IdmEntityState.class);
		Root<IdmEntityState> subRoot = subquery.from(IdmEntityState.class);
		subquery.select(subRoot);
		subquery.where(
                builder.and(
                		builder.equal(subRoot.get(IdmEntityState_.ownerId), root.get(AbstractEntity_.id)), // correlation attr
                		builder.equal(subRoot.get(IdmEntityState_.result).get(OperationResult_.CODE), CoreResultCode.MONITORING_IGNORED.getCode())
                )
        );
		// exists - entity is ignored
		Predicate predicate = builder.exists(subquery);
		//
		if (monitoringIgnored) {
			// exists - entity is ignored
			return predicate;
		}
		// entity is not ignored
		return builder.not(predicate);
	}

	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by monitoring ignored is not supported, use concrete service instead.");
	}
}
