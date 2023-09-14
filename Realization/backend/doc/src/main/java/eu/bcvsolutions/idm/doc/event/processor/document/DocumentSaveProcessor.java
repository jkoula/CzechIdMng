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
@Component(DocumentSaveProcessor.PROCESSOR_NAME)
@Description("Save newly created or update exists document. Cannot be disabled.")
public class DocumentSaveProcessor extends CoreEventProcessor<DocumentDto> {

	public static final String PROCESSOR_NAME = "document-save-processor";

	private final DocumentService documentService;

	@Autowired
	public DocumentSaveProcessor(DocumentService documentService) {
		super(DocumentEventType.CREATE, DocumentEventType.UPDATE);
		this.documentService = documentService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<DocumentDto> process(EntityEvent<DocumentDto> event) {
		DocumentDto dto = event.getContent();

		dto = documentService.saveInternal(dto);
		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
