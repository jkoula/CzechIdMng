package eu.bcvsolutions.idm.core.model.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.InstanceIdentifiable;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.InstanceIdentifiableFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.filter.BaseFilterBuilder;

/**
 * Common filter on service instance identifier.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component
public class InstanceIdentifiableFilterBuilder<E extends AbstractEntity> extends BaseFilterBuilder<E, DataFilter> {

	public static final String PROPERTY_NAME = InstanceIdentifiable.PROPERTY_INSTANCE_ID;
	
	@Override
	public String getName() {
		return PROPERTY_NAME;
	}
	
	@Override
	public Predicate getPredicate(Root<E> root, AbstractQuery<?> query, CriteriaBuilder builder, DataFilter filter) {
		if (!(filter instanceof InstanceIdentifiableFilter)) {
			return null;
		}
		String instanceId = ((InstanceIdentifiableFilter) filter).getInstanceId();
		if (StringUtils.isEmpty(instanceId)) {
			return null;
		}
		//
		return builder.equal(root.get(InstanceIdentifiable.PROPERTY_INSTANCE_ID), instanceId);
	}
	
	@Override
	public Page<E> find(DataFilter filter, Pageable pageable) {
		throw new UnsupportedOperationException("Find by server instance identifier only is not supported, use concrete service instead.");
	}
}
