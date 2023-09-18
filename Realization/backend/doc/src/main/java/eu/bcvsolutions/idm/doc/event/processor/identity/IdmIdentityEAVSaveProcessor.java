package eu.bcvsolutions.idm.doc.event.processor.identity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.dto.filter.DocDocumentFilter;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;

/**
 * Processor recalculates document state after identity eav's save
 * 
 * @author Jirka Koula
 *
 */
@Component
@Description("Recalculates document state after identity eav's save.")
public class IdmIdentityEAVSaveProcessor extends CoreEventProcessor<IdmIdentityDto> implements IdentityProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmIdentityDeleteProcessor.class);

	public static final String PROCESSOR_NAME = "identity-eav-save-document-processor";

	private final DocDocumentService documentService;
	private final IdmFormDefinitionService formDefinitionService;
	private final FormService formService;

	@Autowired
	public IdmIdentityEAVSaveProcessor(
			DocDocumentService documentService,
			IdmFormDefinitionService formDefinitionService,
			FormService formService
	) {
		super(IdentityEvent.IdentityEventType.EAV_SAVE);
		this.documentService = documentService;
		this.formDefinitionService = formDefinitionService;
		this.formService = formService;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		Assert.notNull(identity, "Identity is required.");
		UUID identityId = identity.getId();
		Assert.notNull(identityId, "Identity identifier is required.");

		// let's find identity date of birth attribute - hardcoded form definition TODO take any form
		ZonedDateTime dateOfBirthIdentity;
		IdmFormDefinitionDto formDefinitionIdentity =
				formDefinitionService.findOneByTypeAndCode(IdmIdentity.class.getCanonicalName(), "ide");
		if (formDefinitionIdentity != null) {
			IdmFormAttributeDto attributeDefinitionIdentity = formDefinitionIdentity.getMappedAttributeByCode("dateOfBirth");
			if (attributeDefinitionIdentity != null) {
				List<IdmFormValueDto> dateOfBirthDtos = formService.getValues(identity, attributeDefinitionIdentity);
				if (dateOfBirthDtos.size() == 1) {
					dateOfBirthIdentity = dateOfBirthDtos.get(0).getDateValue();
				} else {
					dateOfBirthIdentity = null;
				}
			} else {
				dateOfBirthIdentity = null;
			}
		} else {
			dateOfBirthIdentity = null;
		}

		if (dateOfBirthIdentity != null) {
			// we have identity date of birth, so all document date of birth have to be the same
			IdmFormDefinitionDto formDefinition =
					formDefinitionService.findOneByTypeAndCode(DocDocument.class.getCanonicalName(), "doc");
			if (formDefinition != null) {
				IdmFormAttributeDto attributeDefinition = formDefinition.getMappedAttributeByCode("dateOfBirth");
				if (attributeDefinition != null) {
					DocDocumentFilter documentFilter = new DocDocumentFilter();
					documentFilter.setIdentityId(identityId);
					List<DocDocumentDto> documents = documentService.find(documentFilter, null).getContent();
					documents.forEach(document -> {
						List<IdmFormValueDto> dateOfBirthDtos = formService.getValues(document, attributeDefinition);
						if (dateOfBirthDtos.size() == 1) {
							ZonedDateTime dateOfBirthDocument = dateOfBirthDtos.get(0).getDateValue();
							if (!dateOfBirthDocument.isEqual(dateOfBirthIdentity)) {
								// document date of birth is different from identity one, mark document as invalid
								document.setState(DocDocumentState.INVALID);
								documentService.save(document);
								LOG.debug("Mark document [{}] as INVALID.", document.getId());
							}
						}
					});
				}
			}
		}

		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		// after save
		return super.getOrder() + 500;
	}
}
