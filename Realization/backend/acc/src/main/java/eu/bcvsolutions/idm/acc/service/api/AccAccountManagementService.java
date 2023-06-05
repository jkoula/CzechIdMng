package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;

/**
 * 
 * @author Vít Švanda
 *
 */
public interface AccAccountManagementService extends EntityAccountManagementService<IdmIdentityDto> {
	
	/**
	 * We needs accounts (IDs) which were connected to deleted identity-role in next processors (we want to execute provisioning only for that accounts).
	 */
	String ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE = "account-ids-for-deleted-identity-role";

	/**
	 * Create or delete accounts for this identity according their roles
	 * @param identity
	 * @return
	 */
	boolean resolveIdentityAccounts(IdmIdentityDto identity);

	/**
	 * Identity role is deleting, we have to delete linked identity accounts
	 * 
	 * @param identityRole
	 * @return list of accounts IDs (used this identity-role)
	 */
	List<UUID> deleteIdentityAccount(AbstractRoleAssignmentDto identityRole);
	
	/**
	 * Identity role is deleting, we have to delete linked identity accounts, or mark them for delete
	 * 
	 * @param event
	 */
	void deleteIdentityAccount(EntityEvent<AbstractRoleAssignmentDto> event);

	/**
	 * Create new identity-accounts and accounts for given identity-roles
	 * 
	 * @param identity
	 * @param identityRoles
	 * @return List account's IDs for modified by this action (for this accounts provisioning should be executed).
	 */
	List<UUID> resolveNewIdentityRoles(IdmIdentityDto identity, AbstractRoleAssignmentDto... identityRoles);

	/**
	 * Create or delete identity-accounts and accounts for given identity-roles
	 * 
	 * @param identity
	 * @param identityRoles
	 * @return List account's IDs for modified by this action (for this accounts provisioning should be executed).
	 */
	List<UUID> resolveUpdatedIdentityRoles(IdmIdentityDto identity, AbstractRoleAssignmentDto... identityRoles);
}
