package eu.bcvsolutions.idm.core.eav.rest.lookup;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.rest.lookup.AbstractDtoMapper;
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
		extends AbstractDtoMapper<IdmFormValueDto, AbstractFormValue<? extends FormableEntity>> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdmFormValueDtoMapper.class);
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return IdmFormValueDto.class.isAssignableFrom(delimiter);
	}

	@Override
	public IdmFormValueDto map(AbstractFormValue<? extends FormableEntity> entity, IdmFormValueDto dto, DataFilter context) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			dto = getModelMapper().map(entity, IdmFormValueDto.class);
		} else {
			getModelMapper().map(entity, dto);
		}
		//
		dto.setOwnerId(entity.getOwner().getId());
		dto.setOwnerType(entity.getOwner().getClass());
		//
		// try to put owner to embedded (FE usage)
		if (entity.getOwner() != null 
				&& context != null
				&& getParameterConverter().toBoolean(context.getData(), IdmFormValueFilter.PARAMETER_ADD_OWNER_DTO, false)) {
			try {
				dto.getEmbedded().put(
						FormValueService.PROPERTY_OWNER,
						getLookupService().toDto(entity.getOwner(), null, null)
				);
			} catch (Exception ex) {
				// e.g. Load entity from audit with NotFoundAction.IGNORE action can fail.
				LOG.debug("Failed to convert owner [{}] into dto, Owner dto will be not set in embedded.",
						entity.getOwner().getId(), ex);
			}
		}
		//
		return dto;
	}
}
