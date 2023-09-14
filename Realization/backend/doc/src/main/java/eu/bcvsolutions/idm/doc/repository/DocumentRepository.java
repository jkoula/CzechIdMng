package eu.bcvsolutions.idm.doc.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.doc.entity.Document;

/**
 * Document repository
 *
 * @author Jirka Koula
 */
public interface DocumentRepository extends AbstractEntityRepository<Document> {

}

