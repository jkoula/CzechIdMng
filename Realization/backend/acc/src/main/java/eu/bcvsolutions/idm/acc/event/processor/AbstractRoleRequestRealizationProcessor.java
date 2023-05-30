package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.impl.AbstractAccountManagementService;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleRequestProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Abstract realization of request in ACC module - ensure account management and provisioning.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractRoleRequestRealizationProcessor<O extends AbstractDto> extends CoreEventProcessor<IdmRoleRequestDto> implements RoleRequestProcessor {


    private static final Logger LOG = LoggerFactory.getLogger(AbstractRoleRequestRealizationProcessor.class);

    private final ProvisioningService provisioningService;
    private final AccAccountService accountService;

    protected AbstractRoleRequestRealizationProcessor(ProvisioningService provisioningService, AccAccountService accountService) {
        super(RoleRequestEvent.RoleRequestEventType.NOTIFY);
        this.provisioningService = provisioningService;
        this.accountService = accountService;
    }

    @Override
    public EventResult<IdmRoleRequestDto> process(EntityEvent<IdmRoleRequestDto> event) {
        IdmRoleRequestDto request = event.getContent();
        final O owner = getOwner(request);
        final var accountManagementService = getAccountManagementService();

        Set<AbstractRoleAssignmentDto> addedIdentityRoles = this.getSetProperty(AbstractRoleAssignmentEvent.PROPERTY_ASSIGNED_NEW_ROLES, event, AbstractRoleAssignmentDto.class);
        Set<AbstractRoleAssignmentDto> updatedIdentityRoles = this.getSetProperty(AbstractRoleAssignmentEvent.PROPERTY_ASSIGNED_UPDATED_ROLES, event, AbstractRoleAssignmentDto.class);
        Set<UUID> removedIdentityAccounts = this.getSetProperty(IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM, event, UUID.class);
        Set<UUID> accountsForAdditionalProvisioning = this.getSetProperty(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING, event, UUID.class);
        boolean skipProvisioning = this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties());

        Set<UUID> accountsForProvisioning = new HashSet<>(accountsForAdditionalProvisioning);

        if (!addedIdentityRoles.isEmpty()) {
            LOG.debug("Call account management for owner [{}] and new owner-roles [{}]", owner, addedIdentityRoles);
            List<UUID> accounts = accountManagementService.resolveNewRoleAssignments(owner, addedIdentityRoles.toArray(new AbstractRoleAssignmentDto[0]));
            addAccounts(accountsForProvisioning, accounts);
        }

        if (!updatedIdentityRoles.isEmpty()) {
            LOG.debug("Call account management for owner [{}] and updated owner-roles [{}]", owner, updatedIdentityRoles);
            List<UUID> accounts = accountManagementService.resolveUpdatedRoleAssignments(owner, updatedIdentityRoles.toArray(new AbstractRoleAssignmentDto[0]));
            addAccounts(accountsForProvisioning, accounts);
        }

        // Remove delayed owner-accounts (includes provisioning)
        if (!removedIdentityAccounts.isEmpty()) {
            LOG.debug("Call account management for owner [{}] - remove owner-accounts [{}]", owner, removedIdentityAccounts);
            //
            final Map<String, Serializable> eventProperties = Map.of(
                    AccIdentityAccountService.DELETE_TARGET_ACCOUNT_KEY, Boolean.TRUE,
                    AccIdentityAccountService.FORCE_DELETE_OF_IDENTITY_ACCOUNT_KEY, Boolean.FALSE,
                    IdmRoleRequestService.ROLE_REQUEST_ID_KEY, request.getId());
            //
            final var accountsToProvision = removedIdentityAccounts.stream()
                    .distinct()
                    .map(identityAccountId -> getAccountManagementService().publish(IdentityAccountEvent.IdentityAccountEventType.DELETE, identityAccountId, eventProperties))
                    .filter(Objects::nonNull)
                    .map(EntityAccountDto::getAccount)
                    .collect(Collectors.toList());
            accountsForProvisioning.addAll(accountsToProvision);
        }

        // Init context in owner DTO and set ID of role-request to it.
        initContext(owner, request);

        // Skip provisionig
        if (skipProvisioning) {
            return new DefaultEventResult<>(event, this);
        }

        // Provisioning for modified account
        return provisionAccounts(event, owner, accountsForProvisioning);
    }

    private DefaultEventResult<IdmRoleRequestDto> provisionAccounts(EntityEvent<IdmRoleRequestDto> event, O owner, Set<UUID> accountsForProvisioning) {
        accountsForProvisioning.forEach(accountId -> {
            AccAccountDto account = accountService.get(accountId);
            if (account != null) { // Account could be null (was deleted).
                LOG.debug("Call provisioning for owner [{}] and account [{}]", owner,
                        account.getUid());
                provisioningService.doProvisioning(account, owner);
            }
        });

        return new DefaultEventResult<>(event, this);
    }

    protected abstract AbstractAccountManagementService<O, ? extends EntityAccountDto> getAccountManagementService();

    protected abstract O getOwner(IdmRoleRequestDto request);

    /**
     * Init context in identity DTO and set ID of role-request to it.
     * @param owner DTO
     * @param request role request
     */
    protected abstract void initContext(O owner, IdmRoleRequestDto request);

    private void addAccounts(Set<UUID> accountsForProvisioning, List<UUID> accounts) {
        if (accounts != null) {
            accountsForProvisioning.addAll(accounts);
        }
    }

    @Override
    public boolean conditional(EntityEvent<IdmRoleRequestDto> event) {
        return super.conditional(event) &&
                getOwnerClass().getCanonicalName().equals(event.getContent().getApplicantInfo().getApplicantType());
    }

    protected abstract Class<O> getOwnerClass();

    @Override
    public int getOrder() {
        return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
    }
}
