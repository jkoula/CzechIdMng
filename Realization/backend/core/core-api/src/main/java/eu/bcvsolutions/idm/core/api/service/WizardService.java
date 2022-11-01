package eu.bcvsolutions.idm.core.api.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.core.Ordered;

import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Interface for wizard with core functionality
 * @author Roman Kucera
 */
public interface WizardService<W extends AbstractWizardDto> extends Ordered, BeanNameAware {

	String STEP_FINISH = "finish";

	/**
	 * Bean name / unique identifier (spring bean name).
	 *
	 * @return
	 */
	String getId();

	/**
	 * Returns module
	 *
	 * @return
	 */
	default String getModule() {
		return EntityUtils.getModule(this.getClass());
	}

	/**
	 * Order of wizards
	 *
	 * @return
	 */
	@Override
	int getOrder();

	/**
	 * If false, then wizard will not be visible to a user.
	 *
	 * @return
	 */
	boolean supports();

	/**
	 * Specific data for a wizard.
	 */
	default Map<String, String> getMetadata() {
		return new HashMap<>();
	}

	/**
	 * Execute some wizard step.
	 */
	default W execute(W wizardDto) {
		return wizardDto;
	}

	/**
	 * Load data for specific wizard/step (for open existing system in the wizard).
	 */
	default W load(W wizardDto) {
		return wizardDto;
	}
}
