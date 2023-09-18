package eu.bcvsolutions.idm.doc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.doc.domain.DocGroupPermission;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.event.DocDocumentEvent;
import eu.bcvsolutions.idm.doc.repository.DocDocumentRepository;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;

/**
 * Default document service implementation
 * 
 * @author Jirka Koula
 *
 */
@Service("documentService")
public class DefaultDocDocumentService
	extends AbstractFormableService<DocDocumentDto, DocDocument, DocDocumentFilter> implements DocDocumentService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultDocDocumentService.class);

	private final EntityEventManager entityEventManager;

	@Autowired
	public DefaultDocDocumentService(
			DocDocumentRepository repository,
			FormService formService,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager, formService);
		//
		this.entityEventManager = entityEventManager;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(DocGroupPermission.DOCUMENT, getEntityClass());
	}

	@Override
	@Transactional
	public DocDocumentDto save(DocDocumentDto dto, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required.");
		checkAccess(toEntity(dto, null), permission);
		//
		LOG.debug("Saving document [{}]", dto);
		//
		if (isNew(dto)) { // create
			return entityEventManager.process(new DocDocumentEvent(DocDocumentEvent.DocumentEventType.CREATE, dto)).getContent();
		}
		return entityEventManager.process(new DocDocumentEvent(DocDocumentEvent.DocumentEventType.UPDATE, dto)).getContent();
	}

}
