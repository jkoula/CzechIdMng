package eu.bcvsolutions.idm.doc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.entity.DocDocument_;
import eu.bcvsolutions.idm.doc.repository.DocDocumentRepository;

/**
 * Document filter - by type, equals.
 * 
 * @author Jirka Koula
 *
 */
@Component
@Description("Document filter - by type, equal.")
public class DocDocumentTypeFilter extends AbstractFilterBuilder<DocDocument, DocDocumentFilter> {

	@Autowired
	public DocDocumentTypeFilter(DocDocumentRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return DocDocumentFilter.PARAMETER_TYPE;
	}
	
	@Override
	public Predicate getPredicate(Root<DocDocument> root, AbstractQuery<?> query, CriteriaBuilder builder, DocDocumentFilter filter) {
		if (filter.getType() == null) {
			return null;
		}	
		return builder.equal(root.get(DocDocument_.type), filter.getType());
	}
}