package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.domain.Codeable;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormInstanceDto;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormProjectionDto;

/**
 * Find {@link IdmFormProjectionDto} by uuid identifier or by {@link Codeable} identifier.
 * 
 * @author Radek Tomiška
 * @param <DTO> dto
 * @since 11.0.0
 */
public interface FormProjectionLookup<DTO extends BaseDto> extends Plugin<Class<?>> {

	/**
	 * Returns {@link IdmFormProjectionDto} for given DTO.
	 * 
	 * @param dto basic fields owner
	 * @return related {@link IdmFormProjectionDto}
	 */
	IdmFormProjectionDto lookupProjection(DTO dto);
	
	/**
	 * Returns {@link IdmFormDefinitionDto} of basic fields for given DTO.
	 * 
	 * @param dto basic fields owner
	 * @return filled {@link IdmFormDefinitionDto}
	 */
	IdmFormDefinitionDto lookupBasicFieldsDefinition(DTO dto);
	
	/**
	 * Returns {@link IdmFormDefinitionDto} of overriden / configured form definition for given DTO.
	 * 
	 * @param dto fields owner
	 * @param formDefinition form definition to load
	 * @return filled {@link IdmFormDefinitionDto}
	 * @since 12.0.0
	 */
	IdmFormDefinitionDto lookupFormDefinition(DTO dto, IdmFormDefinitionDto formDefinition);
	
	/**
	 * Returns {@link IdmFormInstanceDto} of basic fields for given DTO with filled values.
	 * 
	 * @param dto basic fields owner
	 * @return filled {@link IdmFormInstanceDto}
	 */
	IdmFormInstanceDto lookupBasicFieldsInstance(DTO dto);
}
