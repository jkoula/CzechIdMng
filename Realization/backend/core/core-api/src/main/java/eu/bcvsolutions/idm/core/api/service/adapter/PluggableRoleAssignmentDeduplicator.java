package eu.bcvsolutions.idm.core.api.service.adapter;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;

/**
 * Beans of this type implement a method which compares two roles and find the duplicated one.
 * This role will them be unassigned from a user.
 *
 * These deduplicators are currently only used in {@link IdentityRoleByIdentityDeduplicationBulkAction}.
 * The way that role deduplications are implemented is that all the assigned roles need to pass all the
 * separate deduplicators in order to be unassigned.
 *
 * The main goal of this is to avoid deduplicating roles which are important to some module features.
 *
 * @author Tomáš Doischer
 */
public interface PluggableRoleAssignmentDeduplicator {

	/**
	 *  Check if {@link AbstractRoleAssignmentDto} <b>ONE</b> is duplicit against {@link AbstractRoleAssignmentDto} <b>TWO</b>.</br></br>
	 *
	 * @param one
	 * @param two
	 * @param skipSubdefinition
	 * @return
	 */
	AbstractRoleAssignmentDto getDuplicated(AbstractRoleAssignmentDto one, AbstractRoleAssignmentDto two, Boolean skipSubdefinition);

	/**
	 * The method getDuplicated returns one of the role assignment as duplicate. In some cases, a deduplicator may not
	 * differentiate between which role assignment is considered duplicate.
	 *
	 * @return
	 */
	boolean considerOrder();
}
