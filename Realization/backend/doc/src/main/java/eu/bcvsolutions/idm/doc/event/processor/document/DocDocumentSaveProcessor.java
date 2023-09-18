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
@Component(DocDocumentSaveProcessor.PROCESSOR_NAME)
@Description("Save newly created or update exists document. Cannot be disabled.")
public class DocDocumentSaveProcessor extends CoreEventProcessor<DocDocumentDto> {

	public static final String PROCESSOR_NAME = "document-save-processor";

	private final DocDocumentService documentService;

	@Autowired
	public DocDocumentSaveProcessor(DocDocumentService documentService) {
		super(DocumentEventType.CREATE, DocumentEventType.UPDATE);
		this.documentService = documentService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<DocDocumentDto> process(EntityEvent<DocDocumentDto> event) {
		DocDocumentDto dto = event.getContent();

		dto = documentService.saveInternal(dto);
		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
