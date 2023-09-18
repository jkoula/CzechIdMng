package eu.bcvsolutions.idm.doc.event.processor.identity;

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
public class IdmIdentitySaveEAVProcessorTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private IdmIdentityService identityService;
	@Autowired private DocDocumentService documentService;

	@Test
	public void testIdentityChangeEAV() {
		IdmIdentityDto identity = helper.createIdentity();
		helper.setIdentityDateOfBirth(identity, "2000-01-01");

		DocDocumentDto document = helper.createValidDocument(identity, DocDocumentType.ID_CARD);
		Assert.assertEquals(DocDocumentState.VALID, document.getState());

		helper.setIdentityDateOfBirth(identity, "2001-02-03");
		document = documentService.get(document.getId());
		Assert.assertEquals(DocDocumentState.INVALID, document.getState());

		identityService.delete(identity);
	}

}
