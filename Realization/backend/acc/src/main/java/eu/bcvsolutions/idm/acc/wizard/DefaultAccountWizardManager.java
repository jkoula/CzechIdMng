package eu.bcvsolutions.idm.acc.wizard;

import java.util.List;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.acc.service.api.AccountWizardManager;
import eu.bcvsolutions.idm.acc.service.api.AccountWizardsService;

/**
 * Implementation of wizard manager for accounts
 * @author Roman Kucera
 */

@Service("accountWizardManager")
public class DefaultAccountWizardManager implements AccountWizardManager {

	@Override
	public List<AccountWizardsService> getSupportedTypes() {
		return null;
	}

	@Override
	public AccountWizardsService getWizardType(String id) {
		return null;
	}

	@Override
	public AccountWizardDto convertTypeToDto(AccountWizardsService wizard) {
		return null;
	}

	@Override
	public AccountWizardDto execute(AccountWizardDto wizardDto) {
		return null;
	}

	@Override
	public AccountWizardDto load(AccountWizardDto wizardDto) {
		return null;
	}
}
