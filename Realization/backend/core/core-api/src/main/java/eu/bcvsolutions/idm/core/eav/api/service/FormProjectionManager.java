package eu.bcvsolutions.idm.core.eav.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.eav.api.dto.FormProjectionRouteDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;

/**
 * Provides supported form projections.
 * 
 * @author Radek Tomiška
 * @since 10.3.0
 */
public interface FormProjectionManager {
	
	/**
	 * Returns supported form projection routes.
	 * 
	 * @return registered form projection routes
	 */
	List<FormProjectionRouteDto> getSupportedRoutes();
	
	/**
	 * Return form definition for basic fields.
	 * 
	 * @param owner owner
	 * @return for definition
	 * @since 11.0.0
	 */
	IdmFormDefinitionDto getBasicFieldsDefinition(Identifiable owner);
	
	/**
	 * Returns {@link IdmFormDefinitionDto} of configured form definition for given DTO.
	 * 
	 * @param dto fields owner
	 * @param formDefinition form definition to load
	 * @return configured {@link IdmFormDefinitionDto} by owner's projection
	 * @since 12.0.0
	 */
	IdmFormDefinitionDto getConfiguredFormDefinition(Identifiable owner, IdmFormDefinitionDto formDefinition);
	
	/**
	 * Returns overridden {@link IdmFormDefinitionDto} of configured form definition for given DTO.
	 * 
	 * @param dto fields owner
	 * @param formDefinition form definition to load
	 * @return filled overridden {@link IdmFormDefinitionDto} by owner's projection
	 * @since 12.0.0
	 */
	IdmFormDefinitionDto overrideFormDefinition(Identifiable owner, IdmFormDefinitionDto originalDefinition);
	
	/**
	 * Returns overridden {@link IdmFormDefinitionDto} by given configured form definition.
	 * 
	 * @param originalDefinition form definition to override
	 * @param configuredDefinition form definition by projection
	 * @return filled overridden {@link IdmFormDefinitionDto} by given configured form definition.
	 * @since 12.0.0
	 */
	IdmFormDefinitionDto overrideFormDefinition(IdmFormDefinitionDto originalDefinition, IdmFormDefinitionDto configuredDefinition);
	
	/**
	 * Return form instance (with form definition) for basic fields.
	 * 
	 * @param owner owner
	 * @return form instance with definition and values by dto fields
	 * @since 11.0.0
	 */
	IdmFormInstanceDto getBasicFieldsInstance(Identifiable owner);
}
