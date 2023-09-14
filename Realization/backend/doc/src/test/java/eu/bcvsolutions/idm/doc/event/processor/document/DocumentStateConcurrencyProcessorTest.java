package eu.bcvsolutions.idm.doc.event.processor.document;

import org.junit.Assert;
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
public class DocumentStateConcurrencyProcessorTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocumentService documentService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testConcurrency() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		// create valid id card
		DocumentDto idCard = helper.createValidDocument(identity, DocumentType.ID_CARD);
		Assert.assertEquals(DocumentState.VALID, idCard.getState());

		// create valid id card for another identity
		DocumentDto idCardForeign = helper.createValidDocument(identity2, DocumentType.ID_CARD);
		Assert.assertEquals(DocumentState.VALID, idCardForeign.getState());

		// first identity id card should be still valid
		idCard = documentService.get(idCard.getId());
		Assert.assertEquals(DocumentState.VALID, idCard.getState());

		// create valid passport
		DocumentDto passport = helper.createValidDocument(identity, DocumentType.PASSPORT);
		Assert.assertEquals(DocumentState.VALID, passport.getState());

		// create another valid id card
		DocumentDto idCard2 = helper.createValidDocument(identity, DocumentType.ID_CARD);
		Assert.assertEquals(DocumentState.VALID, idCard2.getState());

		// previous id card should be invalid
		idCard = documentService.get(idCard.getId());
		Assert.assertEquals(DocumentState.INVALID, idCard.getState());

		// passport should be still valid
		passport = documentService.get(passport.getId());
		Assert.assertEquals(DocumentState.VALID, passport.getState());

		// second identity id card should be still valid
		idCardForeign = documentService.get(idCardForeign.getId());
		Assert.assertEquals(DocumentState.VALID, idCardForeign.getState());

		identityService.delete(identity);
		identityService.delete(identity2);
	}

}
