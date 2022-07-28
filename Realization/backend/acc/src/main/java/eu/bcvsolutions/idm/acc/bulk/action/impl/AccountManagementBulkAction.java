package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Executes recalculation and provisioning of account.
 * 
 * @author Tomáš Doischer
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(AccountManagementBulkAction.NAME)
@Description("Executes provisioning of account")
public class AccountManagementBulkAction extends AbstractBulkAction<AccAccountDto, AccAccountFilter> {
	
	public static final String NAME = "acc-account-management-bulk-action";

	@Autowired
	private AccAccountService accountService;
	@Autowired
	private ProvisioningService provisioningService;
	@Autowired
	private LookupService lookupService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private AccAccountManagementService accountManagementService;

	@Override
	protected OperationResult processDto(AccAccountDto dto) {
		AbstractDto entity = lookupService.lookupDto(dto.getTargetEntityType(), dto.getTargetEntityId());
		provisioningService.doProvisioning(dto, entity);

		return new OperationResult(OperationState.EXECUTED);
		
//		// check if the entity type is identity since nothing else can be recalculated
//		if (dto.getEntityType() == SystemEntityType.IDENTITY) {
//			AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
//			identityAccountFilter.setAccountId(dto.getId());
//			List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountFilter, null).getContent();
//			
//			List<IdmIdentityRoleDto> allIdentityRoles = identityAccounts
//					.stream()
//					.map(ia -> DtoUtils.getEmbedded(ia, AccIdentityAccount_.identityRole, IdmIdentityRoleDto.class, null))
//					.collect(Collectors.toList());
//
//			allIdentityRoles.forEach(identityRole -> {
//				IdmIdentityDto identity = getEmbeddedIdentity(identityRole);
//				try {
//					// Execute account management for identity and exists assigned role
//					List<UUID> accountIds = accountManagementService.resolveUpdatedIdentityRoles(identity, identityRole);
//					// Execute provisioning
//					accountIds.forEach(accountId -> {
//						AccAccountDto account = accountService.get(accountId);
//						if (account != null) { // Account could be null (was deleted).
////							LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(), account.getUid());
//							provisioningService.doProvisioning(account, identity);
//						}
//					});
////					successIdentityRoles.add(identityRole);
//				} catch (Exception ex) {
////					LOG.error("Call acm and provisioning for assigned role [{}], identity [{}] failed",
////							identityRole.getId(), identity.getUsername(), ex);
//					//
////					failedIdentityRoles.put(identityRole, ex);
//				}
//			});
//			
////			IdmIdentityDto entity = identityService.get(dto.getTargetEntityId());
////			//
////			IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
////			identityRoleFilter.setIdentityId(entity.getId());
////			List<IdmIdentityRoleDto> identityRoles = identityRoleService
////					.find(identityRoleFilter,
////							PageRequest.of(0, Integer.MAX_VALUE, Sort.by(IdmIdentityRole_.created.getName())))
////					.getContent();
////
////			AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
////			identityAccountFilter.setIdentityId(entity.getId());
////			identityAccountFilter.setSystemId(dto.getSystem()); //TODO this may be more straightforward?
////			List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(identityAccountFilter, null)
////					.getContent();
////			List<IdmIdentityRoleDto> identityRolesForSystem = Lists.newArrayList();
////			// Filtering only identity-roles for this system.
////			identityAccounts.forEach(identityAccount -> {
////				identityRolesForSystem.addAll(identityRoles.stream()
////						.filter(identityRole -> identityRole.getId().equals(identityAccount.getIdentityRole()))
////						.collect(Collectors.toList())
////				);
////			});
////			
////			identityRolesForSystem.forEach(ir -> accountManagementService.resolveUpdatedIdentityRoles(entity, ir));
////			provisioningService.doProvisioning(dto, entity);
//			//
//			return new OperationResult(OperationState.EXECUTED);
//		}
//		
//		AbstractDto entity = lookupService.lookupDto(dto.getTargetEntityType(), dto.getTargetEntityId());
//		provisioningService.doProvisioning(dto, entity);
//
//		return new OperationResult(OperationState.EXECUTED);
	}
	
	/**
	 * Identity is required in embedded.
	 * 
	 * @param identityRole
	 * @return
	 */
	private IdmIdentityDto getEmbeddedIdentity(IdmIdentityRoleDto identityRole) {
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract,
				IdmIdentityContractDto.class);
		//
		return getLookupService().lookupEmbeddedDto(contract, IdmIdentityContract_.identity);
	}
	
	@Override
	public ReadWriteDtoService<AccAccountDto, AccAccountFilter> getService() {
		return accountService;
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(AccGroupPermission.ACCOUNT_READ, AccGroupPermission.ACCOUNT_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1600;
	}

}
