package eu.bcvsolutions.idm.doc.event.processor.document;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.doc.domain.DocumentState;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.doc.event.DocumentEvent.DocumentEventType;
import eu.bcvsolutions.idm.doc.event.processor.DocumentProcessor;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;

/**
 * Sets INVALID state in case first/last name differs from identity attributes.
 *
 * @author Jirka Koula
 */
@Component(DocumentStateConcurrencyProcessor.PROCESSOR_NAME)
@Description("Sets INVALID state in case first/last name differs from identity attributes. Cannot be disabled.")
public class DocumentStateConcurrencyProcessor
		extends CoreEventProcessor<DocumentDto>
		implements DocumentProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocumentStateConcurrencyProcessor.class);
	public static final String PROCESSOR_NAME = "doc-document-state-concurrency-processor";
	//
	private final IdmIdentityService identityService;

	private final DocumentService documentService;

	@Autowired
	public DocumentStateConcurrencyProcessor(IdmIdentityService identityService, DocumentService documentService) {
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
	public EventResult<DocumentDto> process(EntityEvent<DocumentDto> event) {
		DocumentDto entity = event.getContent();

		// if setting valid document, we have to invalidate all valid documents of the same type
		if (entity.getState().equals(DocumentState.VALID)) {
			UUID entityId = entity.getId();

			DocumentFilter filter = new DocumentFilter();
			filter.setIdentityId(entity.getIdentity());
			filter.setType(entity.getType());
			filter.setState(DocumentState.VALID);
			List<DocumentDto> documents = documentService.find(filter, null).getContent();
			documents.forEach(document -> {
				if (!document.getId().equals(entityId)) {
					document.setState(DocumentState.INVALID);
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
