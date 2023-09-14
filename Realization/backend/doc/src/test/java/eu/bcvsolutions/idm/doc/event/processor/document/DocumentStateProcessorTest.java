package eu.bcvsolutions.idm.doc.event.processor.document;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.TestHelper;
import eu.bcvsolutions.idm.doc.domain.DocumentState;
import eu.bcvsolutions.idm.doc.domain.DocumentType;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic document tests
 *
 * @author Jirka Koula
 *
 */
public class DocumentStateProcessorTest extends AbstractIntegrationTest {

	private static final String DOCUMENT_WRONG_FIRST_NAME = "Cheetah";
	private static final String DOCUMENT_WRONG_LAST_NAME = "Rider";

	@Autowired private TestHelper helper;
	@Autowired private DocumentService documentService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testNameChanges() {
		// create valid entity
		IdmIdentityDto identity = helper.createIdentity();
		DocumentDto document = helper.createValidDocument(identity, DocumentType.ID_CARD);
		assertEquals(DocumentState.VALID, document.getState());

		// change first name and check that state changes to INVALID
		document.setFirstName(DOCUMENT_WRONG_FIRST_NAME);
		documentService.save(document);
		assertEquals(DOCUMENT_WRONG_FIRST_NAME, document.getFirstName());
		assertEquals(DocumentState.INVALID, document.getState());

		// make document valid again
		makeValid(document, identity);

		// change last name and check that state changes to INVALID
		document.setLastName(DOCUMENT_WRONG_LAST_NAME);
		documentService.save(document);
		assertEquals(DOCUMENT_WRONG_LAST_NAME, document.getLastName());
		assertEquals(DocumentState.INVALID, document.getState());

		identityService.delete(identity);
	}

	private void makeValid(DocumentDto document, IdmIdentityDto identity) {
		document.setFirstName(identity.getFirstName());
		document.setLastName(identity.getLastName());
		document.setState(DocumentState.VALID);
		documentService.save(document);
		assertEquals(DocumentState.VALID, document.getState());
	}

}
