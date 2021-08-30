package eu.bcvsolutions.idm.core.model.repository.filter;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleRepository;

/**
 * Find role by text ~ quick filter.
 * 
 * @author Radek Tomiška
 * @since 11.2.0 
 */
@Component
@Description("Find role by text ~ quick filter.")
public class IdmRoleTextFilter extends AbstractFilterBuilder<IdmRole, IdmRoleFilter> {
	
	@Autowired
	public IdmRoleTextFilter(IdmRoleRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return IdmRoleFilter.PARAMETER_TEXT;
	}
	
	@Override
	public Predicate getPredicate(Root<IdmRole> root, AbstractQuery<?> query, CriteriaBuilder builder, IdmRoleFilter filter) {
		String text = filter.getText();
		if (StringUtils.isEmpty(text)) {
			return null;
		}
		text = text.toLowerCase();
		List<Predicate> textPredicates = new ArrayList<>(4);
		//
		RepositoryUtils.appendUuidIdentifierPredicate(textPredicates, root, builder, text);
		textPredicates.add(builder.like(builder.lower(root.get(IdmRole_.code)), "%" + text + "%"));
		textPredicates.add(builder.like(builder.lower(root.get(IdmRole_.name)), "%" + text + "%"));
		textPredicates.add(builder.like(builder.lower(root.get(IdmRole_.description)), "%" + text + "%"));
		//
		return builder.or(textPredicates.toArray(new Predicate[textPredicates.size()]));
	}	
}