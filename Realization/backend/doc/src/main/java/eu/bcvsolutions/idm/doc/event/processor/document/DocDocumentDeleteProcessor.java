package eu.bcvsolutions.idm.doc.event.processor.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.event.DocDocumentEvent.DocumentEventType;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;

/**
 * Save {@link DocDocumentDto}
 *
 * @author Jirka Koula
 *
 */
@Component(DocDocumentDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes document. Cannot be disabled.")
public class DocDocumentDeleteProcessor extends CoreEventProcessor<DocDocumentDto> {

	public static final String PROCESSOR_NAME = "document-delete-processor";

	private final DocDocumentService documentService;

	@Autowired
	public DocDocumentDeleteProcessor(DocDocumentService documentService) {
		super(DocumentEventType.DELETE);
		this.documentService = documentService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<DocDocumentDto> process(EntityEvent<DocDocumentDto> event) {
		documentService.deleteInternal(event.getContent());
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
