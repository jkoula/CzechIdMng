package eu.bcvsolutions.idm.doc.event.processor.identity;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;

/**
 * Before identity delete - deletes all documents.
 * 
 * @author Jirka Koula
 *
 */
@Component(IdentityDeleteProcessor.PROCESSOR_NAME)
@Description("Ensures referential integrity. Cannot be disabled. Removes documents.")
public class IdentityDeleteProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityDeleteProcessor.class);
	
	public static final String PROCESSOR_NAME = "doc-identity-delete-processor";
	//
	private final DocumentService documentService;

	@Autowired
	public IdentityDeleteProcessor(DocumentService documentService) {
		super(IdentityEventType.DELETE);
		this.documentService = documentService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		Assert.notNull(identity, "Identity is required.");
		UUID identityId = identity.getId();
		Assert.notNull(identityId, "Identity identifier is required.");

		// remove all identity documents
		DocumentFilter documentFilter = new DocumentFilter();
		documentFilter.setIdentityId(identityId);
		List<DocumentDto> documents = documentService.find(documentFilter, null).getContent();
		documents.forEach(document -> {
			LOG.debug("Remove recipient from provisioning break [{}]", document.getId());
			documentService.delete(document);
		});

		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// right now before identity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}