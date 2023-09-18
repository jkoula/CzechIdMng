package eu.bcvsolutions.idm.doc;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;

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
	DocDocumentDto createValidDocument(IdmIdentityDto identity, DocDocumentType type);

	void setIdentityDateOfBirth(IdmIdentityDto identity, String value);
}
