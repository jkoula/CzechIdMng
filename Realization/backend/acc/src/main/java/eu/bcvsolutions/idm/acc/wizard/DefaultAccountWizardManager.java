package eu.bcvsolutions.idm.acc.wizard;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.acc.dto.AccountWizardDto;
import eu.bcvsolutions.idm.acc.service.api.AccountWizardManager;
import eu.bcvsolutions.idm.acc.service.api.AccountWizardsService;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Implementation of wizard manager for accounts
 * @author Roman Kucera
 */

@Service("accountWizardManager")
public class DefaultAccountWizardManager implements AccountWizardManager {

	@Autowired
	private ApplicationContext context;
	@Lazy
	@Autowired
	private EnabledEvaluator enabledEvaluator;

	@Override
	public List<AccountWizardsService> getSupportedTypes() {
		return context
				.getBeansOfType(AccountWizardsService.class)
				.values()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.filter(AccountWizardsService::supports)
				.sorted(Comparator.comparing(AccountWizardsService::getOrder))
				.collect(Collectors.toList());
	}

	@Override
	public AccountWizardsService getWizardType(String id) {
		return this.getSupportedTypes().stream()
				.filter(type -> type.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	@Override
	public AccountWizardDto convertTypeToDto(AccountWizardsService wizard) {
		AccountWizardDto accountWizardDto = new AccountWizardDto();
		accountWizardDto.setId(wizard.getId());
		accountWizardDto.setName(wizard.getId());
		accountWizardDto.setModule(wizard.getModule());
		accountWizardDto.setMetadata(wizard.getMetadata());
		accountWizardDto.setOrder(wizard.getOrder());
		accountWizardDto.setFormDefinition(wizard.getFormDefinitionDto());

		return accountWizardDto;
	}

	@Override
	public AccountWizardDto execute(AccountWizardDto wizardDto) {
		Assert.notNull(wizardDto, "Wizard cannot be null!");
		Assert.notNull(wizardDto.getId(), "Wizard ID cannot be null!");
		AccountWizardsService wizardType = this.getWizardType(wizardDto.getId());
		Assert.notNull(wizardType, "Wizard type was not found!");

		return wizardType.execute(wizardDto);
	}

	@Override
	public AccountWizardDto load(AccountWizardDto wizardDto) {
		Assert.notNull(wizardDto, "Wizard cannot be null!");
		Assert.notNull(wizardDto.getId(), "Wizard ID cannot be null!");

		AccountWizardsService wizardType = this.getWizardType(wizardDto.getId());
		Assert.notNull(wizardType, "Wizard type was not found!");

		return wizardType.load(wizardDto);
	}
}
