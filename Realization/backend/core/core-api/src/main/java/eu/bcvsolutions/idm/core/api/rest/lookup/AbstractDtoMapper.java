package eu.bcvsolutions.idm.core.api.rest.lookup;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Convert {@link BaseEntity} to {@link BaseDto}.
 * 
 * @author Radek Tomiška
 * @param <DTO> dto type - registerable by dto type
 * @param <E> entity type
 * @param <C> context parameters
 * @since 11.2.0
 */
public abstract class AbstractDtoMapper<DTO extends BaseDto, E extends BaseEntity> implements DtoMapper<DTO, E> {

	@Autowired 
	private ModelMapper modelMapper;
	@Autowired @Lazy
	private LookupService lookupService;
	private ParameterConverter parameterConverter;	
	
	public AbstractDtoMapper() {
	}
	
	public AbstractDtoMapper(ModelMapper modelMapper) {
		Assert.notNull(modelMapper, "Model mapper is required.");
		//
		this.modelMapper = modelMapper;
	}
	
	protected ModelMapper getModelMapper() {
		return modelMapper;
	}
	
	protected LookupService getLookupService() {
		return lookupService;
	}
	
	protected ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(getLookupService());
		}
		return parameterConverter;
	}
}
