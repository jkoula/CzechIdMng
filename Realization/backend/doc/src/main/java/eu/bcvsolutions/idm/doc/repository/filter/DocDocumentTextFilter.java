package eu.bcvsolutions.idm.doc.repository.filter;

import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.repository.filter.AbstractFilterBuilder;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.entity.DocDocument_;
import eu.bcvsolutions.idm.doc.repository.DocDocumentRepository;

/**
 * Document filter - by text.
 * 
 * @author Jirka Koula
 *
 */
@Component
@Description("Document filter - by text. Search as \"like\" in firstName and lastName - lower, case insensitive.")
public class DocDocumentTextFilter extends AbstractFilterBuilder<DocDocument, DocDocumentFilter> {

	@Autowired
	public DocDocumentTextFilter(DocDocumentRepository repository) {
		super(repository);
	}
	
	@Override
	public String getName() {
		return DocDocumentFilter.PARAMETER_TEXT;
	}
	
	@Override
	public Predicate getPredicate(Root<DocDocument> root, AbstractQuery<?> query, CriteriaBuilder builder, DocDocumentFilter filter) {
		String text = filter.getText();
		if (StringUtils.isEmpty(filter.getText())) {
			return null;
		}
		//
		text = text.toLowerCase();
		return builder.or(
				builder.like(builder.lower(root.get(DocDocument_.firstName)), "%" + text + "%"),
				builder.like(builder.lower(root.get(DocDocument_.lastName)), "%" + text + "%")
				);
	}
}