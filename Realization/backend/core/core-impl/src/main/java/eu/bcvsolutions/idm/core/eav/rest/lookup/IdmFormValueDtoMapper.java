package eu.bcvsolutions.idm.core.eav.rest.lookup;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.rest.lookup.DtoMapper;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormValueDto;
import eu.bcvsolutions.idm.core.eav.api.dto.filter.IdmFormValueFilter;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.api.service.FormValueService;
import eu.bcvsolutions.idm.core.eav.entity.AbstractFormValue;

/**
 * Map form value to dto.
 * 
 * @author Radek Tomiška
 * @since 11.2.0
 */
@Order(0)
@Component
public class IdmFormValueDtoMapper 
		implements DtoMapper<IdmFormValueDto, AbstractFormValue<? extends FormableEntity>, IdmFormValueFilter<? extends FormableEntity>> {

	@Autowired 
	private ModelMapper modelMapper;
	@Autowired @Lazy
	private LookupService lookupService;
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return IdmFormValueDto.class.isAssignableFrom(delimiter);
	}

	@Override
	public IdmFormValueDto map(AbstractFormValue<? extends FormableEntity> entity, IdmFormValueDto dto, IdmFormValueFilter<?> context) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			dto = modelMapper.map(entity, IdmFormValueDto.class);
		}
		modelMapper.map(entity, dto);
		//
		dto.setOwnerId(entity.getOwner().getId());
		dto.setOwnerType(entity.getOwner().getClass());
		// put owner to embedded
		dto.getEmbedded().put(
				FormValueService.PROPERTY_OWNER,
				lookupService.toDto(entity.getOwner(), null, null)
		);
		return dto;
	}

}
