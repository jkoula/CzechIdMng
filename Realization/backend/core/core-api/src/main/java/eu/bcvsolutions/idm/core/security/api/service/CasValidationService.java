package eu.bcvsolutions.idm.core.security.api.service;

import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;

/**
 * Provide ticket validation for CAS.
 *
 * @author Roman Kučera
 * @since 12.0.0
 */
public interface CasValidationService {

	/**
	 * Validate token in CAS.
	 * 
	 * @param ticket which will be validated
	 * @param service IdM URL - same as in CAS service configuration
	 * @param casUrl CAS URL - where CAS is accessible
	 * @return
	 */
	Assertion validate(String ticket, String service, String casUrl) throws TicketValidationException;
}