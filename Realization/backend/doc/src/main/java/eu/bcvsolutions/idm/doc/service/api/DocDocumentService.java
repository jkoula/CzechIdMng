package eu.bcvsolutions.idm.doc.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;

/**
 * Example product service
 * 
 * @author Jirka Koula
 *
 */
public interface DocDocumentService extends
		ReadWriteDtoService<DocDocumentDto, DocDocumentFilter>,
		AuthorizableService<DocDocumentDto> {
}
