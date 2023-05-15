package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.EntityAccount;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccSchemaFormAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractAccountManagementService<O extends AbstractDto, A extends EntityAccountDto> {

    private final EntityStateManager entityStateManager;
    private final SysRoleSystemService roleSystemService;
    private final SysSystemAttributeMappingService systemAttributeMappingService;
    private final SysRoleSystemAttributeService roleSystemAttributeService;
    private final SysSystemGroupSystemService systemGroupSystemService;
    private final AccAccountService accountService;
    private final SysSystemMappingService systemMappingService;
    private final AccSchemaFormAttributeService schemaFormAttributeService;
    private final SysSchemaObjectClassService schemaObjectClassService;

    protected AbstractAccountManagementService(EntityStateManager entityStateManager, SysRoleSystemService roleSystemService, SysSystemAttributeMappingService systemAttributeMappingService,
            SysRoleSystemAttributeService roleSystemAttributeService, SysSystemGroupSystemService systemGroupSystemService, AccAccountService accountService,
            SysSystemMappingService systemMappingService, AccSchemaFormAttributeService schemaFormAttributeService, SysSchemaObjectClassService schemaObjectClassService) {
        this.entityStateManager = entityStateManager;
        this.roleSystemService = roleSystemService;
        this.systemAttributeMappingService = systemAttributeMappingService;
        this.roleSystemAttributeService = roleSystemAttributeService;
        this.systemGroupSystemService = systemGroupSystemService;
        this.accountService = accountService;
        this.systemMappingService = systemMappingService;
        this.schemaFormAttributeService = schemaFormAttributeService;
        this.schemaObjectClassService = schemaObjectClassService;
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
            if(existsIdentityAccount.getRoleSystem() == null) {
                // IdentityAccount already exist, but doesn't have relation on RoleSystem. This
                // could happen if system mapping was deleted and recreated or if was role use
                // as sync default role, but without mapping on this system.

                // We have to create missing relation, so we will set and save RoleSystem.
                existsIdentityAccount.setRoleSystem(roleSystem.getId());
                getAccountService().save(existsIdentityAccount);
            }
            return;
        }

        // For this system we need to create new (or found exists) account
        AccAccountDto account = createAccountByRoleSystem(uid, owner, roleSystem,
                identityAccountsToCreate);
        if (account == null) {
            return;
        }

        // Prevent to create the same owner account
        if (identityAccountList.stream().filter(identityAccount -> identityAccount.getAccount().equals(account.getId())
                && identityRole.getId().equals(identityAccount.getIdentityRole())
                && roleSystem.getId().equals(identityAccount.getRoleSystem())).count() == 0) {
            AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
            identityAccount.setAccount(account.getId());
            identityAccount.setIdentity(owner.getId());
            identityAccount.setIdentityRole(identityRole.getId());
            identityAccount.setRoleSystem(roleSystem.getId());
            identityAccount.setOwnership(true);
            identityAccount.getEmbedded().put(AccIdentityAccount_.account.getName(), account);

            identityAccountsToCreate.add(identityAccount);
        }
    }

    /**
     * Resolve identity account to delete
     *
     * @param identityAccountList
     * @param identityRoles
     * @param identityAccountsToDelete
     */
    private void resolveIdentityAccountForDelete(List<A> identityAccountList,
            List<AbstractRoleAssignmentDto> identityRoles, List<A> identityAccountsToDelete) {

        // Search IdentityAccounts to delete
        identityRoles.stream().filter(identityRole -> !identityRole.isValid())
                .forEach(identityRole -> identityAccountList.stream() //
                        .filter(identityAccount -> identityRole.getId().equals(identityAccount.getIdentityRole())) //
                        // Identity-account is not removed (even if that identity-role is invalid) if
                        // the role-system has enabled forward account management and identity-role will
                        // be valid in the future.
                        .filter(identityAccount -> identityAccount.getRoleSystem() == null
                                || !(((SysRoleSystemDto) DtoUtils
                                .getEmbedded(identityAccount, AccIdentityAccount_.roleSystem))
                                .isForwardAccountManagemen() && identityRole.isValidNowOrInFuture())) //
                        .forEach(identityAccountsToDelete::add));

        // Search IdentityAccounts to delete - we want to delete identity-account if
        // identity-role is valid, but mapped system on the role does not longer exist.
        identityRoles.stream().filter(ValidableEntity::isValid).forEach(identityRole ->
                identityAccountList.stream() //
                        .filter(identityAccount -> identityRole.getId().equals(identityAccount.getIdentityRole()))
                        .filter(identityAccount -> {
                            // Remove account if role-system is null.
                            if (identityAccount.getRoleSystem() == null) {
                                return true;
                            }
                            // Remove an account if role-system does not supports creation by default or if is in cross-domain group.
                            SysRoleSystemDto roleSystem = lookupService.lookupEmbeddedDto(identityAccount, AccIdentityAccount_.roleSystem);
                            if (roleSystem != null
                                    && !roleSystem.isCreateAccountByDefault()) {
                                return true;
                            } else if (roleSystem != null) {
                                SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
                                systemGroupSystemFilter.setCrossDomainsGroupsForRoleSystemId(roleSystem.getId());
                                if (systemGroupSystemService.count(systemGroupSystemFilter) >= 1) {
                                    // This role-system overriding a merge attribute which is using in
                                    // active cross-domain group. -> Identity account should be deleted.
                                    return true;
                                }
                            }
                            return false;
                        })
                        .forEach(identityAccountsToDelete::add));
    }

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
            AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
            identityAccountFilter.setSystemId(roleSystem.getSystem());
            identityAccountFilter.setIdentityId(identity.getId());
            List<AccIdentityAccountDto> identityAccountDto = identityAccountService.find(identityAccountFilter, null)
                    .getContent()
                    .stream()
                    .filter(accIdentityAccountDto -> accIdentityAccountDto.getRoleSystem() != null)
                    .filter(identityAccount -> {
                                SysRoleSystemDto roleSystemFromIdentityAccount = lookupService.lookupEmbeddedDto(identityAccount, AccIdentityAccount_.roleSystem);
                                return roleSystemFromIdentityAccount != null
                                        && roleSystem.getSystemMapping().equals(roleSystemFromIdentityAccount.getSystemMapping());
                            }
                    ).collect(Collectors.toList());
            if (additionalAccountsForProvisioning != null) {
                additionalAccountsForProvisioning.addAll(identityAccountDto.stream().map(AccIdentityAccountDto::getAccount).collect(Collectors.toList()));
            }
        }
        return canBeCreated;
    }

    /**
     * Check identity-account for that role-system on change the definition of UID
     *
     */
    private void checkOnChangeUID(String uid, SysRoleSystemDto roleSystem,
            List<A> identityAccountList, List<A> identityAccountsToDelete) {
        identityAccountList.forEach(identityAccount -> { //
            if (roleSystem.getId().equals(identityAccount.getRoleSystem())) {
                // Has identity account same UID as account?
                AccAccountDto account = AccIdentityAccountService.getEmbeddedAccount(identityAccount);
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
        AccIdentityAccountDto sameAccount = identityAccountsToCreate.stream() //
                .filter(identityAccountToCreate -> {
                    AccAccountDto account = DtoUtils.getEmbedded(identityAccountToCreate, AccIdentityAccount_.account.getName(), AccAccountDto.class);

                    return account.getUid().equals(uid)
                            && roleSystem.getId().equals(identityAccountToCreate.getRoleSystem());
                }).findFirst() //
                .orElse(null); //

        if (sameAccount != null) {
            return DtoUtils.getEmbedded(sameAccount, AccIdentityAccount_.account.getName(), AccAccountDto.class);
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
            LOG.info(MessageFormat.format(
                    "For entity [{0}] and entity type [{1}] cannot be created the account (on system [{2}]),"
                            + " because script \"Can be account created\" on the mapping returned \"false\"!",
                    owner.getCode(), IdentitySynchronizationExecutor.SYSTEM_ENTITY_TYPE, system.getName()));
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

    private boolean canBeAccountCreated(String uid, IdmIdentityDto dto, SysSystemMappingDto mapping,
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
                        AccAccountDto account = DtoUtils.getEmbedded(entityAccount, AccIdentityAccount_.account.getName(), AccAccountDto.class);
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
        final var serviceForAssignment = roleAssignmentManager.getServiceForAssignment(roleAssignment);
        final IdmIdentityDto relatedIdentity = serviceForAssignment.getRelatedIdentity(roleAssignment);
        if (relatedIdentity == null) {
            // other type of assignment needs to be processed in other way
            return;
        }
        //
        AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
        identityAccountFilter.setSystemId(roleSystem.getSystem());
        identityAccountFilter.setIdentityId(relatedIdentity.getId());
        identityAccountService.find(identityAccountFilter, null).getContent()
                .forEach(identityAccount ->
                        // Noting identity-accounts for delayed additional provisioning.
                        notingIdentityAccountForDelayedAcm(event, identityAccount, IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING));
    }

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
     * @param identityAccount
     */
    private void createIdentityAccountIfNotExists(List<UUID> accounts, AccIdentityAccountDto identityAccount) {
        AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
        identityAccountFilter.setIdentityId(identityAccount.getIdentity());
        identityAccountFilter.setIdentityRoleId(identityAccount.getIdentityRole());
        identityAccountFilter.setAccountId(identityAccount.getAccount());
        identityAccountFilter.setRoleSystemId(identityAccount.getRoleSystem());
        // Check if on exist same identity-account (for same identity-role, account and
        // role-system)
        long count = identityAccountService.count(identityAccountFilter);

        // check if account should be provisioned based on role mapping, must be same as the one for account
        AccAccountDto accountDto = DtoUtils.getEmbedded(identityAccount, AccIdentityAccount_.account, AccAccountDto.class, null);
        long accountWithSameMapping = 0;
        String accountSystem = "";
        String accountId = "";
        String accountMappingId = "";
        String accountUid = "";
        if (accountDto != null) {
            accountSystem = accountDto.getSystem() != null ? accountDto.getSystem().toString() : "";
            accountId = accountDto.getId() != null ? accountDto.getId().toString() : "";
            accountUid = accountDto.getUid();
            accountMappingId = accountDto.getSystemMapping() != null ? accountDto.getSystemMapping().toString() : "";

            IdmRequestIdentityRoleFilter rif = new IdmRequestIdentityRoleFilter();
            rif.setRoleAssignmentUuid(identityAccount.getIdentityRole());

            final List<AbstractRoleAssignmentDto> assignments = roleAssignmentManager.find(rif, null, (s, a) -> {});
            AbstractRoleAssignmentDto roleAssignmentDto = assignments.stream().findFirst().orElseThrow();
            SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
            roleSystemFilter.setRoleId(roleAssignmentDto.getRole());
            roleSystemFilter.setSystemId(accountDto.getSystem());
            roleSystemFilter.setSystemMappingId(accountDto.getSystemMapping());
            accountWithSameMapping = roleSystemService.count(roleSystemFilter);
        }
        if (accountWithSameMapping == 0) {
            if (count == 0) {
                AccIdentityAccountDto identityAccountDto = identityAccountService.save(identityAccount);
                accounts.add(identityAccountDto.getAccount());
            } else {
                // If this identity-account already exists, then we need to add his account ID
                // (for execute the provisioning).
                accounts.add(identityAccountService.find(identityAccountFilter, null).getContent().get(0).getAccount());
            }
        } else {
            throw new ResultCodeException(AccResultCode.PROVISIONING_ACCOUNT_UID_ALREADY_EXISTS,
                    ImmutableMap.of("uid", accountUid, "account", accountId, "system", accountSystem, "mapping", accountMappingId));
        }
    }

    /**
     * Method for noting identity-accounts for delayed account management or delete.
     */
    private void notingIdentityAccountForDelayedAcm(EntityEvent<AbstractRoleAssignmentDto> event,
            EntityAccountDto identityAccount, String key) {
        Assert.notNull(identityAccount, "Identity account is required.");
        Assert.notNull(identityAccount.getId(), "Identity account identifier is required.");

        if (!event.getProperties().containsKey(key)) {
            event.getProperties().put(key,
                    new HashSet<UUID>());
        }

        @SuppressWarnings("unchecked") Set<UUID> ids = (Set<UUID>) event.getProperties()
                .get(key);

        if (key.equals(IdmAccountDto.ACCOUNT_FOR_ADDITIONAL_PROVISIONING)) {
            // Add single account to parent event
            ids.add(identityAccount.getAccount());
        } else {
            // Add single identity-account to parent event
            ids.add(identityAccount.getId());
        }
    }


    // Abstract methods

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


}
