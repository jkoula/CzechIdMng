package eu.bcvsolutions.idm.doc.event.processor.document;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.TestHelper;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic document tests
 *
 * @author Jirka Koula
 *
 */
public class DocDocumentStateConcurrencyProcessorTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocDocumentService documentService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testConcurrency() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();

		// create valid id card
		DocDocumentDto idCard = helper.createValidDocument(identity, DocDocumentType.ID_CARD);
		Assert.assertEquals(DocDocumentState.VALID, idCard.getState());

		// create valid id card for another identity
		DocDocumentDto idCardForeign = helper.createValidDocument(identity2, DocDocumentType.ID_CARD);
		Assert.assertEquals(DocDocumentState.VALID, idCardForeign.getState());

		// first identity id card should be still valid
		idCard = documentService.get(idCard.getId());
		Assert.assertEquals(DocDocumentState.VALID, idCard.getState());

		// create valid passport
		DocDocumentDto passport = helper.createValidDocument(identity, DocDocumentType.PASSPORT);
		Assert.assertEquals(DocDocumentState.VALID, passport.getState());

		// create another valid id card
		DocDocumentDto idCard2 = helper.createValidDocument(identity, DocDocumentType.ID_CARD);
		Assert.assertEquals(DocDocumentState.VALID, idCard2.getState());

		// previous id card should be invalid
		idCard = documentService.get(idCard.getId());
		Assert.assertEquals(DocDocumentState.INVALID, idCard.getState());

		// passport should be still valid
		passport = documentService.get(passport.getId());
		Assert.assertEquals(DocDocumentState.VALID, passport.getState());

		// second identity id card should be still valid
		idCardForeign = documentService.get(idCardForeign.getId());
		Assert.assertEquals(DocDocumentState.VALID, idCardForeign.getState());

		identityService.delete(identity);
		identityService.delete(identity2);
	}

}
