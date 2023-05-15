package eu.bcvsolutions.idm.acc.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
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
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;

/**
 * Service for control account management. Account management is supported for
 * {@link SystemEntityType#IDENTITY} only.
 * 
 * @author Vít Švanda
 * @author Peter Štrunc <github.com/peter-strunc>
 * @author Roman Kucera
 *
 */
@Service
public class DefaultAccAccountManagementService extends AbstractAccountManagementService<IdmIdentityDto, AccIdentityAccountDto> implements AccAccountManagementService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(DefaultAccAccountManagementService.class);
	private final AccAccountService accountService;
	private final SysRoleSystemService roleSystemService;
	private final AccIdentityAccountService identityAccountService;
	private final SysRoleSystemAttributeService roleSystemAttributeService;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysSystemMappingService systemMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	@Autowired
	private IdmRoleAssignmentManager roleAssignmentManager;
	private final EntityStateManager entityStateManager;
	@Autowired
	private EntityEventManager entityEventManager;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private SysSystemGroupSystemService systemGroupSystemService;
	@Autowired
	private AccSchemaFormAttributeService schemaFormAttributeService;

	@Autowired
	public DefaultAccAccountManagementService(SysRoleSystemService roleSystemService, AccAccountService accountService,
			AccIdentityAccountService identityAccountService, SysRoleSystemAttributeService roleSystemAttributeService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSystemMappingService systemMappingService, SysSchemaObjectClassService schemaObjectClassService, EntityStateManager entityStateManager) {
		super(entityStateManager, roleSystemService, systemAttributeMappingService, roleSystemAttributeService, systemGroupSystemService, accountService, systemMappingService, schemaFormAttributeService, schemaObjectClassService);
		//
		Assert.notNull(identityAccountService, "Service is required.");
		Assert.notNull(roleSystemService, "Service is required.");
		Assert.notNull(accountService, "Service is required.");
		Assert.notNull(roleSystemAttributeService, "Service is required.");
		Assert.notNull(systemAttributeMappingService, "Service is required.");
		Assert.notNull(systemMappingService, "Service is required.");
		Assert.notNull(schemaObjectClassService, "Service is required.");
		//
		this.roleSystemService = roleSystemService;
		this.accountService = accountService;
		this.identityAccountService = identityAccountService;
		this.roleSystemAttributeService = roleSystemAttributeService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.systemMappingService = systemMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
		this.entityStateManager = entityStateManager;
	}

	/**
	 * {@inheritDoc}
	 * @deprecated @since 13.0.4 use {@link #resolveAccounts(IdmIdentityDto)} instead
	 */
	@Override
	@Deprecated
	public boolean resolveIdentityAccounts(IdmIdentityDto identity) {
		return resolveAccounts(identity);
	}
	
	
	@Override
	public List<UUID> resolveNewIdentityRoles(IdmIdentityDto identity, AbstractRoleAssignmentDto... identityRoles) {
		Assert.notNull(identity, "Identity is required.");

		if (identityRoles == null || identityRoles.length == 0) {
			// No identity-roles ... we don't have anything to do
			return null;
		}

		List<AbstractRoleAssignmentDto> identityRolesList = Lists.newArrayList(identityRoles);
		List<AccIdentityAccountDto> identityAccountsToCreate = Lists.newArrayList();

		// For this account should be executed provisioning
		List<UUID> accounts = Lists.newArrayList();
		
		// Is role valid in this moment
		resolveIdentityAccountForCreate(identity, Lists.newArrayList(), identityRolesList, identityAccountsToCreate,
				Lists.newArrayList(), true, accounts);

		// Create new identity accounts
		identityAccountsToCreate.forEach(identityAccount ->
			// Check if this identity-account already exists, if yes then is his account ID
			// add to accounts (for provisioning).
			createIdentityAccountIfNotExists(accounts, identityAccount));
		return accounts;
	}
	
	@Override
	public  List<UUID>  resolveUpdatedIdentityRoles(IdmIdentityDto identity, AbstractRoleAssignmentDto... identityRoles) {
		Assert.notNull(identity, "Identity is required.");

		if (identityRoles == null || identityRoles.length == 0) {
			// No identity-roles ... we don't have anything to do
			return null;
		}
		List<AbstractRoleAssignmentDto> identityRolesList = Lists.newArrayList(identityRoles);
		// Find identity-accounts for changed identity-roles (using IN predicate)
		List<UUID> identityRoleIds = identityRolesList.stream() //
				.map(AbstractRoleAssignmentDto::getId) //
				.collect(Collectors.toList()); //
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		filter.setIdentityRoleIds(identityRoleIds);
		List<AccIdentityAccountDto> identityAccountList = identityAccountService.find(filter, null).getContent();

		// create / remove accounts
		List<AccIdentityAccountDto> identityAccountsToCreate = new ArrayList<>();
		List<AccIdentityAccountDto> identityAccountsToDelete = new ArrayList<>();

		// Is role valid in this moment
		resolveIdentityAccountForCreate(identity, identityAccountList, identityRolesList, identityAccountsToCreate,
				identityAccountsToDelete, false, null);

		// Is role invalid in this moment
		resolveIdentityAccountForDelete(identityAccountList, identityRolesList, identityAccountsToDelete);

		// For this accounts should be execute a provisioning. We have to execute provisioning for all changed accounts
		// although identity-role was not changed (EAV attributes could have been changed).
		List<UUID> accounts = identityAccountList.stream() //
				.map(AccIdentityAccountDto::getAccount) //
				.distinct() //
				.collect(Collectors.toList());

		// Create new identity accounts
		identityAccountsToCreate.forEach(identityAccount ->
			// Check if this identity-account already exists, if yes then is his account ID
			// add to accounts (for provisioning).
			createIdentityAccountIfNotExists(accounts, identityAccount));

		// Delete invalid identity accounts
		identityAccountsToDelete.forEach(identityAccount -> identityAccountService.deleteById(identityAccount.getId()));
		
		return accounts;
	}


	@Override
	@Transactional
	public List<UUID> deleteIdentityAccount(AbstractRoleAssignmentDto entity) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityRoleId(entity.getId());
		List<UUID> accountIds = Lists.newArrayList();
		
		identityAccountService.find(filter, null).getContent() //
				.forEach(identityAccount -> { //
					accountIds.add(identityAccount.getAccount());
					identityAccountService.delete(identityAccount);
				});
		return accountIds;
	}
	
	@Override
	@Transactional
	public void deleteIdentityAccount(EntityEvent<AbstractRoleAssignmentDto> event) {
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
			List<UUID> accountIds = deleteIdentityAccount(identityRole);
			// We needs accounts which were connected to deleted identity-role in next
			// processor (we want to execute provisioning only for that accounts)
			event.getProperties().put(ACCOUNT_IDS_FOR_DELETED_IDENTITY_ROLE, (Serializable) accountIds);
			return;
		}
		
		// Role is deleted in bulk (e.g. role request) - account management has to be called outside
		// we just mark identity account to be deleted and remove identity role
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityRoleId(identityRole.getId());

		identityAccountService.find(filter, null).getContent() //
				.forEach(identityAccount -> { //
					// Set relation on identity-role to null
					identityAccount.setIdentityRole(null);

					if (bulk) {
						// For bulk create entity state for identity account. 
						IdmEntityStateDto stateDeleted = new IdmEntityStateDto();
						stateDeleted.setSuperOwnerId(identityAccount.getIdentity());
						stateDeleted.setResult(new OperationResultDto.Builder(OperationState.RUNNING)
								.setModel(new DefaultResultModel(CoreResultCode.DELETED)).build());
						entityStateManager.saveState(identityAccount, stateDeleted);
					} else {
						// Noting identity-accounts for delayed delete and account management
						notingIdentityAccountForDelayedAcm(event, identityAccount, IdmAccountDto.IDENTITY_ACCOUNT_FOR_DELAYED_ACM);
					}
					identityAccountService.save(identityAccount);
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


	@Override
	protected boolean isAccountAssignedByAssignment(AccIdentityAccountDto entityAccount, AbstractRoleAssignmentDto roleAssignment) {
		return roleAssignment.getId().equals(entityAccount.getIdentityRole());
	}

	@Override
	protected List<AbstractRoleAssignmentDto> fetchCurrentRoleAssignments(IdmIdentityDto owner) {
		return roleAssignmentManager.findAllByIdentity(owner.getId());
	}

	@Override
	protected Class<AccIdentityAccountDto> getAccountType() {
		return AccIdentityAccountDto.class;
	}

	@Override
	protected List<AccIdentityAccountDto> fetchCurrentlyOwnedAccounts(IdmIdentityDto owner) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setIdentityId(owner.getId());
		return identityAccountService.find(filter, null).getContent();
	}

	@Override
	protected ReadWriteDtoService<AccIdentityAccountDto, AccIdentityAccountFilter> getAccountService() {
		return identityAccountService;
	}
}
