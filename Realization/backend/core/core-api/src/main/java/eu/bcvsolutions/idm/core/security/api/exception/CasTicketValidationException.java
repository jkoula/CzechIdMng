package eu.bcvsolutions.idm.core.security.api.exception;

/**
 * Cas ticket validation failed.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
public class CasTicketValidationException extends IdmAuthenticationException {

	private static final long serialVersionUID = 1L;

	public CasTicketValidationException(String msg) {
		super(msg);
	}
	
	public CasTicketValidationException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
