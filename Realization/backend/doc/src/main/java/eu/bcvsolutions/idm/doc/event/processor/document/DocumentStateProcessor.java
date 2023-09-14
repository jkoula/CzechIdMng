package eu.bcvsolutions.idm.doc.event.processor.document;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
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
@Component(DocumentStateProcessor.PROCESSOR_NAME)
@Description("Sets INVALID state in case first/last name differs from identity attributes. Cannot be disabled.")
public class DocumentStateProcessor
		extends CoreEventProcessor<DocumentDto>
		implements DocumentProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DocumentStateProcessor.class);
	public static final String PROCESSOR_NAME = "doc-document-state-processor";
	//
	private final IdmIdentityService identityService;

	private final DocumentService documentService;

	@Autowired
	public DocumentStateProcessor(IdmIdentityService identityService, DocumentService documentService) {
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
		UUID identityId = entity.getIdentity();
		IdmIdentityDto identity = identityService.get(identityId);
		if (
				!entity.getFirstName().equals(identity.getFirstName())
						||
						!entity.getLastName().equals(identity.getLastName())
		) {
			entity.setState(DocumentState.INVALID);
		}

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return -100; // before save
	}

	@Override
	public boolean isDisableable() {
		return false;
	}
}
