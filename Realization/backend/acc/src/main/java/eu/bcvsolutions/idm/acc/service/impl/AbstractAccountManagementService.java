package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService.ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractAccountManagementService<O extends AbstractDto, A extends EntityAccountDto> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
            .getLogger(AbstractAccountManagementService.class);

    private final EntityStateManager entityStateManager;
    private final SysRoleSystemService roleSystemService;
    private final SysSystemAttributeMappingService systemAttributeMappingService;
    private final SysRoleSystemAttributeService roleSystemAttributeService;
    private final SysSystemGroupSystemService systemGroupSystemService;
    private final AccAccountService accountService;
    private final SysSystemMappingService systemMappingService;
    private final AccSchemaFormAttributeService schemaFormAttributeService;
    private final SysSchemaObjectClassService schemaObjectClassService;

    private final EntityEventManager entityEventManager;

    protected AbstractAccountManagementService(EntityStateManager entityStateManager, SysRoleSystemService roleSystemService, SysSystemAttributeMappingService systemAttributeMappingService,
            SysRoleSystemAttributeService roleSystemAttributeService, SysSystemGroupSystemService systemGroupSystemService, AccAccountService accountService,
            SysSystemMappingService systemMappingService, AccSchemaFormAttributeService schemaFormAttributeService, SysSchemaObjectClassService schemaObjectClassService,
            EntityEventManager entityEventManager) {
        this.entityStateManager = entityStateManager;
        this.roleSystemService = roleSystemService;
        this.systemAttributeMappingService = systemAttributeMappingService;
        this.roleSystemAttributeService = roleSystemAttributeService;
        this.systemGroupSystemService = systemGroupSystemService;
        this.accountService = accountService;
        this.systemMappingService = systemMappingService;
        this.schemaFormAttributeService = schemaFormAttributeService;
        this.schemaObjectClassService = schemaObjectClassService;
        this.entityEventManager = entityEventManager;
    }

    public boolean resolveAccounts(O owner) {
        Assert.notNull(owner, "Owner is required.");
        // find not deleted identity accounts
        List<A> allAccounts = fetchCurrentlyOwnedAccounts(owner);
        List<AbstractRoleAssignmentDto> roleAssignments = fetchCurrentRoleAssignments(owner);

        if (CollectionUtils.isEmpty(roleAssignments) && CollectionUtils.isEmpty(allAccounts)) {
            // No roles and accounts ... we don't have anything to do
            return false;
        }

        // account with delete accepted states will be removed on the end
        IdmEntityStateFilter accountStateFilter = new IdmEntityStateFilter();
        accountStateFilter.setSuperOwnerId(owner.getId());
        accountStateFilter.setOwnerType(entityStateManager.getOwnerType(getAccountType()));
        accountStateFilter.setResultCode(CoreResultCode.DELETED.getCode());
        List<IdmEntityStateDto> entityAccountStates = entityStateManager.findStates(accountStateFilter, null).getContent();
        List<A> entityAccountsList = allAccounts
                .stream()
                .filter(ia -> entityAccountStates
                        .stream()
                        .noneMatch(state -> ia.getId().equals(state.getOwnerId())))
                .collect(Collectors.toList());

        // create / remove accounts
        if (!CollectionUtils.isEmpty(roleAssignments) || !CollectionUtils.isEmpty(entityAccountsList)) {
            List<A> identityAccountsToCreate = new ArrayList<>();
            List<A> identityAccountsToDelete = new ArrayList<>();

            // Is role valid in this moment
            resolveIdentityAccountForCreate(owner, entityAccountsList, roleAssignments, identityAccountsToCreate,
                    identityAccountsToDelete, false, null);

            // Is role invalid in this moment
            resolveIdentityAccountForDelete(entityAccountsList, roleAssignments, identityAccountsToDelete);

            // Create new identity accounts
            identityAccountsToCreate.forEach(getAccountService()::save);

            // Delete invalid identity accounts
            identityAccountsToDelete.forEach(identityAccount -> getAccountService().deleteById(identityAccount.getId()));
        }
        // clear identity accounts marked to be deleted
        entityAccountStates //
                .forEach(state -> {	//
                    A deleteIdentityAccount = getAccountService().get(state.getOwnerId());
                    if (deleteIdentityAccount != null) {
                        // identity account can be deleted manually.
                        getAccountService().delete(deleteIdentityAccount);
                    }
                    entityStateManager.deleteState(state);
                });

        // Return value is deprecated since version 9.5.0  (is useless)
        return true;
    }

    public List<UUID> resolveNewRoleAssignments(O owner, AbstractRoleAssignmentDto[] identityRoles) {
        Assert.notNull(owner, "Identity is required.");

        if (identityRoles == null || identityRoles.length == 0) {
            // No identity-roles ... we don't have anything to do
            return null;
        }

        List<AbstractRoleAssignmentDto> identityRolesList = Lists.newArrayList(identityRoles);
        List<A> identityAccountsToCreate = Lists.newArrayList();

        // For this account should be executed provisioning
        List<UUID> accounts = Lists.newArrayList();

        // Is role valid in this moment
        resolveIdentityAccountForCreate(owner, Lists.newArrayList(), identityRolesList, identityAccountsToCreate,
                Lists.newArrayList(), true, accounts);

        // Create new identity accounts
        identityAccountsToCreate.forEach(identityAccount ->
                // Check if this identity-account already exists, if yes then is his account ID
                // add to accounts (for provisioning).
                createIdentityAccountIfNotExists(accounts, identityAccount));
        return accounts;
    }

    public List<UUID> resolveUpdatedRoleAssignments(O owner, AbstractRoleAssignmentDto[] identityRoles) {
        Assert.notNull(owner, "Identity is required.");

        if (identityRoles == null || identityRoles.length == 0) {
            // No identity-roles ... we don't have anything to do
            return Collections.emptyList();
        }
        List<AbstractRoleAssignmentDto> identityRolesList = Lists.newArrayList(identityRoles);
        // Find identity-accounts for changed identity-roles (using IN predicate)
        List<A> identityAccountList = getAccountsForRoleAssignments(owner, identityRolesList);

        // create / remove accounts
        List<A> identityAccountsToCreate = new ArrayList<>();
        List<A> identityAccountsToDelete = new ArrayList<>();

        // Is role valid in this moment
        resolveIdentityAccountForCreate(owner, identityAccountList, identityRolesList, identityAccountsToCreate,
                identityAccountsToDelete, false, null);

        // Is role invalid in this moment
        resolveIdentityAccountForDelete(identityAccountList, identityRolesList, identityAccountsToDelete);

        // For this accounts should be execute a provisioning. We have to execute provisioning for all changed accounts
        // although identity-role was not changed (EAV attributes could have been changed).
        List<UUID> accounts = identityAccountList.stream() //
                .map(EntityAccountDto::getAccount) //
                .distinct() //
                .collect(Collectors.toList());

        // Create new identity accounts
        identityAccountsToCreate.forEach(identityAccount ->
                // Check if this identity-account already exists, if yes then is his account ID
                // add to accounts (for provisioning).
                createIdentityAccountIfNotExists(accounts, identityAccount));

        // Delete invalid identity accounts
        identityAccountsToDelete.forEach(identityAccount -> getAccountService().deleteById(identityAccount.getId()));

        return accounts;
    }

    @Transactional
    public List<UUID> deleteEntityAccounts(AbstractRoleAssignmentDto entity) {
        List<UUID> accountIds = Lists.newArrayList();

        getAccountsForRoleAssignments(null, Collections.singletonList(entity)) //
                .forEach(identityAccount -> { //
                    accountIds.add(identityAccount.getAccount());
                    getAccountService().delete(identityAccount);
                });
        return accountIds;
    }

    @Transactional
    public void deleteEntityAccount(EntityEvent<AbstractRoleAssignmentDto> event) {
        Assert.notNull(event, "Event is required.");
        AbstractRoleAssignmentDto identityRole = event.getContent();
        Assert.notNull(identityRole, "Identity role is required.");
        Assert.notNull(identityRole, "Identity role identifier is required.");
        //
        boolean skipPropagate = event.getBooleanProperty(IdmAccountDto.SKIP_PROPAGATE);
        boolean bulk = event.getRootId() != null
                && entityEventManager.isRunnable(event.getRootId())
                && !entityEventManager.getEvent(event.getRootId()).getOwnerType() // check parent event is not role request
                .equals(entityEventManager.getOwnerType(IdmRoleRequestDto.class));

        if (!skipPropagate && !bulk) {
            // role is deleted without request or without any parent ... we need to remove account synchronously
            List<UUID> accountIds = deleteEntityAccounts(identityRole);
            // We needs accounts which were connected to deleted identity-role in next
            // processor (we want to execute provisioning only for that accounts)
            event.getProperties().put(ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE, (Serializable) accountIds);
            return;
        }

        // Role is deleted in bulk (e.g. role request) - account management has to be called outside
        // we just mark identity account to be deleted and remove identity role
        getAccountsForRoleAssignments(null, Collections.singletonList(identityRole)) //
                .forEach(identityAccount -> { //
                    // Set relation on identity-role to null
                    prepareAccountForDelete(identityAccount);


                    if (bulk) {
                        // For bulk create entity state for identity account.
                        IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
                        stateDeleted.setSuperOwnerId(identityAccount.getEntity());
                        stateDeleted.setResult(new OperationResultDto.Builder(OperationState.RUNNING)
                                .setModel(new DefaultResultModel(CoreResultCode.DELETED)).build());
                        entityStateManager.saveState(identityAccount, stateDeleted);
                    } else {
                        // Noting identity-accounts for delayed delete and account management
                        notingIdentityAccountForDelayedAcm(event, identityAccount, IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM);
                    }
                    getAccountService().save(identityAccount);
                });

        // If default creation of accounts is disabled for this role-system  (or system is in a cross-domain group), then relation between identity
        // and account may not exist. In this scenario we have to made provisioning too.
        // So we try to find these role-systems and its accounts.
        SysRoleSystemFilter roleSystemForProvisioningFilter = new SysRoleSystemFilter();
        roleSystemForProvisioningFilter.setRoleId(identityRole.getRole());

        roleSystemService.find(roleSystemForProvisioningFilter, null).getContent().stream()
                .filter(roleSystem -> shouldProcessDelayedAcm(identityRole, roleSystem))
                .forEach(roleSystem -> scheduleAllAccountsWithSameOwnerToDelayedAcm(event, identityRole, roleSystem));
    }

    protected void prepareAccountForDelete(A account){
        // Not doing anything by default
    }

    protected abstract List<A> getAccountsForRoleAssignments(O owner, List<AbstractRoleAssignmentDto> identityRolesList);

    /**
     * Resolve Identity account - to create.
     */
    private void resolveIdentityAccountForCreate(O owner,
            List<A> identityAccountList,
            List<AbstractRoleAssignmentDto> identityRoles,
            List<A> identityAccountsToCreate,
            List<A> identityAccountsToDelete,
            boolean onlyCreateNew,
            List<UUID> additionalAccountsForProvisioning
    ) {

        identityRoles.forEach(identityRole -> {
            SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
            roleSystemFilter.setRoleId(identityRole.getRole());
            List<SysRoleSystemDto> roleSystems = roleSystemService.find(roleSystemFilter, null).getContent();

            // Is role valid in this moment or
            // role-system has enabled forward account management (identity-role have to be
            // valid in the future)
            roleSystems.stream()
                    .filter(roleSystem -> (identityRole.isValid()
                            || (roleSystem.isForwardAccountManagemen() && identityRole.isValidNowOrInFuture())))
                    // Create account only if role-systems supports creation by default (not for cross-domains).
                    .filter(roleSystem -> supportsAccountCreation(roleSystem, owner, additionalAccountsForProvisioning))
                    .forEach(roleSystem ->
                            resolveIdentityAccountForCreate(owner, identityAccountList, identityAccountsToCreate,
                                    identityAccountsToDelete, onlyCreateNew, roleSystem, identityRole)
                    );
        });
    }

    private void resolveIdentityAccountForCreate(O owner,
            List<A> identityAccountList,
            List<A> identityAccountsToCreate,
            List<A> identityAccountsToDelete,
            boolean onlyCreateNew,
            SysRoleSystemDto roleSystem,
            AbstractRoleAssignmentDto identityRole)  {
        String uid = generateUID(owner, roleSystem);
        // Check on change of UID is not executed if all given owner-roles are new
        if (!onlyCreateNew) {
            // Check owner-account for that role-system on change the definition of UID
            checkOnChangeUID(uid, roleSystem, identityAccountList, identityAccountsToDelete);
        }

        // Try to find owner-account for this owner-role. If exists and doesn't in
        // list of owner-account to delete, then we are done.
        A existsIdentityAccount = findAlreadyExistsIdentityAccount(
                identityAccountList, identityAccountsToDelete, identityRole, roleSystem);

        if (existsIdentityAccount != null) {
            updateRoleSystemRelationIfNeeded(existsIdentityAccount, roleSystem);
            return;
        }

        // For this system we need to create new (or found exists) account
        AccAccountDto account = createAccountByRoleSystem(uid, owner, roleSystem,
                identityAccountsToCreate);
        if (account == null) {
            return;
        }

        // Prevent to create the same owner account
        if (identityAccountList.stream().noneMatch(identityAccount -> isSameAccount(identityAccount, account, roleSystem, identityRole))) {
            identityAccountsToCreate.add(createAccountDto(owner, roleSystem, identityRole, account));
        }
    }

    protected abstract A createAccountDto(O owner, SysRoleSystemDto roleSystem, AbstractRoleAssignmentDto identityRole, AccAccountDto account);

    @SuppressWarnings("unused")
    protected boolean isSameAccount(A entityAccount, AccAccountDto account, SysRoleSystemDto roleSystem, AbstractRoleAssignmentDto roleAssignment) {
        // not all entity accounts have to have link to role system or role assignment
        return account == null || entityAccount.getAccount().equals(account.getId());
    }

    protected void updateRoleSystemRelationIfNeeded(A existsIdentityAccount, SysRoleSystemDto roleSystem) {
        // by default this does nothing because not all entity accounts have role system
    }

    /**
     * Resolve identity account to delete
     *
     * @param identityAccountList
     * @param roleAssignments
     * @param identityAccountsToDelete
     */
    private void resolveIdentityAccountForDelete(List<A> identityAccountList,
            List<AbstractRoleAssignmentDto> roleAssignments, List<A> identityAccountsToDelete) {
        shouldAccountBeDeleted(identityAccountList, roleAssignments, identityAccountsToDelete);
    }

    protected abstract void shouldAccountBeDeleted(List<A> account, List<AbstractRoleAssignmentDto> roleAssignments, List<A> identityAccountsToDelete);

    private boolean supportsAccountCreation(SysRoleSystemDto roleSystem, O identity, List<UUID> additionalAccountsForProvisioning)  {
        boolean canBeCreated = roleSystem.isCreateAccountByDefault();
        if (canBeCreated) {
            SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
            systemGroupSystemFilter.setCrossDomainsGroupsForRoleSystemId(roleSystem.getId());
            if (systemGroupSystemService.count(systemGroupSystemFilter) >= 1) {
                // This role-system overriding a merge attribute which is using in
                // active cross-domain group. -> Account will not be created.
                canBeCreated = false;
            }
        }
        //
        if (!canBeCreated) {
            // We need to do provisioning for skipped identity-role/accounts (because Cross-domains).
            // We have to find all identity-accounts for identity and system.
            List<A> accountsForProvisioning = getOwnerAccountsOnSystem(identity, roleSystem.getSystem())
                    .filter(entityAccount -> shouldAccountBeProvisioned(entityAccount, roleSystem))
                    .collect(Collectors.toList());
            if (additionalAccountsForProvisioning != null) {
                additionalAccountsForProvisioning.addAll(accountsForProvisioning.stream().map(EntityAccountDto::getAccount).collect(Collectors.toList()));
            }
        }
        return canBeCreated;
    }

    protected abstract boolean shouldAccountBeProvisioned(A entityAccount, SysRoleSystemDto roleSystem);

    protected abstract Stream<A> getOwnerAccountsOnSystem(O identity, UUID system);

    /**
     * Check identity-account for that role-system on change the definition of UID
     *
     */
    private void checkOnChangeUID(String uid, SysRoleSystemDto roleSystem,
            List<A> identityAccountList, List<A> identityAccountsToDelete) {
        identityAccountList.forEach(identityAccount -> { //
            if (isSameAccount(identityAccount, null, roleSystem, null)) {
                // Has identity account same UID as account?
                AccAccountDto account = getAccountForEntityAccount(identityAccount);
                if (!uid.equals(account.getUid())) {
                    // We need to check that UID is not manually overridden because we cannot delete the account if it is
                    boolean uidOverridden = schemaFormAttributeService.isUidAttributeOverriddenForAccount(account);

                    // We found identityAccount for same identity and roleSystem, but this
                    // identityAccount is link to Account with different UID. It's probably means
                    // definition of UID
                    // (transformation) on roleSystem was changed. We have to delete this
                    // identityAccount.
                    if (!uidOverridden) {
                        identityAccountsToDelete.add(identityAccount);
                    }
                }
            }
        });
    }

    /**
     * Create Account by given roleSystem
     *
     * @param owner
     * @param roleSystem
     * @param identityAccountsToCreate
     * @return
     */
    private AccAccountDto createAccountByRoleSystem(String uid, O owner, SysRoleSystemDto roleSystem,
            List<A> identityAccountsToCreate) {

        // We try find account for same UID on same system
        // First we try to search same account in list for create new accounts
        A sameAccount = identityAccountsToCreate.stream() //
                .filter(identityAccountToCreate -> isSameAccount(identityAccountToCreate, null, roleSystem, null))
                .findFirst() //
                .orElse(null); //

        if (sameAccount != null) {
            return getAccountForEntityAccount(sameAccount);
        }

        // If account is not in the list accounts to create, then we will search in
        // database

        // Account management - can be the account created? - execute the script on the
        // system mapping
        SysSystemDto system = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system);
        SysSystemMappingDto mapping = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.systemMapping);
        if (mapping == null) {
            return null;
        }
        if (!this.canBeAccountCreated(uid, owner, mapping, system)) {
            LOG.info(
                    "For entity [{0}] and entity type [{1}] cannot be created the account (on system [{2}]),"
                            + " because script \"Can be account created\" on the mapping returned \"false\"!",
                    owner, IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getName());
            return null;
        }

        AccAccountFilter accountFilter = new AccAccountFilter();
        accountFilter.setUid(uid);
        accountFilter.setSystemId(roleSystem.getSystem());
        List<AccAccountDto> sameAccounts = accountService.find(accountFilter, null).getContent();
        // If account with same uid exists, we can say it's the same as the one that should be created only
        // if the original account has no mappingId, in other cases we need to compare mappingId
        if (!CollectionUtils.isEmpty(sameAccounts) && (sameAccounts.get(0).getSystemMapping() == null ||
                sameAccounts.get(0).getSystemMapping().equals(mapping.getId()))) {
            // We use existed account
            return sameAccounts.get(0);
        } else {
            // Create and persist new account
            return createAccount(uid, roleSystem, mapping.getId());
        }

    }

    protected abstract AccAccountDto getAccountForEntityAccount(A sameAccount);

    private AccAccountDto createAccount(String uid, SysRoleSystemDto roleSystem, UUID mappingId) {
        AccAccountFilter accountFilter = new AccAccountFilter();
        accountFilter.setUid(uid);
        accountFilter.setSystemId(roleSystem.getSystem());
        long numberOfAccountWithSameUid = accountService.count(accountFilter);
        if (numberOfAccountWithSameUid > 0) {
            throw new ResultCodeException(AccResultCode.PROVISIONING_ACCOUNT_UID_ALREADY_EXISTS,
                    ImmutableMap.of("uid", uid, "account", "", "system", roleSystem.getSystem(), "mapping", mappingId));
        }

        AccAccountDto account = new AccAccountDto();
        account.setUid(uid);
        account.setEntityType(IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE);
        account.setSystem(roleSystem.getSystem());
        account.setSystemMapping(mappingId);

        return accountService.save(account);
    }

    private boolean canBeAccountCreated(String uid, O dto, SysSystemMappingDto mapping,
            SysSystemDto system) {
        return systemMappingService.canBeAccountCreated(uid, dto, mapping.getCanBeAccountCreatedScript(), system);
    }

    /**
     * Try to find identity-account for this identity-role and system. If exists and doesn't in
     * list of identity-account to delete, then we are done.
     *
     * @param identityAccountList
     * @param identityAccountsToDelete
     * @param identityRole
     * @param roleSystem
     * @return
     */
    private A findAlreadyExistsIdentityAccount(List<A> identityAccountList,
            List<A> identityAccountsToDelete, AbstractRoleAssignmentDto identityRole, SysRoleSystemDto roleSystem) {
        return identityAccountList.stream() //
                .filter(entityAccount -> isAccountAssignedByAssignment(entityAccount, identityRole))
                .filter(entityAccount -> !identityAccountsToDelete.contains(entityAccount))
                .filter(entityAccount -> {
                        AccAccountDto account = getAccountForEntityAccount(entityAccount);
                        Assert.notNull(account.getSystem(), "System is required for account.");
                        return account.getSystem().equals(roleSystem.getSystem());
                }).findFirst().orElse(null);
    }

    protected abstract boolean isAccountAssignedByAssignment(A entityAccount, AbstractRoleAssignmentDto roleAssignment);


    /**
     * Finds all {@link eu.bcvsolutions.idm.acc.entity.AccIdentityAccount} for given {@link SysRoleSystemDto#getSystem()} and {@link AbstractRoleAssignmentDto#getEntity()}
     * and schedules delayed account management for these accounts.
     *
     * @param event
     * @param roleAssignment
     * @param roleSystem
     */
    private void scheduleAllAccountsWithSameOwnerToDelayedAcm(EntityEvent<AbstractRoleAssignmentDto> event, AbstractRoleAssignmentDto roleAssignment, SysRoleSystemDto roleSystem) {
        final O owner = getOwner(roleAssignment);
        if (owner == null) {
            // other type of assignment needs to be processed in other way
            return;
        }
        //
        getOwnerAccountsOnSystem(owner, roleSystem.getSystem())
                .forEach(identityAccount ->
                        // Noting identity-accounts for delayed additional provisioning.
                        notingIdentityAccountForDelayedAcm(event, identityAccount, IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING));
    }

    protected abstract O getOwner(AbstractRoleAssignmentDto roleAssignment);

    /**
     * This method decides, whether all accounts for given {@link AbstractRoleAssignmentDto} and {@link SysRoleSystemDto} should
     * be scheduled for delayed account management.
     *
     * Method returns true if one of the following is met:
     * - {@link SysRoleSystemDto#isCreateAccountByDefault()} is false
     * - roleSystem is in cross domain system group
     *
     * @param roleAssignment
     * @param roleSystem
     * @return true, if role does not create account by default, or is in cross domain group
     */
    private boolean shouldProcessDelayedAcm(AbstractRoleAssignmentDto roleAssignment, SysRoleSystemDto roleSystem) {
        if (!roleSystem.isCreateAccountByDefault()) {
            return true;
        } else {
            SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
            systemGroupSystemFilter.setCrossDomainsGroupsForRoleSystemId(roleSystem.getId());
            if (systemGroupSystemService.count(systemGroupSystemFilter) >= 1
                    && (roleAssignment.getRoleSystem() == null || roleSystem.getId().equals(roleAssignment.getRoleSystem()))
            ) {
                // This role-system overriding a merge attribute which is using in
                // active cross-domain group and roleSystem in identity-role is null or same as role-system.
                // -> Provisioning should be made.
                return true;
            }
        }
        return false;
    }



    /**
     * Create identity-account, but first check if this identity-account already
     * exist, if yes then is only his account ID add to accounts (for provisioning).
     *
     * @param accounts
     * @param entityAccount
     */
    private void createIdentityAccountIfNotExists(List<UUID> accounts, A entityAccount) {

        // check if account should be provisioned based on role mapping, must be same as the one for account
        AccAccountDto accountDto = getAccountForEntityAccount(entityAccount);
        if (accountDto != null) {
            // All identity-accounts with the same uid for this identity must have same system mapping
            getEntityAccountsForAccount(accountDto)
                    .filter(identAccount -> !haveSameSystemMapping(identAccount, entityAccount))
                    .findAny()
                    .ifPresent(acc -> {
                        throw new ResultCodeException(AccResultCode.PROVISIONING_ACCOUNT_UID_ALREADY_EXISTS,
                                Map.of("uid", accountDto.getUid(), "account", accountDto.getId(),
                                        "system", accountDto.getSystem(), "mapping", accountDto.getSystemMapping()));
                    });
        }
        //
        A existingAccount = getExistingSimilarAccount(entityAccount);
        //
        if (existingAccount == null) {
            A identityAccountDto = getAccountService().save(entityAccount);
            accounts.add(identityAccountDto.getAccount());
        } else {
            // If this identity-account already exists, then we need to add his account ID
            // (for execute the provisioning).
            accounts.add(existingAccount.getAccount());
        }
    }

    protected abstract boolean haveSameSystemMapping(A identAccount, A entityAccount);


    protected abstract Stream<A> getEntityAccountsForAccount(AccAccountDto accountDto);

    /**
     * Method for noting identity-accounts for delayed account management or delete.
     */
    private void notingIdentityAccountForDelayedAcm(EntityEvent<AbstractRoleAssignmentDto> event,
            EntityAccountDto entityAccount, String key) {
        Assert.notNull(entityAccount, "Identity account is required.");
        Assert.notNull(entityAccount.getId(), "Identity account identifier is required.");

        if (!event.getProperties().containsKey(key)) {
            event.getProperties().put(key,
                    new HashSet<UUID>());
        }

        @SuppressWarnings("unchecked") Set<UUID> ids = (Set<UUID>) event.getProperties()
                .get(key);

        if (key.equals(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING)) {
            // Add single account to parent event
            ids.add(entityAccount.getAccount());
        } else {
            // Add single identity-account to parent event
            ids.add(UUID.fromString(String.valueOf(entityAccount.getId())));
        }
    }


    // Abstract methods


    protected abstract A getExistingSimilarAccount(A entityAccount);

    /**
     * Return UID for this identity and roleSystem. First will be find and use
     * transform script from roleSystem attribute. If isn't UID attribute for
     * roleSystem defined, then will be use default UID attribute handling.
     *
     * @param entity
     * @param roleSystem
     * @return
     */
    public String generateUID(AbstractDto entity, SysRoleSystemDto roleSystem) {

        // Find attributes for this roleSystem
        SysRoleSystemAttributeFilter roleSystemAttrFilter = new SysRoleSystemAttributeFilter();
        roleSystemAttrFilter.setRoleSystemId(roleSystem.getId());
        roleSystemAttrFilter.setIsUid(Boolean.TRUE);

        List<SysRoleSystemAttributeDto> attributesUid = roleSystemAttributeService
                .find(roleSystemAttrFilter, null) //
                .getContent(); //

        if (attributesUid.size() > 1) {
            IdmRoleDto roleDto = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.role);
            SysSystemDto systemDto = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system);
            throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_MORE_UID,
                    ImmutableMap.of("role", roleDto.getCode(), "system", systemDto.getName()));
        }

        SysRoleSystemAttributeDto uidRoleAttribute = !attributesUid.isEmpty() ? attributesUid.get(0) : null;

        // If roleSystem UID attribute found, then we use his transformation
        // script.
        if (uidRoleAttribute != null) {
            // Default values (values from schema attribute handling)
            SysSystemAttributeMappingDto systemAttributeMapping = DtoUtils.getEmbedded(uidRoleAttribute,
                    SysRoleSystemAttribute_.systemAttributeMapping.getName(), SysSystemAttributeMappingDto.class);

            uidRoleAttribute.setSchemaAttribute(systemAttributeMapping.getSchemaAttribute());
            uidRoleAttribute.setTransformFromResourceScript(systemAttributeMapping.getTransformFromResourceScript());
            Object uid = systemAttributeMappingService.getAttributeValue(null, entity, uidRoleAttribute);
            if (uid == null) {
                SysSystemDto systemEntity = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.system);
                throw new ProvisioningException(AccResultCode.PROVISIONING_GENERATED_UID_IS_NULL,
                        ImmutableMap.of("system", systemEntity.getName()));
            }
            if (!(uid instanceof String)) {
                throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_UID_IS_NOT_STRING,
                        ImmutableMap.of("uid", uid));
            }
            return (String) uid;
        }

        // If roleSystem UID was not found, then we use default UID schema
        // attribute handling
        SysSystemAttributeMappingFilter attributeMappingFilter = new SysSystemAttributeMappingFilter();
        attributeMappingFilter.setSystemMappingId(roleSystem.getSystemMapping());
        attributeMappingFilter.setIsUid(Boolean.TRUE);
        attributeMappingFilter.setDisabledAttribute(Boolean.FALSE);
        List<SysSystemAttributeMappingDto> defaultUidAttributes = systemAttributeMappingService
                .find(attributeMappingFilter, null).getContent();
        if(defaultUidAttributes.size() == 1) {
            return systemAttributeMappingService.generateUid(entity, defaultUidAttributes.get(0));
        }

        // Default UID attribute was not correctly found, getUidAttribute method will be throw exception.
        // This is good time for loading the system (is used in exception message)
        SysSystemMappingDto mapping = systemMappingService.get(roleSystem.getSystemMapping());
        SysSchemaObjectClassDto objectClassDto = schemaObjectClassService.get(mapping.getObjectClass());
        SysSystemDto system = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system);
        systemAttributeMappingService.getUidAttribute(defaultUidAttributes, system);
        // Exception occurred
        return null;
    }

    protected abstract List<AbstractRoleAssignmentDto> fetchCurrentRoleAssignments(O owner);

    protected abstract Class<? extends A> getAccountType();

    protected abstract List<A> fetchCurrentlyOwnedAccounts(O owner);

    protected abstract ReadWriteDtoService<A, ? extends BaseFilter> getAccountService();


    /**
     * Publish event for provisioning.
     *
     * @param eventType     Type of event
     * @param accountId    Account identifier
     * @param eventProperties Properties for event
     * @return Account DTO
     */
    public abstract A publish(EventType eventType, UUID accountId, Map<String, Serializable> eventProperties);
}
