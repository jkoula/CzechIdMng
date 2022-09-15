package eu.bcvsolutions.idm.acc.wizard;

import org.springframework.beans.factory.BeanNameAware;

import eu.bcvsolutions.idm.acc.service.api.AccountWizardsService;

public abstract class AbstractAccountWizard implements AccountWizardsService, BeanNameAware {

	private String beanName; // spring bean name - used as id

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public String getId() {
		return beanName;
	}
}
