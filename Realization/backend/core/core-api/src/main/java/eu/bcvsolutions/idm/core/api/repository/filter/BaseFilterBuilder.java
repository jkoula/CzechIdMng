package eu.bcvsolutions.idm.core.api.repository.filter;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.GenericTypeResolver;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Registrable filter - filters will be applied, when property with defined name will be found in filtering parameters.
 * Filter construct partial criteria where clause => {@link Predicate}, which will be appended to query for defined domain type.
 *  
 * @author Radek Tomiška
 * @see DataFilter
 *
 * @param <E> {@link BaseEntity} type - this filter will be applied to this domain type
 * @param <F> {@link DataFilter} type
 */
public abstract class BaseFilterBuilder<E extends BaseEntity, F extends DataFilter> 
		implements FilterBuilder<E, F>, BeanNameAware {

	private final Class<E> entityClass;
	private final Class<F> filterClass;
	private String beanName; // spring bean name - used as processor id
	private ParameterConverter parameterConverter;
	//
	@Autowired(required = false)
	private ConfigurationService configurationService; // optional internal dependency - checks for processor is enabled
	@Autowired @Lazy
	private LookupService lookupService;
	
	@SuppressWarnings("unchecked")
	public BaseFilterBuilder() {
		Class<?>[] genericTypes = GenericTypeResolver.resolveTypeArguments(getClass(), FilterBuilder.class);
		//
		Assert.notEmpty(genericTypes, "Wrong generic types is given, fix class definition");
		entityClass = (Class<E>) genericTypes[0];
		filterClass = (Class<F>) genericTypes[1];
	}

    @Override
	public Class<E> getEntityClass(){
		return entityClass;
	}
    
    @Override
    public Class<F> getFilterClass() {
		return filterClass;
	}
    
    @Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	@Override
	public String getId() {
		return beanName;
	}

	@Override
	public boolean supports(FilterKey delimiter) {
		return entityClass.isAssignableFrom(delimiter.getEntityClass())
				&& delimiter.getName().equals(this.getName());
	}
	
	@Override
	public int getOrder() {
		return ConfigurationService.DEFAULT_ORDER;
	}
	
	/**
	 * Returns prefix to configuration for this filter builder. 
	 * Under this prefix could be found all builder's properties.
	 * 
	 * Adds entityClass to standard configuration prefix
	 * 
	 * @return
	 */
	@Override
	public String getConfigurationPrefix() {
		return ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX
				+ getModule()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getConfigurableType()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ entityClass.getSimpleName()
				+ ConfigurationService.PROPERTY_SEPARATOR
				+ getName();
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
	
	/**
	 * Return parameter converter helper.
	 * 
	 * @return initialized parameter converter
	 * @since 11.2.0
	 */
	protected ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(lookupService);
		}
		return parameterConverter;
	}
}
