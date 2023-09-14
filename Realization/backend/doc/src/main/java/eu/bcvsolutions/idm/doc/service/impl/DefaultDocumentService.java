package eu.bcvsolutions.idm.doc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.doc.domain.DocGroupPermission;
import eu.bcvsolutions.idm.doc.domain.DocumentState;
import eu.bcvsolutions.idm.doc.dto.DocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocumentFilter;
import eu.bcvsolutions.idm.doc.entity.Document;
import eu.bcvsolutions.idm.doc.event.DocumentEvent;
import eu.bcvsolutions.idm.doc.repository.DocumentRepository;
import eu.bcvsolutions.idm.doc.service.api.DocumentService;

/**
 * Default document service implementation
 * 
 * @author Jirka Koula
 *
 */
@Service("docDocumentService")
public class DefaultDocumentService
	extends AbstractEventableDtoService<DocumentDto, Document, DocumentFilter> implements DocumentService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultDocumentService.class);

	private final EntityEventManager entityEventManager;

	@Autowired
	public DefaultDocumentService(
			DocumentRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		Assert.notNull(entityEventManager, "Manager is required.");
		Assert.notNull(repository, "Repository is required.");
		//
		this.entityEventManager = entityEventManager;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(DocGroupPermission.DOCUMENT, getEntityClass());
	}

	@Override
	@Transactional
	public DocumentDto save(DocumentDto dto, BasePermission... permission) {
		Assert.notNull(dto, "DTO is required.");
		checkAccess(toEntity(dto, null), permission);
		//
		LOG.debug("Saving document [{}]", dto);
		//
		if (isNew(dto)) { // create
			return entityEventManager.process(new DocumentEvent(DocumentEvent.DocumentEventType.CREATE, dto)).getContent();
		}
		return entityEventManager.process(new DocumentEvent(DocumentEvent.DocumentEventType.UPDATE, dto)).getContent();
	}

}
