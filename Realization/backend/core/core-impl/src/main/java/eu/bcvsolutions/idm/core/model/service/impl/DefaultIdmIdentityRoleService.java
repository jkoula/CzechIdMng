package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRequestIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.adapter.DtoAdapter;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.AbstractRoleAssignment_;
import eu.bcvsolutions.idm.core.model.event.AbstractRoleAssignmentEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.audit.service.SiemLoggerManager;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterManager;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.repository.IdmAutomaticRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with identity roles.
 * 
 * @author svanda
 * @author Radek Tomiška
 * @author Ondrej Kopr
 * @author Peter Štrunc <github.com/peter-strunc>
 *
 */
public class DefaultIdmIdentityRoleService 
		extends AbstractRoleAssignmentService<IdmIdentityRoleDto, IdmIdentityRole, IdmIdentityRoleFilter>
		implements IdmIdentityRoleService {

	private final IdmIdentityRoleRepository repository;
	//
	private final LookupService lookupService;

	@Autowired
	public DefaultIdmIdentityRoleService(
			IdmIdentityRoleRepository repository,
			FormService formService,
			EntityEventManager entityEventManager, IdmRoleService roleService, LookupService lookupService, IdmAutomaticRoleRepository automaticRoleRepository, FilterManager filterManager) {
		super(repository, entityEventManager, formService, roleService, automaticRoleRepository, lookupService, filterManager);
		//
		this.repository = repository;
		this.lookupService = lookupService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYROLE, getEntityClass());
	}

	@Override
	public IdmIdentityRoleDto getDuplicated(IdmIdentityRoleDto one, IdmIdentityRoleDto two, Boolean skipSubdefinition) {
		Assert.notNull(one, "The first assinged role to compare is required.");
		Assert.notNull(two, "The second assinged role to compare is required.");
		//
		if (!one.getIdentityContract().equals(two.getIdentityContract())) {
			return null;
		}
		return super.getDuplicated(one, two, skipSubdefinition);
	}

	@Override
	public Class<IdmIdentityRoleDto> getType() {
		return IdmIdentityRoleDto.class;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityRole> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// quick - by identity's username
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			text = text.toLowerCase();
			predicates.add(
					builder.like(
							builder.lower(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.username)),
							"%" + text + "%")
					);
		}
		List<UUID> identities = filter.getIdentities();
		if (!identities.isEmpty()) {
			predicates.add(
					root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity).get(IdmIdentity_.id).in(identities) 
			);
		}
		//
		Boolean valid = filter.getValid();
		if (valid != null) {
			// Only valid identity-role include check on contract validity too
			if (valid) {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.and(
								RepositoryUtils.getValidPredicate(root, builder, today),
								RepositoryUtils.getValidPredicate(root.get(IdmIdentityRole_.identityContract), builder, today),
								builder.equal(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.disabled), Boolean.FALSE)
						));
			}
			// Only invalid identity-role
			if (!valid) {
				final LocalDate today = LocalDate.now();
				predicates.add(
						builder.or(
								builder.not(RepositoryUtils.getValidPredicate(root, builder, today)),
								builder.not(RepositoryUtils.getValidPredicate(root.get(IdmIdentityRole_.identityContract), builder, today)),
								builder.equal(root.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.disabled), Boolean.TRUE)
								)
						);
			}
		}
		//
		UUID identityContractId = filter.getIdentityContractId();
		if (identityContractId != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.identityContract).get(AbstractEntity_.id), 
					identityContractId)
					);
		}
		//
		UUID contractPositionId = filter.getContractPositionId();
		if (contractPositionId != null) {
			predicates.add(builder.equal(
					root.get(IdmIdentityRole_.contractPosition).get(AbstractEntity_.id), 
					contractPositionId)
					);
		}

		UUID directRoleId = filter.getDirectRoleId();
		if (directRoleId != null) {
			predicates.add(builder.equal(root.get(IdmIdentityRole_.directRole).get(AbstractEntity_.id), directRoleId));
		}
		// is direct role
		Boolean directRole = filter.getDirectRole();
		if (directRole != null) {
			if (directRole) {
				predicates.add(builder.isNull(root.get(IdmIdentityRole_.directRole)));
			} else {
				predicates.add(builder.isNotNull(root.get("directRole")));
			}
		}
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityRoleDto> findAllByIdentity(UUID identityId) {
		return toDtos(repository.findAllByIdentityContract_Identity_Id(identityId, getDefaultSort()), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityRoleDto> findAllByContract(UUID identityContractId) {
		Assert.notNull(identityContractId, "contract identifier is required.");
		//
		return toDtos(repository.findAllByIdentityContract_Id(identityContractId, getDefaultSort()), false);
	}
	
	@Override
	public List<IdmIdentityRoleDto> findAllByContractPosition(UUID contractPositionId) {
		Assert.notNull(contractPositionId, "contract position identifier is required.");
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setContractPositionId(contractPositionId);
		//
		return find(filter, null).getContent();
	}

	@Override
	public Page<IdmIdentityRoleDto> findValidRoles(UUID identityId, Pageable pageable) {
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setValid(Boolean.TRUE);
		identityRoleFilter.setIdentityId(identityId);
		//
		return this.find(identityRoleFilter, pageable);
	}

	/**
	 * Get valid till for {@link IdmIdentityRoleDto}. Valid till could be set from contract if
	 * date is after valid till from contract.
	 *
	 * @param identityRole
	 * @return
	 */
	@Override
	protected LocalDate getDateForValidTill(IdmIdentityRoleDto identityRole) {
		LocalDate validTill = identityRole.getValidTill();
		IdmIdentityContractDto identityContractDto = DtoUtils.getEmbedded(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT, IdmIdentityContractDto.class, null);
		LocalDate validTillContract = identityContractDto.getValidTill();

		if (validTill != null && validTillContract != null && validTillContract.isAfter(validTill)) {
			return validTill;
		}

		if (validTillContract == null && validTill != null) {
			return validTill;
		}

		return validTillContract;
	}


	/**
	 * Method provides specific logic for role assignment siem logging.
	 * 
	 */
	@Override
	protected void siemLog(EntityEvent<IdmIdentityRoleDto> event, String status, String detail) {
		if (event == null) {
			return;
		}
		IdmIdentityRoleDto dto = event.getContent();
		String operationType = event.getType().name();
		String action = siemLoggerManager.buildAction(SiemLoggerManager.ROLE_ASSIGNMENT_LEVEL_KEY, operationType);
		if(siemLoggerManager.skipLogging(action)) {
			return;
		}
		IdmIdentityContractDto contractDto = lookupService.lookupEmbeddedDto(dto, IdmIdentityRole_.identityContract.getName());
		IdmRoleDto subjectDto = lookupService.lookupEmbeddedDto(dto, AbstractRoleAssignment_.role.getName());
		IdmIdentityDto targetDto = lookupService.lookupEmbeddedDto(contractDto, IdmIdentityContract_.identity.getName());		
		String transactionUuid = Objects.toString(dto.getTransactionId(),"");
		siemLog(action, status, targetDto, subjectDto, transactionUuid, detail);
	}

	/**
	 * Default sort by role's name
	 * 
	 * @returnx
	 */
	private Sort getDefaultSort() {
		return Sort.by(AbstractRoleAssignment_.role.getName() + "." + IdmRole_.code.getName());
	}

	@Override
	public IdmIdentityRoleFilter getFilter() {
		return new IdmIdentityRoleFilter();
	}

	@Override
	public List<IdmIdentityRoleDto> findAllByOwnerId(UUID ownerUuid) {
		return findAllByContract(ownerUuid);
	}

	@Override
	public IdmIdentityDto getRelatedIdentity(IdmIdentityRoleDto roleAssignment) {
		IdmIdentityContractDto contract = DtoUtils.getEmbedded(roleAssignment, IdmIdentityRole_.identityContract,
				IdmIdentityContractDto.class);
		//
		return lookupService.lookupEmbeddedDto(contract, IdmIdentityContract_.identity);
	}

	@Override
	public AbstractRoleAssignmentEvent<IdmIdentityRoleDto> getEventForAssignment(IdmIdentityRoleDto assignment, AbstractRoleAssignmentEvent.RoleAssignmentEventType eventType, String... flags) {
		@SuppressWarnings("deprecation") IdentityRoleEvent event = new IdentityRoleEvent(eventType, assignment, setupFlags(flags));
		event.setPriority(PriorityType.IMMEDIATE);
		return event;
	}

	@Override
	public <F2 extends BaseFilter> DtoAdapter<IdmIdentityRoleDto, IdmRequestIdentityRoleDto> getAdapter(F2 originalFilter) {
		IdmRequestIdentityRoleFilter translatedFilter = modelMapper.map(originalFilter, IdmRequestIdentityRoleFilter.class);
		return new IdentityRoleConceptCompilingAdapter(translatedFilter, /*TODO*/null, applicationContext.getBean(IdmConceptRoleRequestService.class), this);
	}

	@Override
	public Class<?> getOwnerType() {
		return IdmIdentityContractDto.class;
	}
}
