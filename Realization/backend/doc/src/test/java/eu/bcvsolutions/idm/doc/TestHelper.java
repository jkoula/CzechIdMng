package eu.bcvsolutions.idm.doc;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.doc.domain.DocumentType;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;

/**
 * Reuses core TestHelper and adds example spec. methods
 * 
 * @author Jirka Koula
 *
 */
public interface TestHelper extends eu.bcvsolutions.idm.test.api.TestHelper {

	/**
	 * Creates valid test document of given type
	 *
	 * @param identity
	 * @param type
	 * @return
	 */
	DocumentDto createValidDocument(IdmIdentityDto identity, DocumentType type);

}
