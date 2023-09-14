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
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.doc.repository.DocumentRepository;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic document tests
 *
 * @author Jirka Koula
 *
 */
public class DocumentSaveProcessorTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocumentService documentService;
	@Autowired private DocumentRepository documentRepository;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testCreateUpdateDocument() {
		IdmIdentityDto identity = helper.createIdentity();
		DocumentDto document = helper.createValidDocument(identity, DocumentType.ID_CARD);
		Assert.assertNotNull(document.getId());
		Assert.assertTrue(documentRepository.existsById(document.getId()));

		Assert.assertEquals(DocumentState.VALID, document.getState());
		document.setState(DocumentState.INVALID);
		document = documentService.save(document);
		Assert.assertEquals(DocumentState.INVALID, document.getState());

		identityService.delete(identity);
	}
}
