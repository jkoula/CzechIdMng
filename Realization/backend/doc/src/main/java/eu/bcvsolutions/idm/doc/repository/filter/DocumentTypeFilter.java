package eu.bcvsolutions.idm.doc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.doc.entity.Document;
import eu.bcvsolutions.idm.doc.entity.Document_;
import eu.bcvsolutions.idm.doc.repository.DocumentRepository;

/**
 * Document filter - by type, equals.
 * 
 * @author Jirka Koula
 *
 */
@Component
@Description("Document filter - by type, equal.")
public class DocumentTypeFilter extends AbstractFilterBuilder<Document, DocumentFilter> {

	@Autowired
	public DocumentTypeFilter(DocumentRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return DocumentFilter.PARAMETER_TYPE;
	}
	
	@Override
	public Predicate getPredicate(Root<Document> root, AbstractQuery<?> query, CriteriaBuilder builder, DocumentFilter filter) {
		if (filter.getType() == null) {
			return null;
		}	
		return builder.equal(root.get(Document_.type), filter.getType());
	}
}