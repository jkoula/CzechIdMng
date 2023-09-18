package eu.bcvsolutions.idm.doc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;

/**
 * Events for document
 * 
 * @author Jirka Koula
 *
 */
public class DocDocumentEvent extends CoreEvent<DocDocumentDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported identity events
	 *
	 */
	public enum DocumentEventType implements EventType {
		CREATE,
		UPDATE,
		DELETE;
	}

	public DocDocumentEvent(DocumentEventType operation, DocDocumentDto content) {
		super(operation, content);
	}

	public DocDocumentEvent(DocumentEventType operation, DocDocumentDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}