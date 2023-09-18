package eu.bcvsolutions.idm.doc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.eav.api.service.IdmFormDefinitionService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.doc.domain.DocDocumentState;
import eu.bcvsolutions.idm.doc.domain.DocDocumentType;
import eu.bcvsolutions.idm.doc.dto.DocDocumentDto;
import eu.bcvsolutions.idm.doc.entity.DocDocument;
import eu.bcvsolutions.idm.doc.service.api.DocDocumentService;

/**
 * Document test helper - custom test helper can be defined in modules.
 * 
 * @author Jirka Koula
 */
@Primary
@Component("exampleTestHelper")
public class DefaultDocumentTestHelper extends eu.bcvsolutions.idm.test.api.DefaultTestHelper implements TestHelper {

	@Autowired private DocDocumentService documentService;
	@Autowired private IdmFormDefinitionService formDefinitionService;
	@Autowired private IdmFormAttributeService formAttributeService;
	@Autowired private FormService formService;

	private static final String IDENTITY_FORM_CODE = "ide";
	private static final String DOCUMENT_FORM_CODE = "doc";
	private static final String DATE_OF_BIRTH = "dateOfBirth";

	@Override
	public DocDocumentDto createValidDocument(IdmIdentityDto identity, DocDocumentType type) {
		DocDocumentDto documentDto = new DocDocumentDto();
		documentDto.setIdentity(identity.getId());
		documentDto.setUuid("" + UUID.randomUUID());
		documentDto.setType(type);
		documentDto.setNumber("test-" + System.currentTimeMillis());
		documentDto.setFirstName(identity.getFirstName());
		documentDto.setLastName(identity.getLastName());
		documentDto.setState(DocDocumentState.VALID);
		documentDto = documentService.save(documentDto);

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate dateOfBirthValue = LocalDate.parse("2000-01-01", dateFormatter);

		IdmFormValueDto dateOfBirth = new IdmFormValueDto(getDocumentAttributeDefinition());
		dateOfBirth.setValue(dateOfBirthValue);

		formService.saveValues(documentDto, getDocumentFormDefinition(), Lists.newArrayList(dateOfBirth));

		return documentDto;
	}

	private IdmFormDefinitionDto getDocumentFormDefinition() {
		IdmFormDefinitionDto formDefinition =
				formDefinitionService.findOneByTypeAndCode(DocDocument.class.getCanonicalName(), DOCUMENT_FORM_CODE);

		if (formDefinition == null) {
			formDefinition = new IdmFormDefinitionDto();
			formDefinition.setType(DocDocument.class.getCanonicalName());
			formDefinition.setCode(DOCUMENT_FORM_CODE);
			formDefinition = formDefinitionService.save(formDefinition);
		}

		return formDefinition;
	}

	private IdmFormAttributeDto getDocumentAttributeDefinition() {
		IdmFormDefinitionDto formDefinition = getDocumentFormDefinition();
		IdmFormAttributeDto attributeDefinition = formDefinition.getMappedAttributeByCode(DATE_OF_BIRTH);

		if (attributeDefinition == null) {
			attributeDefinition = new IdmFormAttributeDto();
			attributeDefinition.setFormDefinition(formDefinition.getId());
			attributeDefinition.setCode(DATE_OF_BIRTH);
			attributeDefinition.setName(attributeDefinition.getCode());
			attributeDefinition.setPersistentType(PersistentType.DATE);
			attributeDefinition = formAttributeService.save(attributeDefinition);
		}

		return attributeDefinition;
	}

	private IdmFormDefinitionDto getIdentityFormDefinition() {
		IdmFormDefinitionDto formDefinition =
				formDefinitionService.findOneByTypeAndCode(IdmIdentity.class.getCanonicalName(), IDENTITY_FORM_CODE);

		if (formDefinition == null) {
			formDefinition = new IdmFormDefinitionDto();
			formDefinition.setType(IdmIdentity.class.getCanonicalName());
			formDefinition.setCode(IDENTITY_FORM_CODE);
			formDefinition = formDefinitionService.save(formDefinition);
		}

		return formDefinition;
	}

	private IdmFormAttributeDto getIdentityAttributeDefinition() {
		IdmFormDefinitionDto formDefinition = getIdentityFormDefinition();
		IdmFormAttributeDto attributeDefinition = formDefinition.getMappedAttributeByCode(DATE_OF_BIRTH);

		if (attributeDefinition == null) {
			attributeDefinition = new IdmFormAttributeDto();
			attributeDefinition.setFormDefinition(formDefinition.getId());
			attributeDefinition.setCode(DATE_OF_BIRTH);
			attributeDefinition.setName(attributeDefinition.getCode());
			attributeDefinition.setPersistentType(PersistentType.DATE);
			attributeDefinition = formAttributeService.save(attributeDefinition);
		}

		return attributeDefinition;
	}

	public void setIdentityDateOfBirth(IdmIdentityDto identity, String value) {
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		LocalDate dateOfBirthValue = LocalDate.parse(value, dateFormatter);

		IdmFormValueDto dateOfBirth = new IdmFormValueDto(getIdentityAttributeDefinition());
		dateOfBirth.setValue(dateOfBirthValue);

		formService.saveValues(identity, getIdentityFormDefinition(), Lists.newArrayList(dateOfBirth));
	}
}
