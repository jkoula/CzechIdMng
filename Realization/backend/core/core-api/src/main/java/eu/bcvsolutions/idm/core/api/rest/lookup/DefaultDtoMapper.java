package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
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
public class DefaultDtoMapper extends AbstractDtoMapper<BaseDto, BaseEntity> {

	private final Class<? extends BaseDto> dtoClass;
	
	@Autowired
	public DefaultDtoMapper(ModelMapper modelMapper, Class<? extends BaseDto> dtoClass) {
		super(modelMapper);
		//
		Assert.notNull(modelMapper, "Model mapper is required.");
		Assert.notNull(dtoClass, "DTO class is required.");
		//
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
	public BaseDto map(BaseEntity entity, BaseDto dto, DataFilter context) {
		if (entity == null) {
			return null;
		}
		if (dto == null) {
			return getModelMapper().map(entity, getDtoClass(entity));
		}
		getModelMapper().map(entity, dto);
		//
		return dto;
	}
}
