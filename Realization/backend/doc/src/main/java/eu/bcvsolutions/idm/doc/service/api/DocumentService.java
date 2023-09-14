package eu.bcvsolutions.idm.doc.service.api;

import eu.bcvsolutions.idm.core.api.service.CodeableService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;

/**
 * Example product service
 * 
 * @author Jirka Koula
 *
 */
public interface DocumentService extends
		ReadWriteDtoService<DocumentDto, DocumentFilter>,
		AuthorizableService<DocumentDto> {
}
