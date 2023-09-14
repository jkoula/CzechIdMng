package eu.bcvsolutions.idm.doc;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.doc.domain.DocumentState;
import eu.bcvsolutions.idm.doc.domain.DocumentType;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;

/**
 * Document test helper - custom test helper can be defined in modules.
 * 
 * @author Jirka Koula
 */
@Primary
@Component("exampleTestHelper")
public class DefaultDocumentTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {

	@Autowired private DocumentService documentService;

	@Override
	public DocumentDto createValidDocument(IdmIdentityDto identity, DocumentType type) {
		DocumentDto document = new DocumentDto();
		document.setIdentity(identity.getId());
		document.setUuid("" + UUID.randomUUID());
		document.setType(type);
		document.setNumber("test-" + System.currentTimeMillis());
		document.setFirstName(identity.getFirstName());
		document.setLastName(identity.getLastName());
		document.setState(DocumentState.VALID);

		return documentService.save(document);
	}

}
