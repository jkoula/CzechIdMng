package eu.bcvsolutions.idm.doc.domain;

/**
 * Identity document state.
 *
 * @author Jirka Koula
 */
public enum DocumentState {
	VALID,
	INVALID;

	public boolean isValid() {
		return this == VALID;
	}
}
