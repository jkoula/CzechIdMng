package eu.bcvsolutions.idm.doc.repository.filter;

import java.util.List;

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
import eu.bcvsolutions.idm.doc.service.api.DocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic document tests
 *
 * @author Jirka Koula
 *
 */
public class DocumentStateFilterTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocumentService documentService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testStateFilter() {
		IdmIdentityDto identity = helper.createIdentity();
		DocumentDto document = helper.createValidDocument(identity, DocumentType.ID_CARD);
		document.setState(DocumentState.INVALID);
		documentService.save(document);
		helper.createValidDocument(identity, DocumentType.PASSPORT);
		//
		DocumentFilter documentFilter = new DocumentFilter();
		documentFilter.setState(document.getState());
		List<DocumentDto> documents = documentService.find(documentFilter, null).getContent();
		//
		Assert.assertEquals(1, documents.size());
		Assert.assertEquals(document.getId(), documents.get(0).getId());

		identityService.delete(identity);
	}

}
