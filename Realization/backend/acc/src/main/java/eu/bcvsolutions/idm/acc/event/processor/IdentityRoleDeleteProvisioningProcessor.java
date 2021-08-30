package eu.bcvsolutions.idm.acc.event.processor;

import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity provisioning after role has been deleted.
 *
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Component(IdentityRoleDeleteProvisioningProcessor.PROCESSOR_NAME)
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after identity role is deleted.")
public class IdentityRoleDeleteProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-provisioning-processor";
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleDeleteProvisioningProcessor.class);
	//
	@Autowired private ProvisioningService provisioningService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private EntityEventManager entityEventManager;
	@Autowired private SysRoleSystemService roleSystemService;
	@Autowired private AccAccountService accountService;
	@Autowired private SysSystemGroupSystemService systemGroupSystemService;

	public IdentityRoleDeleteProvisioningProcessor() {
		super(IdentityRoleEventType.DELETE);
	}
	
	/**
	 *  Account management should be executed from parent event - request. 
	 *  Look out, request event is already closed, when asynchronous processing is disabled.
	 */
	@Override
	public boolean conditional(EntityEvent<IdmIdentityRoleDto> event) {
		return super.conditional(event)
				// Skip provisioning
				&& (!this.getBooleanProperty(IdmAccountDto.SKIP_PROPAGATE, event.getProperties()))
				&& (!this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties()))
				&& (event.getRootId() == null || !entityEventManager.isRunnable(event.getRootId())) ;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@SuppressWarnings("unchecked")
	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		
		// If for this role doesn't exists any mapped system, then is provisioning useless!
		UUID roleId = identityRole.getRole();
		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setRoleId(roleId);
		long numberOfMappedSystem = roleSystemService.count(roleSystemFilter);
		if(numberOfMappedSystem == 0) {
			return new DefaultEventResult<>(event, this);
		}
		
		// TODO: Optimalization - load identity by identity-role with filter
		IdmIdentityContractDto identityContract = identityContractService.get(identityRole.getIdentityContract());
		IdmIdentityDto identity = DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity);
		
		Serializable accountsIdsObj = event.getProperties().get(AccAccountManagementService.ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE);
		List<UUID> accountsIds = null;
		if(accountsIdsObj instanceof List) {
			accountsIds =  (List<UUID>) accountsIdsObj;
		}
		
		if (accountsIds == null) {
			// We don't know about specific accounts, so we will execute provisioning for all accounts.
			LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
			provisioningService.doProvisioning(identity);
			
			return new DefaultEventResult<>(event, this);
		}
		
		// If default creation of accounts is disabled for this role-system (or system is in a cross-domain group), then relation between identity
		// and account may not exist. In this scenario we have to made provisioning too.
		// So we try to find these role-systems and its accounts.
		SysRoleSystemFilter roleSystemForProvisioningFilter = new SysRoleSystemFilter();
		roleSystemForProvisioningFilter.setRoleId(roleId);

		List<UUID> finalAccountsIds = accountsIds;
		roleSystemService.find(roleSystemForProvisioningFilter, null).getContent().stream()
				.filter(roleSystem -> {
					if (!roleSystem.isCreateAccountByDefault()) {
						return true;
					} else {
						SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
						systemGroupSystemFilter.setCrossDomainsGroupsForRoleSystemId(roleSystem.getId());
						if ((identityRole.getRoleSystem() == null || roleSystem.getId().equals(identityRole.getRoleSystem())
								&& systemGroupSystemService.count(systemGroupSystemFilter) >= 1)
						) {
							// This role-system overriding a merge attribute which is using in
							// active cross-domain group and roleSystem in identity-role is null or same as role-system.
							// -> Provisioning should be made.
							return true;
						}
					}
					return false;
				})
				.forEach(roleSystem -> {
					AccAccountFilter accountFilter = new AccAccountFilter();
					accountFilter.setSystemId(roleSystem.getSystem());
					accountFilter.setIdentityId(identity.getId());
					accountService.find(accountFilter, null).getContent()
							.stream()
							.filter(account -> !finalAccountsIds.contains(account.getId()))
							.forEach(account -> {
								finalAccountsIds.add(account.getId());
							});
				});

		finalAccountsIds.forEach(accountId -> {
			AccAccountDto account = accountService.get(accountId);
			if (account != null) { // Account could be null (was deleted).
				LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(), account.getUid());
				provisioningService.doProvisioning(account, identity);
			}
		});
		
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}
