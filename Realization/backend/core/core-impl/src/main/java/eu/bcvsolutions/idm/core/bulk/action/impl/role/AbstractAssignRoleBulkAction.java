package eu.bcvsolutions.idm.core.bulk.action.impl.role;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.model.event.processor.role.RoleRequestApprovalProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.ContractBasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Bulk operation for assign role to identity.
 *
 * @author Radek Tomiška
 * @since 11.2.0
 */
public abstract class AbstractAssignRoleBulkAction<DTO extends AbstractDto, F extends BaseFilter> 
		extends AbstractBulkAction<DTO, F> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAssignRoleBulkAction.class);
	//
	public static final String PROPERTY_ROLE = "role";
	public static final String PROPERTY_IDENTITY = "identity";
	public static final String PROPERTY_PRIME_CONTRACT = "primeContract";
	public static final String PROPERTY_VALID_TILL = "validTill";
	public static final String PROPERTY_VALID_FROM = "validFrom";
	public static final String PROPERTY_APPROVE = "approve";
	//
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private SecurityService securityService;

	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		List<IdmFormAttributeDto> formAttributes = super.getFormAttributes();
		formAttributes.add(getIdentityAttribute());
		formAttributes.add(getApproveAttribute());
		formAttributes.add(getPrimeContractAttribute());
		formAttributes.add(getValidFromAttribute());
		formAttributes.add(getValidTillAttribute());
		return formAttributes;
	}

	protected void assignRoles(List<UUID> identityIds, List<UUID> roleIds) {
		for (UUID identityId : identityIds) {
			List<IdmIdentityContractDto> contracts = new ArrayList<>();
			//
			if (isPrimeContract()) {
				IdmIdentityContractDto contract = identityContractService.getPrimeValidContract(identityId);
				//
				if (contract != null) {
					contracts.add(contract);
				}
			} else {
				IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
				filter.setIdentity(identityId);
				filter.setValidNowOrInFuture(Boolean.TRUE);
				//
				contracts.addAll(identityContractService.find(filter, null).getContent());
			}
			//
			// nothing to process
			if (contracts.isEmpty()) {
				continue;
			}
			//
			boolean approve = isApprove();
			LocalDate validFrom = this.getValidFrom();
			LocalDate validTill = this.getValidTill();
			//
			List<IdmConceptRoleRequestDto> concepts = new ArrayList<>();
			for (IdmIdentityContractDto contract : contracts) {
				if (!checkPermissionForContract(contract)) {
					LOG.warn("Insufficient permissions for asign role for contract [{}]", contract.getId());
					//
					logItemProcessed(
							contract,
							new OperationResult
								.Builder(OperationState.NOT_EXECUTED)
								.setModel(
									new DefaultResultModel(
											CoreResultCode.BULK_ACTION_NOT_AUTHORIZED_ASSING_ROLE_FOR_CONTRACT,
											ImmutableMap.of("contractId", contract.getId())
									)
								)
								.build()
					);
					//
					continue;
				}
				//
				for (UUID roleId : roleIds) {
					IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
					concept.setRole(roleId);
					concept.setIdentityContract(contract.getId());
					concept.getEmbedded().put(IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, contract);
					concept.setOperation(ConceptRoleRequestOperation.ADD);
					// filled automatically - prevent to provision future valid roles by default
					concept.setValidFrom(validFrom == null ? contract.getValidFrom() : validFrom);
					// #1887: its not filled automatically from contract (validity will be controlled by contract validity dynamically)
					concept.setValidTill(validTill);
					concepts.add(concept);
				}
			}
			// if exists at least one concept create and starts request
			if (!concepts.isEmpty()) {
				// create request
				IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
				roleRequest.setApplicant(identityId);
				roleRequest.setRequestedByType(RoleRequestedByType.MANUALLY);
				roleRequest.setLog("Request was created by bulk action.");
				roleRequest.setExecuteImmediately(!approve); // if set approve, don't execute immediately
				
				roleRequest = roleRequestService.save(roleRequest, IdmBasePermission.CREATE);
				//
				List<IdmIdentityContractDto> processedContracts = new ArrayList<>(concepts.size());
				for (IdmConceptRoleRequestDto concept : concepts) {
					processedContracts.add(DtoUtils.getEmbedded(concept, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT));
					concept.setRoleRequest(roleRequest.getId());
					concept = conceptRoleRequestService.save(concept, IdmBasePermission.CREATE);
				}
				//
				Map<String, Serializable> properties = new HashMap<>();
				properties.put(RoleRequestApprovalProcessor.CHECK_RIGHT_PROPERTY, Boolean.TRUE);
				RoleRequestEvent event = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest, properties);
				event.setPriority(PriorityType.HIGH);
				IdmRoleRequestDto request = roleRequestService.startRequestInternal(event);
				processedContracts.forEach(contract -> {
					logItemProcessed(
							contract, 
							new OperationResult.Builder(request.getState() == RoleRequestState.EXECUTED ? OperationState.EXECUTED : OperationState.CREATED).build()
					); 
				});
			}
		}
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> permissions = super.getAuthorities();
		//
		permissions.add(CoreGroupPermission.IDENTITYCONTRACT_READ);
		//
		return permissions;
	}

	private boolean checkPermissionForContract(IdmIdentityContractDto contract) {
		Set<String> permissions = identityContractService.getPermissions(contract);
		//
		return PermissionUtils.hasPermission(permissions, IdmBasePermission.READ) 
				&& PermissionUtils.hasAnyPermission( // OR -> CANBEREQUESTED or CHANGEPERMISSION for both sides from role guarantee or contract manager
						permissions, 
						ContractBasePermission.CHANGEPERMISSION,
						ContractBasePermission.CANBEREQUESTED);
	}
	
	protected List<UUID> getRoles() {
		return getParameterConverter().toUuids(getProperties(), PROPERTY_ROLE);
	}

	protected List<UUID> getIdentities() {
		return getParameterConverter().toUuids(getProperties(), PROPERTY_IDENTITY);
	}
	
	protected boolean isApprove() {
		return getParameterConverter().toBoolean(getProperties(), PROPERTY_APPROVE, true);
	}

	protected boolean isPrimeContract() {
		return getParameterConverter().toBoolean(getProperties(), PROPERTY_PRIME_CONTRACT, true);
	}

	protected LocalDate getValidFrom() {
		return getParameterConverter().toLocalDate(getProperties(), PROPERTY_VALID_FROM);
	}
	
	protected LocalDate getValidTill() {
		return getParameterConverter().toLocalDate(getProperties(), PROPERTY_VALID_TILL);
	}
	
	protected IdmFormAttributeDto getApproveAttribute() {
		IdmFormAttributeDto approve = new IdmFormAttributeDto(
				PROPERTY_APPROVE, 
				PROPERTY_APPROVE, 
				PersistentType.BOOLEAN);
		approve.setDefaultValue(Boolean.TRUE.toString());
		approve.setReadonly(!securityService.hasAnyAuthority(CoreGroupPermission.ROLE_REQUEST_EXECUTE));
		return approve;
	}
	
	protected IdmFormAttributeDto getRoleAttribute() {
		IdmFormAttributeDto attribute = new IdmFormAttributeDto(
				PROPERTY_ROLE, 
				PROPERTY_ROLE, 
				PersistentType.UUID);
		attribute.setFaceType(BaseFaceType.ROLE_CAN_BE_REQUESTED_SELECT);
		attribute.setMultiple(true);
		attribute.setRequired(true);
		//
		return attribute;
	}
	
	protected IdmFormAttributeDto getIdentityAttribute() {
		IdmFormAttributeDto roles = new IdmFormAttributeDto(
				PROPERTY_IDENTITY, 
				PROPERTY_IDENTITY, 
				PersistentType.UUID);
		roles.setFaceType(BaseFaceType.IDENTITY_READ_SELECT); // ~ READ permission - the same as bulk action from identity side
		roles.setMultiple(true);
		roles.setRequired(true);
		return roles;
	}

	protected IdmFormAttributeDto getPrimeContractAttribute() {
		IdmFormAttributeDto primaryContract = new IdmFormAttributeDto(
				PROPERTY_PRIME_CONTRACT, 
				PROPERTY_PRIME_CONTRACT, 
				PersistentType.BOOLEAN);
		primaryContract.setDefaultValue(Boolean.TRUE.toString());
		return primaryContract;
	}
	
	protected IdmFormAttributeDto getValidTillAttribute() {
		IdmFormAttributeDto validTill = new IdmFormAttributeDto(
				PROPERTY_VALID_TILL, 
				PROPERTY_VALID_TILL, 
				PersistentType.DATE);
		return validTill;
	}
	
	protected IdmFormAttributeDto getValidFromAttribute() {
		IdmFormAttributeDto validFrom = new IdmFormAttributeDto(
				PROPERTY_VALID_FROM, 
				PROPERTY_VALID_FROM, 
				PersistentType.DATE);
		return validFrom;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 300;
	}
}
