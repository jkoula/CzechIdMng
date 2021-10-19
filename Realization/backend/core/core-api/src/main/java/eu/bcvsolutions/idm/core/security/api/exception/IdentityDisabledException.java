package eu.bcvsolutions.idm.core.security.api.exception;

/**
 * Identity is disabled - identity cannot log in.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
public class IdentityDisabledException extends IdmAuthenticationException {

	private static final long serialVersionUID = 1L;

	public IdentityDisabledException(String msg) {
		super(msg);
	}
	
	public IdentityDisabledException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
