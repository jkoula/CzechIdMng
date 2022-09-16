package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.AbstractWizardDto;

/**
 * Abstract manager for wizard
 * @author Roman Kucera
 * @param <W>
 * @param <S>
 */
public interface WizardManager<W extends AbstractWizardDto, S extends WizardService<W>> {

	/**
	 * Returns all registered connector types.
	 *
	 * @return
	 */
	List<S> getSupportedTypes();

	/**
	 * Get connector type by ID.
	 */
	S getWizardType(String id);

	/**
	 * Converts connectorType to DTO version.
	 */
	W convertTypeToDto(S wizard);

	/**
	 * execute some wizard step.
	 */
	W execute(W wizardDto);

	/**
	 * Load data for specific wizard/step (for open existing system in the wizard).
	 */
	W load(W wizardDto);
}
