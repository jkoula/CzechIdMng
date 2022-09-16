package eu.bcvsolutions.idm.acc.wizard;

import org.springframework.stereotype.Component;

/**
 * Wizard for personal accounts
 * @author Roman Kucera
 */
@Component(PersonalAccountWizard.NAME)
public class PersonalAccountWizard extends AbstractAccountWizard {

	public static final String NAME = "personal-account-wizard";

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports() {
		return true;
	}
}
