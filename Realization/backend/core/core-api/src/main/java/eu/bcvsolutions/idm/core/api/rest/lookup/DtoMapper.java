package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.springframework.plugin.core.Plugin;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;

/**
 * Convert {@link BaseEntity} to {@link BaseDto}.
 * 
 * @author Radek Tomiška
 * @param <DTO> dto type - registerable by dto type
 * @param <E> entity type
 * @param <C> context parameters
 * @since 11.2.0
 */
public interface DtoMapper<DTO extends BaseDto, E extends BaseEntity> extends Plugin<Class<?>> {

	/**
	 * Convert {@link BaseEntity} to {@link BaseDto}.
	 * 
	 * @param entity entity to convert
	 * @param dto [optional] prepapred dto instance
	 * @param context [optional] context parameters
	 * @return filled dto
	 */
	DTO map(E entity, DTO dto, DataFilter context);
}
