package eu.bcvsolutions.idm.doc.event.processor.document;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.event.DocDocumentEvent.DocumentEventType;
import eu.bcvsolutions.idm.doc.event.processor.DocDocumentProcessor;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;

/**
 * Sets INVALID state in case first/last name differs from identity attributes.
 *
 * @author Jirka Koula
 */
@Component(DocDocumentStateProcessor.PROCESSOR_NAME)
@Description("Sets INVALID state in case first/last name differs from identity attributes. Cannot be disabled.")
public class DocDocumentStateProcessor
		extends CoreEventProcessor<DocDocumentDto>
		implements DocDocumentProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocDocumentStateProcessor.class);
	public static final String PROCESSOR_NAME = "doc-document-state-processor";
	//
	private final IdmIdentityService identityService;

	private final DocDocumentService documentService;

	@Autowired
	public DocDocumentStateProcessor(IdmIdentityService identityService, DocDocumentService documentService) {
		super(DocumentEventType.UPDATE, DocumentEventType.CREATE);
		//
		Assert.notNull(identityService, "Identity service is required.");
		Assert.notNull(documentService, "Document service is required.");
		//
		this.identityService = identityService;
		this.documentService = documentService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<DocDocumentDto> process(EntityEvent<DocDocumentDto> event) {
		DocDocumentDto entity = event.getContent();
		UUID identityId = entity.getIdentity();
		IdmIdentityDto identity = identityService.get(identityId);
		if (
				!entity.getFirstName().equals(identity.getFirstName())
						||
						!entity.getLastName().equals(identity.getLastName())
		) {
			entity.setState(DocDocumentState.INVALID);
		}

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return -100; // before save
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
