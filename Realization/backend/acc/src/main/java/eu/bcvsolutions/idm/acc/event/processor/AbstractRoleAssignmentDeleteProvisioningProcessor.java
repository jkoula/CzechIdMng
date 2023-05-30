package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleAssignmentDeleteProvisioningProcessor<O extends AbstractDto> extends AbstractEntityEventProcessor<AbstractRoleAssignmentDto> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractRoleAssignmentDeleteProvisioningProcessor.class);
    //
    private final ProvisioningService provisioningService;
    private final SysRoleSystemService roleSystemService;
    private final AccAccountService accountService;
    private final SysSystemGroupSystemService systemGroupSystemService;
    private final EntityEventManager entityEventManager;

    protected AbstractRoleAssignmentDeleteProvisioningProcessor(ProvisioningService provisioningService,SysRoleSystemService roleSystemService,
            AccAccountService accountService, SysSystemGroupSystemService systemGroupSystemService, EntityEventManager entityEventManager) {
        super(AbstractRoleAssignmentEvent.RoleAssignmentEventType.DELETE);
        //
        this.provisioningService = provisioningService;
        this.roleSystemService = roleSystemService;
        this.accountService = accountService;
        this.systemGroupSystemService = systemGroupSystemService;
        this.entityEventManager = entityEventManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EventResult<AbstractRoleAssignmentDto> process(EntityEvent<AbstractRoleAssignmentDto> event) {
        AbstractRoleAssignmentDto roleAssignment = event.getContent();

        // If for this role doesn't exists any mapped system, then is provisioning useless!
        UUID roleId = roleAssignment.getRole();
        SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
        roleSystemFilter.setRoleId(roleId);
        long numberOfMappedSystem = roleSystemService.count(roleSystemFilter);
        if(numberOfMappedSystem == 0) {
            return new DefaultEventResult<>(event, this);
        }

        O owner = getOwner(roleAssignment);
        // getOwner may return null, if owner was not found. This indicates, that role assignment either has different owner type than this processor, or owner was deleted.
        if (owner == null) {
            LOG.debug("Owner for role assignment [{}] not found, skip provisioning.", roleAssignment.getId());
            return new DefaultEventResult<>(event, this);
        }

        Serializable accountsIdsObj = event.getProperties().get(AccAccountManagementService.ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE);
        List<UUID> accountsIds = null;
        if(accountsIdsObj instanceof List) {
            accountsIds =  (List<UUID>) accountsIdsObj;
        }

        if (accountsIds == null) {
            // We don't know about specific accounts, so we will execute provisioning for all accounts.
            LOG.debug("Call provisioning for owner [{}]", owner);
            provisioningService.doProvisioning(owner);

            return new DefaultEventResult<>(event, this);
        }

        // If default creation of accounts is disabled for this role-system (or system is in a cross-domain group), then relation between owner
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
                        if ((roleAssignment.getRoleSystem() == null || roleSystem.getId().equals(roleAssignment.getRoleSystem())
                                && systemGroupSystemService.count(systemGroupSystemFilter) >= 1)
                        ) {
                            // This role-system overriding a merge attribute which is using in
                            // active cross-domain group and roleSystem in owner-role is null or same as role-system.
                            // -> Provisioning should be made.
                            return true;
                        }
                    }
                    return false;
                })
                .forEach(roleSystem -> {
                    AccAccountFilter accountFilter = new AccAccountFilter();
                    accountFilter.setSystemId(roleSystem.getSystem());
                    accountFilter.setIdentityId(owner.getId());
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
                LOG.debug("Call provisioning for owner [{}] and account [{}]", owner, account.getUid());
                provisioningService.doProvisioning(account, owner);
            }
        });

        return new DefaultEventResult<>(event, this);
    }

    /**
     *  Account management should be executed from parent event - request.
     *  Look out, request event is already closed, when asynchronous processing is disabled.
     */
    @Override
    public boolean conditional(EntityEvent<AbstractRoleAssignmentDto> event) {
        return super.conditional(event)
                // Skip provisioning
                && (!this.getBooleanProperty(IdmAccountDto.SKIP_PROPAGATE, event.getProperties()))
                && (!this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties()))
                && (event.getRootId() == null || !entityEventManager.isRunnable(event.getRootId()));
    }

    @Override
    public int getOrder() {
        return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
    }

    protected abstract O getOwner(AbstractRoleAssignmentDto identityRole);
}
