package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Convert {@link BaseEntity} to {@link BaseDto}.
 * 
 * @author Radek Tomiška
 *
 * @param <DTO>
 * @param <E>
 * @since 11.2.0
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefaultDtoMapper implements DtoMapper<BaseDto, BaseEntity, BaseFilter> {

	private final Class<? extends BaseDto> dtoClass;
	private final ModelMapper modelMapper;
	
	@Autowired
	public DefaultDtoMapper(ModelMapper modelMapper, Class<? extends BaseDto> dtoClass) {
		Assert.notNull(modelMapper, "Model mapper is required.");
		Assert.notNull(dtoClass, "DTO class is required.");
		//
		this.modelMapper = modelMapper;
		this.dtoClass = dtoClass;
	}
	
	@Override
	public boolean supports(Class<?> delimiter) {
		return dtoClass.isAssignableFrom(delimiter);
	}
	
	/**
	 * Returns {@link BaseDto} type class, which is controlled by this mapper.
	 * 
	 * @param entity
	 * @return dto class
	 */
	protected Class<? extends BaseDto> getDtoClass(BaseEntity entity) {
		return dtoClass;
	}
	
	@Override
	public BaseDto map(BaseEntity entity, BaseDto dto, BaseFilter context) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			return modelMapper.map(entity, getDtoClass(entity));
		}
		modelMapper.map(entity, dto);
		//
		return dto;
	}
}
