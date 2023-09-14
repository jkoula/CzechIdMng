package eu.bcvsolutions.idm.doc.event.processor;

import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;

/**
 * Document processors should implement this interface.
 * 
 * @author Jirka Koula
 *
 */
public interface DocumentProcessor extends EntityEventProcessor<DocumentDto> {

}
