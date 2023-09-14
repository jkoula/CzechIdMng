package eu.bcvsolutions.idm.doc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;

/**
 * Events for document
 * 
 * @author Jirka Koula
 *
 */
public class DocumentEvent extends CoreEvent<DocumentDto> {

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

	public DocumentEvent(DocumentEventType operation, DocumentDto content) {
		super(operation, content);
	}

	public DocumentEvent(DocumentEventType operation, DocumentDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}