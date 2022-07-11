package eu.bcvsolutions.idm.acc.service.api;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;
import eu.bcvsolutions.idm.core.eav.api.service.FormableDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;
import eu.bcvsolutions.idm.ic.api.IcConnectorObject;

/**
 * Accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccAccountService extends//
		FormableDtoService<AccAccountDto, AccAccountFilter>, //
		ScriptEnabled, //
		AuthorizableService<AccAccountDto> {

	/**
	 * DeleteTargetAccount If is true, then will be call provisioning
	 */
	String DELETE_TARGET_ACCOUNT_PROPERTY = "DELETE_TARGET_ACCOUNT";
	/**
	 * Id of entity connected to the account. Can be null, but provisioning archive
	 * will not have correct information.
	 */
	String ENTITY_ID_PROPERTY = "ENTITY_ID";

	/**
	 * Get accounts for identity on system.
	 * 
	 * @param systemId
	 * @param identityId
	 * @return
	 */
	List<AccAccountDto> getAccounts(UUID systemId, UUID identityId);

	/**
	 * Returns account found by UID on given system.
	 * 
	 * @param uid
	 * @param systemId
	 * @return
	 */
	AccAccountDto getAccount(String uid, UUID systemId);

	/**
	 * Returns accounts with expired protection. Account has to be in protection
	 * mode.
	 * 
	 * @param expirationDate
	 * @param pageable
	 * @return
	 */
	Page<AccAccountDto> findExpired(ZonedDateTime expirationDate, Pageable pageable);

	/**
	 * Load object from the connector.
	 * Loading additional values (for example values from other systems in cross-domain groups.).
	 * 
	 * @param account
	 * @param permissions
	 * @return
	 */
	IcConnectorObject getConnectorObject(AccAccountDto account, BasePermission... permissions);

	/**
	 * Find sync executor for given entity type.
	 *
	 * @since 10.2.0
	 * @param entityType
	 * @return
	 */
	SynchronizationEntityExecutor getSyncExecutor(SystemEntityType entityType);
}
