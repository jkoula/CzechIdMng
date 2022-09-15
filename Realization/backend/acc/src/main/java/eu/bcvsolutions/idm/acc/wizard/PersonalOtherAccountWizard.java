package eu.bcvsolutions.idm.acc.wizard;

/**
 * Wizard for personal other accounts
 * @author Roman Kucera
 */
public class PersonalOtherAccountWizard extends AbstractAccountWizard {

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supports() {
		return true;
	}
}
