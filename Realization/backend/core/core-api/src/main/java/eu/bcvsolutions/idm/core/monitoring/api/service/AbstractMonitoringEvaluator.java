package eu.bcvsolutions.idm.core.monitoring.api.service;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Abstract monitoring evaluator template.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
public abstract class AbstractMonitoringEvaluator implements 
		MonitoringEvaluator,
		BeanNameAware {

	@Autowired private ConfigurationService configurationService;
	@Autowired private LookupService lookupService;
	//
	private String beanName; // spring bean name - used as id
	private ParameterConverter parameterConverter;	
	
	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}
	
	@Override
	public String getId() {
		return beanName;
	}

	@Override
	public int getOrder() {
		return 0;
	}
	
	@Override
	public ConfigurationService getConfigurationService() {
		return configurationService;
	}
	
	/**
	 * Configured lookup service.
	 * 
	 * @return
	 */
	protected LookupService getLookupService() {
		return lookupService;
	}
	
	/**
	 * Return parameter converter helper
	 * 
	 * @return
	 */
	protected ParameterConverter getParameterConverter() {
		if (parameterConverter == null) {
			parameterConverter = new ParameterConverter(getLookupService());
		}
		return parameterConverter;
	}
	
	
	
}
