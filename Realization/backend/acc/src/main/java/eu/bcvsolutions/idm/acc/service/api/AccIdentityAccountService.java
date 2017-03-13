package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;
import java.util.UUID;

import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccIdentityAccountService extends ReadWriteEntityService<AccIdentityAccount, IdentityAccountFilter> {

	/**
	 * Delete identity account
	 * @param entity
	 * @param deleteAccount  If is true, then will be deleted (call provisioning) account on target system.
	 */
	void delete(AccIdentityAccount entity, boolean deleteAccount);
	
	/**
	 * Method return {@link AccIdentityAccount} for username and system id with ownership flag set to true.
	 * 
	 * @param username
	 * @param systemId
	 * @return
	 */
	List<AccIdentityAccount> getIdentityAccountsForUsernameAndSystem(String username, UUID systemId);
}
	