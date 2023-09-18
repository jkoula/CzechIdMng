package eu.bcvsolutions.idm.doc.repository;

import eu.bcvsolutions.idm.core.eav.repository.AbstractFormValueRepository;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.entity.eav.DocDocumentFormValue;

/**
 * Extended attributes for document
 * 
 * @author Jirka Koula
 *
 */
public interface DocDocumentFormValueRepository extends AbstractFormValueRepository<DocDocument, DocDocumentFormValue> {
	
}
