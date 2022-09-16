package eu.bcvsolutions.idm.core.api.rest;

import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;

import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;

public interface WizardController<W extends AbstractWizardDto> {

	Resources<W> getSupportedTypes();

	ResponseEntity<W> executeWizardType(W wizardDto);

	ResponseEntity<W> loadWizardType(W wizardDto);
}
