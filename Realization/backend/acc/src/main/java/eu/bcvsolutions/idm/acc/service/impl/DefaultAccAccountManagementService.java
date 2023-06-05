package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.event.IdentityAccountEvent;
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
import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	private final AccIdentityAccountService identityAccountService;

	@Autowired
	private IdmRoleAssignmentManager roleAssignmentManager;
	@Autowired
	private SysSystemGroupSystemService systemGroupSystemService;
	private final LookupService lookupService;


	@Autowired
	public DefaultAccAccountManagementService(SysRoleSystemService roleSystemService, AccAccountService accountService,
			AccIdentityAccountService identityAccountService, SysRoleSystemAttributeService roleSystemAttributeService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSystemMappingService systemMappingService, SysSchemaObjectClassService schemaObjectClassService, EntityStateManager entityStateManager, EntityEventManager entityEventManager,
			SysSystemGroupSystemService systemGroupSystemService, AccSchemaFormAttributeService schemaFormAttributeService, LookupService lookupService) {
		super(entityStateManager, roleSystemService, systemAttributeMappingService, roleSystemAttributeService, systemGroupSystemService, accountService,
				systemMappingService, schemaFormAttributeService, schemaObjectClassService, entityEventManager);
		//
		Assert.notNull(identityAccountService, "Service is required.");
		Assert.notNull(roleSystemService, "Service is required.");
		Assert.notNull(accountService, "Service is required.");
		Assert.notNull(roleSystemAttributeService, "Service is required.");
		Assert.notNull(systemAttributeMappingService, "Service is required.");
		Assert.notNull(systemMappingService, "Service is required.");
		Assert.notNull(schemaObjectClassService, "Service is required.");
		//
		this.lookupService = lookupService;
		this.identityAccountService = identityAccountService;
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
		return resolveNewRoleAssignments(identity, identityRoles);
	}

	@Override
	public  List<UUID>  resolveUpdatedIdentityRoles(IdmIdentityDto identity, AbstractRoleAssignmentDto... identityRoles) {
		return resolveUpdatedRoleAssignments(identity, identityRoles);
	}

	@Override
	@Transactional
	public List<UUID> deleteIdentityAccount(AbstractRoleAssignmentDto entity) {
		return deleteEntityAccounts(entity);
	}
	
	@Override
	@Transactional
	public void deleteIdentityAccount(EntityEvent<AbstractRoleAssignmentDto> event) {
		deleteEntityAccount(event);
	}

	@Override
	protected void updateRoleSystemRelationIfNeeded(AccIdentityAccountDto existsIdentityAccount, SysRoleSystemDto roleSystem) {
		if(existsIdentityAccount.getRoleSystem() == null) {
			// IdentityAccount already exist, but doesn't have relation on RoleSystem. This
			// could happen if system mapping was deleted and recreated or if was role use
			// as sync default role, but without mapping on this system.

			// We have to create missing relation, so we will set and save RoleSystem.
			existsIdentityAccount.setRoleSystem(roleSystem.getId());
			getAccountService().save(existsIdentityAccount);
		}
	}

	@Override
	protected void shouldAccountBeDeleted(List<AccIdentityAccountDto> identityAccountList, List<AbstractRoleAssignmentDto> roleAssignments, List<AccIdentityAccountDto> identityAccountsToDelete) {
			// Search IdentityAccounts to delete
		roleAssignments.stream().filter(identityRole -> !identityRole.isValid())
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
		roleAssignments.stream().filter(ValidableEntity::isValid).forEach(identityRole ->
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

	@Override
	protected boolean shouldAccountBeProvisioned(AccIdentityAccountDto entityAccount, SysRoleSystemDto roleSystem) {
		SysRoleSystemDto roleSystemFromIdentityAccount = entityAccount.getRoleSystem() != null ?
				lookupService.lookupEmbeddedDto(entityAccount, AccIdentityAccount_.roleSystem) : null;
		return roleSystemFromIdentityAccount != null
				&& roleSystem.getSystemMapping().equals(roleSystemFromIdentityAccount.getSystemMapping());
	}

	@Override
	protected Stream<AccIdentityAccountDto> getOwnerAccountsOnSystem(IdmIdentityDto identity, UUID system) {
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setSystemId(system);
		identityAccountFilter.setIdentityId(identity.getId());
		return getAccountService().find(identityAccountFilter, null).stream();
	}

	@Override
	protected AccAccountDto getAccountForEntityAccount(AccIdentityAccountDto sameAccount) {
		return DtoUtils.getEmbedded(sameAccount, AccIdentityAccount_.account.getName(), AccAccountDto.class);
	}

	@Override
	protected boolean isAccountAssignedByAssignment(AccIdentityAccountDto entityAccount, AbstractRoleAssignmentDto roleAssignment) {
		return roleAssignment.getId().equals(entityAccount.getIdentityRole());
	}

	@Override
	protected IdmIdentityDto getOwner(AbstractRoleAssignmentDto roleAssignment) {
		return roleAssignmentManager.getServiceForAssignment(roleAssignment).getRelatedIdentity(roleAssignment);
	}

	@Override
	protected boolean haveSameSystemMapping(AccIdentityAccountDto account1, AccIdentityAccountDto account2) {
		final AccAccountDto acc1 = DtoUtils.getEmbedded(account1, AccIdentityAccount_.account, AccAccountDto.class, null);
		final AccAccountDto acc2 = DtoUtils.getEmbedded(account2, AccIdentityAccount_.account, AccAccountDto.class, null);
		if (acc1 == null || acc2 == null) {
			return false;
		}
		return Objects.equals(acc1.getSystemMapping(), acc2.getSystemMapping());
	}

	@Override
	protected Stream<AccIdentityAccountDto> getEntityAccountsForAccount(AccAccountDto accountDto) {
		AccIdentityAccountFilter accountIdentityAccountFilter = new AccIdentityAccountFilter();
		accountIdentityAccountFilter.setAccountId(accountDto.getId());
		return getAccountService().find(accountIdentityAccountFilter, null).stream();
	}

	@Override
	protected AccIdentityAccountDto getExistingSimilarAccount(AccIdentityAccountDto entityAccount) {
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setIdentityId(entityAccount.getIdentity());
		identityAccountFilter.setIdentityRoleId(entityAccount.getIdentityRole());
		identityAccountFilter.setAccountId(entityAccount.getAccount());
		identityAccountFilter.setRoleSystemId(entityAccount.getRoleSystem());
		return identityAccountService.find(identityAccountFilter, PageRequest.of(0, 1)).stream().findFirst().orElse(null);
	}

	@Override
	protected List<AccIdentityAccountDto> getAccountsForRoleAssignments(IdmIdentityDto owner, List<AbstractRoleAssignmentDto> identityRolesList) {
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		if (owner != null) {
			filter.setIdentityId(owner.getId());
		}
		filter.setIdentityRoleIds(identityRolesList.stream().map(AbstractRoleAssignmentDto::getId).collect(Collectors.toList()));
		// Needs to be wrapped because it is modified in the predecessor method.
		return new ArrayList<>(identityAccountService.find(filter, null).getContent());
	}

	@Override
	protected AccIdentityAccountDto createAccountDto(IdmIdentityDto owner, SysRoleSystemDto roleSystem, AbstractRoleAssignmentDto identityRole, AccAccountDto account) {

		AccIdentityAccountDto identityAccount = new AccIdentityAccountDto();
		identityAccount.setAccount(account.getId());
		identityAccount.setIdentity(owner.getId());
		identityAccount.setIdentityRole(identityRole.getId());
		identityAccount.setRoleSystem(roleSystem.getId());
		identityAccount.setOwnership(true);
		identityAccount.getEmbedded().put(AccIdentityAccount_.account.getName(), account);
		return identityAccount;
	}

	@Override
	protected boolean isSameAccount(AccIdentityAccountDto entityAccount, AccAccountDto account, SysRoleSystemDto roleSystem, AbstractRoleAssignmentDto roleAssignment) {
		// Identity accounts have link to both role-system and identity role, so we have to check both.
		return super.isSameAccount(entityAccount, account, roleSystem, roleAssignment) &&
				(roleAssignment == null || roleAssignment.getId().equals(entityAccount.getIdentityRole())) &&
				(roleSystem == null || roleSystem.getId().equals(entityAccount.getRoleSystem()));
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

	@Override
	public AccIdentityAccountDto publish(EventType eventType, UUID accountId, Map<String, Serializable> eventProperties) {
		AccIdentityAccountDto identityAccountDto = identityAccountService.get(accountId);
		if (identityAccountDto != null) {
			final IdentityAccountEvent identityAccountEvent = new IdentityAccountEvent(IdentityAccountEvent.IdentityAccountEventType.DELETE, identityAccountDto, eventProperties);
			return identityAccountService.publish(identityAccountEvent).getContent();
		}
		return null;
	}

	@Override
	protected void prepareAccountForDelete(AccIdentityAccountDto account) {
		// clear relation to identity role
		account.setIdentityRole(null);
	}
}
