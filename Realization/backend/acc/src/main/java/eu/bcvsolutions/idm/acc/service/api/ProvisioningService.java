package eu.bcvsolutions.idm.acc.service.api;

import java.util.List;

import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcUidAttribute;

/**
 * Service for do provisioning or synchronisation or reconciliation
 * 
 * @author svandav
 *
 */
public interface ProvisioningService {

	/**
	 * Do provisioning for given identity on all connected systems
	 * 
	 * @param identity
	 */
	void doProvisioning(IdmIdentity identity);
	
	/**
	 * Do provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doProvisioning(AccAccount account);
	
	/**
	 * Do provisioning for given account and identity
	 * @param account
	 * @param identity
	 * @param system
	 * @return
	 */
	void doProvisioning(AccAccount account, IdmIdentity identity);

	/**
	 * Do delete provisioning for given account on connected system
	 * 
	 * @param account
	 */
	void doDeleteProvisioning(AccAccount account);
	
	/**
	 * 
	 * Change password for selected identity accounts.
	 * @param identity
	 * @param passwordChange
	 */
	void changePassword(IdmIdentity identity, PasswordChangeDto passwordChange);
	
	/**
	 * Do provisioning only for single attribute. For example, it is needed to change password
	 * 
	 * @param systemEntity
	 * @param mappedAttribute
	 * @param value
	 * @param system
	 * @param operationType
	 * @param entity
	 */
	void doProvisioningForAttribute(SysSystemEntity systemEntity, AttributeMapping mappedAttribute, Object value,
			ProvisioningOperationType operationType, AbstractEntity entity);
	
	/**
	 * Do authenticate check for given username and password on target resource
	 * @param username
	 * @param password
	 * @param system
	 * @param entityType
	 * @return
	 */
	IcUidAttribute authenticate(String username, GuardedString password, SysSystem system, SystemEntityType entityType);

	/**
	 * Convert method for SysRoleSystemAttribute to mapping attribute dto
	 * @param overloadingAttribute
	 * @param overloadedAttribute
	 */
	void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute, AttributeMapping overloadedAttribute);

	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param uid
	 * @param account
	 * @param identity
	 * @param system
	 * @param entityType
	 * @return
	 */
	List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, IdmIdentity identity, SysSystem system, SystemEntityType entityType);

	/**
	 * Create final list of attributes for provisioning.
	 * 
	 * @param identityAccount
	 * @param defaultAttributes
	 * @param overloadingAttributes
	 * @return
	 */
	List<AttributeMapping> compileAttributes(List<? extends AttributeMapping> defaultAttributes,
			List<SysRoleSystemAttribute> overloadingAttributes);


	
}