package eu.bcvsolutions.idm.core.security.api.exception;

/**
 * Identity not found - identity cannot log in.
 * 
 * @author Radek Tomiška
 * @since 12.0.0
 */
public class IdentityNotFoundException extends IdmAuthenticationException {

	private static final long serialVersionUID = 1L;

	public IdentityNotFoundException(String msg) {
		super(msg);
	}
	
	public IdentityNotFoundException(String msg, Throwable ex) {
		super(msg, ex);
	}
}
