package eu.bcvsolutions.idm.doc.event.processor.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.event.DocumentEvent.DocumentEventType;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;

/**
 * Save {@link DocumentDto}
 *
 * @author Jirka Koula
 *
 */
@Component(DocumentDeleteProcessor.PROCESSOR_NAME)
@Description("Deletes document. Cannot be disabled.")
public class DocumentDeleteProcessor extends CoreEventProcessor<DocumentDto> {

	public static final String PROCESSOR_NAME = "document-delete-processor";

	private final DocumentService documentService;

	@Autowired
	public DocumentDeleteProcessor(DocumentService documentService) {
		super(DocumentEventType.DELETE);
		this.documentService = documentService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<DocumentDto> process(EntityEvent<DocumentDto> event) {
		documentService.deleteInternal(event.getContent());
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
