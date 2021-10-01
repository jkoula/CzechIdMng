package eu.bcvsolutions.idm.core.api.domain;

/**
 * Interface for search in table IdmAudit, entities that implements this
 * interface is accessible for search.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface AuditSearchable {
	
	/**
	 * ID of owner this entity. This is the owner of the relation.
	 * @return
	 */
	String getOwnerId();

	/**
	 * Code (see {@link Codeable}) of owner.
	 * @return
	 */
	String getOwnerCode();

	/**
	 * Type of owner {@link Class.getName()}
	 * @return
	 */
	String getOwnerType();

	/**
	 * ID of sub entity of this relation. If return null entity isn't relation. 
	 * @return
	 */
	default String getSubOwnerId() {
		return null;
	}

	/**
	 * Code (see {@link Codeable}) of sub entity.
	 * @return
	 */
	default String getSubOwnerCode() {
		return null;
	}

	/**
	 * Type of sub owner {@link Class.getName()}
	 * @return
	 */
	default String getSubOwnerType() {
		return null;
	}
}
