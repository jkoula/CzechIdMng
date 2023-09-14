package eu.bcvsolutions.idm.doc.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.TestHelper;
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
public class DocumentIdentityFilterTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocumentService documentService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testIdentityFilter() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();
		DocumentDto document = helper.createValidDocument(identity, DocumentType.ID_CARD);
		helper.createValidDocument(identity2, DocumentType.ID_CARD);
		//
		DocumentFilter documentFilter = new DocumentFilter();
		documentFilter.setIdentityId(document.getIdentity());
		List<DocumentDto> documents = documentService.find(documentFilter, null).getContent();
		//
		Assert.assertEquals(1, documents.size());
		Assert.assertEquals(document.getId(), documents.get(0).getId());

		identityService.delete(identity);
		identityService.delete(identity2);
	}

}
