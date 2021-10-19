package eu.bcvsolutions.idm.core.security.service.impl;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas30ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidationException;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.security.api.service.CasValidationService;

/**
 * Wrapper for CAS ticket validation.
 *
 * @author Roman Kučera
 * @since 12.0.0
 */
@Service("casValidationService")
public class DefaultCasValidationService implements CasValidationService {

	@Override
	public Assertion validate(String ticket, String service, String casUrl) throws TicketValidationException {
		Cas30ServiceTicketValidator cas30ServiceTicketValidator = new Cas30ServiceTicketValidator(casUrl);
		// We need to set renew to false, otherwise CAS will require username and password together with token.
		cas30ServiceTicketValidator.setRenew(false);
		return cas30ServiceTicketValidator.validate(ticket, service);
	}
}