package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.bcvsolutions.idm.core.api.dto.AbstractRoleAssignmentDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleAssignmentManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.ResultModels;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk operation for evaluate the account management on the all identities of
 * given role.
 * 
 * @author svandav
 *
 */

@Enabled(AccModuleDescriptor.MODULE_ID)
@Component("roleSaveBulkAction")
@Description("Bulk operation to evaluate the account management for all identities of given role.")
public class RoleAccountManagementBulkAction extends AbstractBulkAction<IdmRoleDto, IdmRoleFilter> {

	private static final Logger LOG = LoggerFactory.getLogger(RoleAccountManagementBulkAction.class);
	public static final String NAME = "role-acm-bulk-action";

	@Autowired
	private IdmRoleService roleService;
	@Autowired
	private IdmRoleAssignmentManager roleAssignmentManager;
	@Autowired 
	private AccAccountManagementService accountManagementService;
	@Autowired 
	private ProvisioningService provisioningService;
	@Autowired 
	private AccAccountService accountService;

	@Override
	protected OperationResult processDto(IdmRoleDto dto) {
		Assert.notNull(dto, "Role is required!");
		Assert.notNull(dto.getId(), "Id of role is required!");
		
		List<AbstractRoleAssignmentDto> successIdentityRoles = Lists.newArrayList();
		Map<AbstractRoleAssignmentDto, Exception> failedIdentityRoles = Maps.newLinkedHashMap();

		IdmRequestIdentityRoleFilter identityRoleFilter = new IdmRequestIdentityRoleFilter();
		identityRoleFilter.setRoleId(dto.getId());

		// Load all identity roles for this roleId.
		// Without check on IdentityRole UPDATE permissions. This operation is
		// controlled by UPDATE right on this role!
		final List<AbstractRoleAssignmentDto> allRoleAssignments = roleAssignmentManager.find(identityRoleFilter, null, (assignment, service) -> {
			IdmIdentityDto identity = getEmbeddedIdentity(assignment);

			try {
				// Execute account management for identity and exists assigned role
				List<UUID> accountIds = accountManagementService.resolveUpdatedIdentityRoles(identity, assignment);
				// Execute provisioning
				accountIds.forEach(accountId -> {
					AccAccountDto account = accountService.get(accountId);
					if (account != null) { // Account could be null (was deleted).
						LOG.debug("Call provisioning for identity [{}] and account [{}]", identity.getUsername(), account.getUid());
						provisioningService.doProvisioning(account, identity);
					}
				});
				successIdentityRoles.add(assignment);
			} catch (Exception ex) {
				LOG.error("Call acm and provisioning for assigned role [{}], identity [{}] failed", assignment.getId(), identity.getUsername(), ex);
				//
				failedIdentityRoles.put(assignment, ex);
			}
		});

		OperationResult operationResult = new OperationResult(OperationState.EXECUTED);
		StringBuilder message = new StringBuilder();
		operationResult = handleFailedRoleAssignments(dto, failedIdentityRoles, operationResult, message, allRoleAssignments);
		handleSuccessfullRoleAssignments(dto, successIdentityRoles, message, allRoleAssignments);

		operationResult.setCause(message.toString());
		return operationResult;
	}

	private void handleSuccessfullRoleAssignments(IdmRoleDto dto, List<AbstractRoleAssignmentDto> successIdentityRoles, StringBuilder message, List<AbstractRoleAssignmentDto> allRoleAssignments) {
		if (!successIdentityRoles.isEmpty()) {
			message.append('\n');
			message.append('\n');
			message.append(MessageFormat.format(
					"For the role [{0}], [{1}] of role assignments were call acm and provisioning [{2}]. Assigned role UUIDs:",
					dto.getCode(), allRoleAssignments.size(), successIdentityRoles.size()));
			successIdentityRoles.forEach(identityRole -> {
				message.append('\n');
				message.append(MessageFormat.format("[{0}], identity [{1}]",
						identityRole.getId(), getEmbeddedIdentity(identityRole).getUsername()));
			});
		}
	}

	private OperationResult handleFailedRoleAssignments(IdmRoleDto dto, Map<AbstractRoleAssignmentDto, Exception> failedIdentityRoles, OperationResult operationResult, StringBuilder message,
			List<AbstractRoleAssignmentDto> allRoleAssignments) {
		if (!failedIdentityRoles.isEmpty()) {
			operationResult = new OperationResult(OperationState.EXCEPTION);
			//
			message.append(MessageFormat.format(
					"For the role [{0}], [{1}] of identity roles were FAILED acm or provisioning [{2}]. Assigned role UUIDs:\n",
					dto.getCode(), allRoleAssignments.size(), failedIdentityRoles.size()));
			failedIdentityRoles.forEach((identityRole, ex) -> {
				message.append('\n');
				message.append(MessageFormat.format("[{0}], identity [{1}], exception:\n{2}",
						identityRole.getId(), getEmbeddedIdentity(identityRole).getUsername(), ex));
				message.append('\n');
			});
		}
		return operationResult;
	}

	@Override
	public ResultModels prevalidate() {
		IdmBulkActionDto action = getAction();
		List<UUID> entities = getEntities(action, new StringBuilder());
		ResultModels result = new ResultModels();

		Map<ResultModel, Long> models = new HashMap<>();
		entities.forEach(roleId -> {
			IdmRequestIdentityRoleFilter identityRoleFilter = new IdmRequestIdentityRoleFilter();
			identityRoleFilter.setRoleId(roleId);
			IdmRoleDto role = getService().get(roleId);

			long count = roleAssignmentManager.find(identityRoleFilter, PageRequest.of(0, 1)).getTotalElements();
			if (count > 0) {
				models.put(new DefaultResultModel(AccResultCode.ROLE_ACM_BULK_ACTION_NUMBER_OF_IDENTITIES,
						ImmutableMap.of("role", role.getCode(), "count", count)), count);
			}
		});

		boolean someIdentitiesFound = models.values() //
				.stream() //
				.filter(count -> count > 0) //
				.findFirst() //
				.isPresent(); //

		if (!someIdentitiesFound) {
			result.addInfo(new DefaultResultModel(AccResultCode.ROLE_ACM_BULK_ACTION_NONE_IDENTITIES));
		} else {
			// Sort by count
			List<Entry<ResultModel, Long>> collect = models //
					.entrySet() //
					.stream() //
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())) //
					.limit(5) //
					.collect(Collectors.toList()); //
			collect.forEach(entry -> {
				result.addInfo(entry.getKey());
			});
		}

		return result;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLE_READ, CoreGroupPermission.ROLE_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}

	@Override
	public ReadWriteDtoService<IdmRoleDto, IdmRoleFilter> getService() {
		return roleService;
	}
	
	/**
	 * Identity is required in embedded.
	 * 
	 * @param roleAssignment
	 * @return
	 */
	private IdmIdentityDto getEmbeddedIdentity(AbstractRoleAssignmentDto roleAssignment) {
		return roleAssignmentManager.getServiceForAssignment(roleAssignment).getRelatedIdentity(roleAssignment);
	}
}
