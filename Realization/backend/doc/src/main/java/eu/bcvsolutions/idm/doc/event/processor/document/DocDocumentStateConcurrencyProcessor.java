package eu.bcvsolutions.idm.doc.event.processor.document;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.event.DocDocumentEvent.DocumentEventType;
import eu.bcvsolutions.idm.doc.event.processor.DocDocumentProcessor;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;

/**
 * Sets INVALID state in case first/last name differs from identity attributes.
 *
 * @author Jirka Koula
 */
@Component(DocDocumentStateConcurrencyProcessor.PROCESSOR_NAME)
@Description("Sets INVALID state in case first/last name differs from identity attributes. Cannot be disabled.")
public class DocDocumentStateConcurrencyProcessor
		extends CoreEventProcessor<DocDocumentDto>
		implements DocDocumentProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocDocumentStateConcurrencyProcessor.class);
	public static final String PROCESSOR_NAME = "doc-document-state-concurrency-processor";
	//
	private final IdmIdentityService identityService;

	private final DocDocumentService documentService;

	@Autowired
	public DocDocumentStateConcurrencyProcessor(IdmIdentityService identityService, DocDocumentService documentService) {
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

		// if setting valid document, we have to invalidate all valid documents of the same type
		if (entity.getState().equals(DocDocumentState.VALID)) {
			UUID entityId = entity.getId();

			DocDocumentFilter filter = new DocDocumentFilter();
			filter.setIdentityId(entity.getIdentity());
			filter.setType(entity.getType());
			filter.setState(DocDocumentState.VALID);
			List<DocDocumentDto> documents = documentService.find(filter, null).getContent();
			documents.forEach(document -> {
				if (!document.getId().equals(entityId)) {
					document.setState(DocDocumentState.INVALID);
					documentService.save(document);
				}
			});
		}

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1; // right before save
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
