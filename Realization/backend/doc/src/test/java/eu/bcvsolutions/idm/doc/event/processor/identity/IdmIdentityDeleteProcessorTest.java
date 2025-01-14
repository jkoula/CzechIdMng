package eu.bcvsolutions.idm.doc.event.processor.identity;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.TestHelper;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.repository.DocDocumentRepository;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic document tests
 *
 * @author Jirka Koula
 *
 */
public class IdmIdentityDeleteProcessorTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocDocumentRepository documentRepository;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testIdentityDelete() {
		IdmIdentityDto identity = helper.createIdentity();
		DocDocumentDto document = helper.createValidDocument(identity, DocDocumentType.ID_CARD);

		IdmIdentityDto identity2 = helper.createIdentity();
		DocDocumentDto document2 = helper.createValidDocument(identity2, DocDocumentType.ID_CARD);

		Assert.assertTrue(documentRepository.existsById(document.getId()));
		Assert.assertTrue(documentRepository.existsById(document2.getId()));

		identityService.delete(identity);

		Assert.assertFalse(documentRepository.existsById(document.getId()));
		Assert.assertTrue(documentRepository.existsById(document2.getId()));

		identityService.delete(identity2);
	}

}
