package eu.bcvsolutions.idm.doc.repository.filter;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.TestHelper;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;
import eu.bcvsolutions.idm.test.api.AbstractIntegrationTest;

/**
 * Basic document tests
 *
 * @author Jirka Koula
 *
 */
public class DocDocumentIdentityFilterTest extends AbstractIntegrationTest {

	@Autowired private TestHelper helper;
	@Autowired private DocDocumentService documentService;
	@Autowired private IdmIdentityService identityService;

	@Test
	public void testIdentityFilter() {
		IdmIdentityDto identity = helper.createIdentity();
		IdmIdentityDto identity2 = helper.createIdentity();
		DocDocumentDto document = helper.createValidDocument(identity, DocDocumentType.ID_CARD);
		helper.createValidDocument(identity2, DocDocumentType.ID_CARD);
		//
		DocDocumentFilter documentFilter = new DocDocumentFilter();
		documentFilter.setIdentityId(document.getIdentity());
		List<DocDocumentDto> documents = documentService.find(documentFilter, null).getContent();
		//
		Assert.assertEquals(1, documents.size());
		Assert.assertEquals(document.getId(), documents.get(0).getId());

		identityService.delete(identity);
		identityService.delete(identity2);
	}

}
