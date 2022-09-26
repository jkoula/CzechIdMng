package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.core.api.service.WizardService;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormDefinitionDto;

/**
 * Service for accounts wizard
 * @author Roman Kucera
 */
public interface AccountWizardsService extends WizardService<AccountWizardDto> {

	default IdmFormDefinitionDto getFormDefinitionDto() {
		return null;
	}
}
