package eu.bcvsolutions.idm.doc.domain;


/**
 * Identity document state.
 *
 * @author Jirka Koula
 */
public enum DocumentType {
	ID_CARD("national ID card"),
	PASSPORT("passport"),
	DRIVING_LICENCE("driving licence");

	private final String name;

	private DocumentType(String name) { this.name = name; }

	public String getName() { return name; }
}
