package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.domain.RoleConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorityChange;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType_;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorityChangeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Operations with IdmIdentity
 * - supports {@link IdentityEvent}
 * 
 * @author Radek Tomiška
 * @see IdentityProcessor
 *
 */
public class DefaultIdmIdentityService
		extends AbstractFormableService<IdmIdentityDto, IdmIdentity, IdmIdentityFilter> 
		implements IdmIdentityService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityService.class);

	private final IdmIdentityRepository repository;
	private final IdmRoleService roleService;
	private final IdmAuthorityChangeRepository authChangeRepository;
	private final EntityEventManager entityEventManager;
	private final RoleConfiguration roleConfiguration;
	private final IdmIdentityContractService identityContractService;
	
	@Autowired
	public DefaultIdmIdentityService(
			IdmIdentityRepository repository,
			FormService formService,
			IdmRoleService roleService,
			EntityEventManager entityEventManager,
			IdmAuthorityChangeRepository authChangeRepository,
			RoleConfiguration roleConfiguration,
			IdmIdentityContractService identityContractService) {
		super(repository, entityEventManager, formService);
		//
		Assert.notNull(roleService);
		Assert.notNull(entityEventManager);
		Assert.notNull(authChangeRepository);
		Assert.notNull(roleConfiguration);
		Assert.notNull(identityContractService);
		//
		this.repository = repository;
		this.roleService = roleService;
		this.authChangeRepository = authChangeRepository;
		this.entityEventManager = entityEventManager;
		this.roleConfiguration = roleConfiguration;
		this.identityContractService = identityContractService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITY, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentityDto getByCode(String code) {
		return getByUsername(code);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentityDto getByUsername(String username) {
		return toDto(repository.findOneByUsername(username));
	}
	
	@Override
	protected IdmIdentity toEntity(IdmIdentityDto dto, IdmIdentity entity) {
		IdmIdentity identity = super.toEntity(dto, entity);
		if (identity != null) {
			if (identity.getState() == null) {
				identity.setState(IdentityState.CREATED); // default state
			}
			identity.setDisabled(identity.getState().isDisabled()); // redundant attribute for queries
		}
		return identity;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentity> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmIdentity_.username)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.firstName)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.lastName)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.email)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmIdentity_.description)), "%" + filter.getText().toLowerCase() + "%")					
					));
		}
		// Identity first name
		if (StringUtils.isNotEmpty(filter.getFirstName())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.firstName), filter.getFirstName()));
		}
		// Identity lastName
		if (StringUtils.isNotEmpty(filter.getLastName())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.lastName), filter.getLastName()));
		}
		// identity with any of given role (OR)
		List<UUID> roles = filter.getRoles();
		if (!roles.isEmpty()) {
			Subquery<IdmIdentityRole> subquery = query.subquery(IdmIdentityRole.class);
			Root<IdmIdentityRole> subRoot = subquery.from(IdmIdentityRole.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmIdentityRole_.identityContract).get(IdmIdentityContract_.identity), root), // correlation attr
                    		subRoot.get(IdmIdentityRole_.role).get(IdmRole_.id).in(roles)
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		// property
		if (StringUtils.equals(IdmIdentity_.username.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.username), filter.getValue()));
		}
		if (StringUtils.equals(IdmIdentity_.firstName.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.firstName), filter.getValue()));
		}
		if (StringUtils.equals(IdmIdentity_.lastName.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.lastName), filter.getValue()));
		}
		if (StringUtils.equals(IdmIdentity_.email.getName(), filter.getProperty())) {
			predicates.add(builder.equal(root.get(IdmIdentity_.email), filter.getValue()));
		}
		//
		// disabled
		if (filter.getDisabled() != null) {
			predicates.add(builder.equal(root.get(IdmIdentity_.disabled), filter.getDisabled()));
		}
		// treeNode
		if (filter.getTreeNode() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			//
			if (filter.isRecursively()) {
				Subquery<IdmTreeNode> subqueryTreeNode = query.subquery(IdmTreeNode.class);
				Root<IdmTreeNode> subqueryTreeNodeRoot = subqueryTreeNode.from(IdmTreeNode.class);
				subqueryTreeNode.select(subqueryTreeNodeRoot);
				subqueryTreeNode.where(
						builder.and(
								builder.equal(subqueryTreeNodeRoot.get(IdmTreeNode_.id), filter.getTreeNode()),
								builder.between(
	                    				subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
	                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
	                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt)
	                    		)
						));				
	
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
	                    		builder.exists(subqueryTreeNode)
	                    		)
	                    );
			} else {
				subquery.where(
	                    builder.and(
	                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
	                    		builder.equal(subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.id), filter.getTreeNode())
	                    		)
	                    );
			}
			predicates.add(builder.exists(subquery));
		}
		// treeType
		if (filter.getTreeType() != null) {
			Subquery<IdmIdentityContract> subquery = query.subquery(IdmIdentityContract.class);
			Root<IdmIdentityContract> subRoot = subquery.from(IdmIdentityContract.class);
			subquery.select(subRoot);
			subquery.where(
                    builder.and(
                    		builder.equal(subRoot.get(IdmIdentityContract_.identity), root), // correlation attr
                    		builder.equal(
                    				subRoot.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType).get(IdmTreeType_.id), 
                    				filter.getTreeType())
                    		)
            );			
			predicates.add(builder.exists(subquery));
		}
		//
		return predicates;
	}
	
	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	@Override
	@Transactional
	public List<OperationResult> passwordChange(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		Assert.notNull(identity);
		//
		LOG.debug("Changing password for identity [{}]", identity.getUsername());
		EventContext<IdmIdentityDto> context = entityEventManager.process(
					new IdentityEvent(
							IdentityEventType.PASSWORD,
							identity, 
							ImmutableMap.of(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO, passwordChangeDto)));
		// get all password change results
		// more provisioning operation can be executed for one password change - we need to distinct them by account id
		Map<UUID, OperationResult> passwordChangeResults = new HashMap<>(); // accountId / result
		context.getResults().forEach(eventResult -> {
			eventResult.getResults().forEach(result -> {
				if (result.getModel() != null) {
					boolean success = result.getModel().getStatusEnum().equals(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_SUCCESS.name());
					boolean failure = result.getModel().getStatusEnum().equals(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_FAILED.name());
					if (success || failure) {				
						IdmAccountDto resultAccount = (IdmAccountDto) result.getModel().getParameters().get(IdmAccountDto.PARAMETER_NAME);
						if (!passwordChangeResults.containsKey(resultAccount.getId())) {
							passwordChangeResults.put(resultAccount.getId(), result);
						} else if (failure) {
							// failure has higher priority
							passwordChangeResults.put(resultAccount.getId(), result);
						}
					}
				}
			});
		});
		return new ArrayList<>(passwordChangeResults.values());
	}
	
	@Override
	public String getNiceLabel(IdmIdentityDto identity) {
		if (identity == null) {
			return null;
		}
		// if lastname is blank, then username is returned
		if (StringUtils.isBlank(identity.getLastName())) {
			return identity.getUsername();
		}
		//
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotEmpty(identity.getTitleBefore())) {
			sb.append(identity.getTitleBefore()).append(' ');
		}
		if (StringUtils.isNotEmpty(identity.getFirstName())) {
			sb.append(identity.getFirstName()).append(' ');
		}
		if (StringUtils.isNotEmpty(identity.getLastName())) {
			sb.append(identity.getLastName());
		}
		if (StringUtils.isNotEmpty(identity.getTitleAfter())) {
			sb.append(", ").append(identity.getTitleAfter());
		}
		return sb.toString().trim();
	}

	/**
	 * Find all identities by assigned role name
	 * 
	 * @param roleName
	 * @return Identities with give role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllByRoleName(String roleName) {
		IdmRoleDto role = roleService.getByCode(roleName);
		if(role == null){
			return new ArrayList<>();
		}
		
		return this.findAllByRole(role.getId());				
	}
	
	/**
	 * Find all identities by assigned role
	 * 
	 * @param roleId
	 * @return List of IdmIdentity with assigned role
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllByRole(UUID roleId) {
		Assert.notNull(roleId, "Role is required");
		//
		return toDtos(repository.findAllByRole(roleId), false);
	}

	/**
	 * Method find all managers by identity contract and return manager's
	 * 
	 * @param forIdentity
	 * @return String - usernames separate by commas
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllManagers(UUID forIdentity) {
		return this.findAllManagers(forIdentity, null);
	}

	/**
	 * Method finds all identity's managers by identity contract (guarantee or by assigned tree structure).
	 * 
	 * @param forIdentity
	 * @param byTreeType If optional tree type is given, then only managers defined with this type is returned
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllManagers(UUID forIdentity, UUID byTreeType) {
		Assert.notNull(forIdentity, "Identity id is required.");
		//		
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setManagersFor(forIdentity);
		filter.setManagersByTreeType(byTreeType);
		//
		List<IdmIdentityDto> results = new ArrayList<>();
		Page<IdmIdentityDto> managers = find(filter, new PageRequest(0, 50, Sort.Direction.ASC, IdmIdentity_.username.getName()));
		results.addAll(managers.getContent());
		while (managers.hasNext()) {
			managers = find(filter, managers.nextPageable());
			results.addAll(managers.getContent());
		}
		//
		if (!results.isEmpty()) {
			return results;
		}
		// return all identities with admin role
		return this.findAllByRole(roleConfiguration.getAdminRoleId());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityDto> findAllGuaranteesByRoleId(UUID roleId) {
		IdmRoleDto role = roleService.get(roleId);
		Assert.notNull(role, "Role is required. Role by name [" + roleId + "] not found.");
		return role.getGuarantees()
				.stream()
				.map(guarantee -> {
					return get(guarantee.getGuarantee());
				})
				.collect(Collectors.toList());			
	}
	
	/**
	 * Contains list of identities some identity with given username.
	 * If yes, then return true.
	 * @param identities
	 * @param identifier
	 * @return
	 */
	@Override
	public boolean containsUser(List<IdmIdentityDto> identities, String identifier){
		return identities.stream().anyMatch(identity -> {
			return identity.getId().toString().equals(identifier);
		});
	}
	
	/**
	 * Convert given identities to string of user names separate with comma 
	 * @param identities
	 * @return
	 */
	@Override
	public String convertIdentitiesToString(List<IdmIdentityDto> identities) {
		if(identities == null){
			return "";
		}
		return identities
				.stream()
				.map(IdmIdentityDto::getUsername)
				.collect(Collectors.joining(","));
	}

	/**
	 * Update authority change timestamp for all given identities. The IdmAuthorityChange
	 * entity is either updated or created anew, if the original relation did not exist.
	 * @param identities identities to update
	 * @param changeTime authority change time
	 */
	@Transactional
	@Override
	public void updateAuthorityChange(List<UUID> identities, DateTime changeTime) {
		Assert.notNull(identities);
		//
		if (identities.isEmpty()) {
			return;
		}
		List<UUID> identitiesCopy = Lists.newArrayList(identities);
		// handle identities without IdmAuthorityChange entity relation (auth. change is null)
		Map<UUID, IdmIdentity> withoutChangeMap = new HashMap<>();
		List<IdmIdentity> withoutAuthChangeRel = repository.findAllWithoutAuthorityChange(identitiesCopy);
		withoutAuthChangeRel.forEach(i -> withoutChangeMap.put(i.getId(), i));
		if (!withoutAuthChangeRel.isEmpty()) {
			identitiesCopy.removeAll(withoutChangeMap.keySet());
			createAuthorityChange(withoutChangeMap.values(), changeTime);
		}
		// run update query on the rest of identities
		if (!identitiesCopy.isEmpty()) {
			repository.setIdmAuthorityChangeForIdentity(identitiesCopy, changeTime);
		}
	}
	
	@Override
	public IdmIdentityDto enable(UUID identityId, BasePermission... permission) {
		Assert.notNull(identityId);
		IdmIdentityDto identity = get(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId.toString()));
		}
		//
		if (identity.getState() != IdentityState.DISABLED_MANUALLY) {
			// not disabled
			throw new ResultCodeException(CoreResultCode.IDENTITY_NOT_DISABLED_MANUALLY, ImmutableMap.of(
					IdmIdentity_.username.getName(), identity.getUsername(),
					IdmIdentity_.state.getName(), identity.getState()));
		}
		identity.setState(evaluateState(identity));
		return save(identity, permission);
	}
	
	@Override
	public IdmIdentityDto disable(UUID identityId, BasePermission... permission) {
		Assert.notNull(identityId);
		IdmIdentityDto identity = get(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId.toString()));
		}
		//
		if (identity.getState() == IdentityState.DISABLED_MANUALLY) {
			// already disabled
			throw new ResultCodeException(CoreResultCode.IDENTITY_ALREADY_DISABLED_MANUALLY, ImmutableMap.of(
					IdmIdentity_.username.getName(), identity.getUsername()));
			
		}
		identity.setState(IdentityState.DISABLED_MANUALLY);
		return save(identity, permission);
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdentityState evaluateState(UUID identityId) {
		Assert.notNull(identityId);
		IdmIdentityDto identity = get(identityId);
		if (identity == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("entity", identityId.toString()));
		}
		//
		// manually disabled - cannot be enable automatically
		if (identity.getState() == IdentityState.DISABLED_MANUALLY) {
			return IdentityState.DISABLED_MANUALLY;
		}
		//
		return evaluateState(identity);
	}
	
	/**
	 * Return evaluated state without {@link IdentityState#DISABLED_MANUALLY} check
	 * - can be used internally for enable identity
	 * 
	 * @param identity
	 * @return
	 */
	private IdentityState evaluateState(IdmIdentityDto identity) {
		if (identity.getId() == null) {
			return IdentityState.CREATED;
		}
		// read identity contract
		List<IdmIdentityContractDto> contracts = identityContractService.findAllByIdentity(identity.getId());
		if (contracts.isEmpty()) {
			return IdentityState.NO_CONTRACT;
		}
		//
		// evaluate state by contracts
		boolean hasFutureContract = false;
		boolean hasValidContract = false;
		boolean hasExcludedContract = false;
		boolean hasInvalidContract = false;
		for (IdmIdentityContractDto contract : contracts) {
			if (contract.isValid()) {
				if (contract.isExcluded()) {
					hasExcludedContract = true;
				} else {
					hasValidContract = true;
				}
			} else if (contract.isValidNowOrInFuture()) {
				hasFutureContract = true;
			} else {
				hasInvalidContract = true;
			}
		}
		if (hasValidContract) {
			return IdentityState.VALID;
		}
		if (hasFutureContract) {
			return IdentityState.FUTURE_CONTRACT;
		}
		if (hasExcludedContract) {
			return IdentityState.DISABLED; // new identity excluded state?
		}
		if (hasInvalidContract) {
			return IdentityState.LEFT;
		}
		//
		return IdentityState.DISABLED;
	}

	private void createAuthorityChange(Collection<IdmIdentity> withoutAuthChange, DateTime changeTime) {
		for (IdmIdentity identity : withoutAuthChange) {
			IdmAuthorityChange ac = new IdmAuthorityChange();
			ac.setAuthChangeTimestamp(changeTime);
			ac.setIdentity(identity);
			authChangeRepository.save(ac);
		}
	}
}
