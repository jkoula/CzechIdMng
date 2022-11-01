package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.core.api.service.WizardManager;

/**
 * Abstract manager for accounts wizard
 * @author Roman Kucera
 */
public interface AccountWizardManager extends WizardManager<AccountWizardDto, AccountWizardsService> {
}
