package eu.bcvsolutions.idm.acc.service.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemGroupType;
import eu.bcvsolutions.idm.acc.dto.*;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.entity.*;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.*;
import eu.bcvsolutions.idm.core.api.dto.*;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.*;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Priority;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Role could assign identity account on target system.
 * 
 * @author Radek Tomiška
 *
 */
@Service
@Priority(Ordered.LOWEST_PRECEDENCE - 100)
@SuppressWarnings(value = "rawtypes")
public class DefaultSysRoleSystemService
		extends AbstractReadWriteDtoService<SysRoleSystemDto, SysRoleSystem, SysRoleSystemFilter>
		implements SysRoleSystemService, IdmRoleSystemService {

	@Autowired private AccIdentityAccountService identityAccountService;
	@Autowired private IdmRoleService roleService;
	@Autowired private RequestManager requestManager;
	@Autowired private SysSystemMappingService systemMappingService;
	@Autowired private SysRoleSystemAttributeService roleSystemAttributeService;
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private LookupService lookupService;
	@Autowired private SysSystemGroupSystemService systemGroupSystemService;
	@Autowired private IdmIdentityRoleService identityRoleService;

	@Autowired
	public DefaultSysRoleSystemService(SysRoleSystemRepository repository) {
		super(repository);
	}

	@Override
	@Transactional
	public void delete(SysRoleSystemDto roleSystem, BasePermission... permission) {
		Assert.notNull(roleSystem, "Role system relation is required.");
		Assert.notNull(roleSystem.getId(), "Role system relation identifier is required.");

		SysRoleSystem roleSystemEntity = this.getEntity(roleSystem.getId());

		// Identity-role check.
		IdmIdentityRoleFilter identityRoleFilter = new IdmIdentityRoleFilter();
		identityRoleFilter.setRoleSystemId(roleSystemEntity.getId());
		long count = identityRoleService.count(identityRoleFilter);
		if (count > 0) {
			IdmRoleDto roleDto = DtoUtils.getEmbedded(roleSystem, SysRoleSystem_.role, IdmRoleDto.class, null);
			throw new ResultCodeException(AccResultCode.ROLE_SYSTEM_IS_USE_IN_IDENTITY_ROLE,
					ImmutableMap.of("role", roleDto != null ? roleDto.getBaseCode() : "-", "count", count));
		}

		//
		// delete attributes
		SysRoleSystemAttributeFilter filter = new SysRoleSystemAttributeFilter();
		filter.setRoleSystemId(roleSystem.getId());
		List<SysRoleSystemAttributeDto> attributes = roleSystemAttributeService.find(filter, null).getContent();
		// We must delete attribute against service NOT repository. Historical
		// controlled values are created by service.
		for (SysRoleSystemAttributeDto attribute : attributes) {
			roleSystemAttributeService.delete(attribute);
		}
		//
		// clear identityAccounts - only link on roleSystem
		AccIdentityAccountFilter identityAccountFilter = new AccIdentityAccountFilter();
		identityAccountFilter.setRoleSystemId(roleSystemEntity.getId());
		identityAccountService
			.find(identityAccountFilter, null)
			.getContent()
			.forEach(identityAccount -> {
				identityAccount.setRoleSystem(null);
				identityAccountService.save(identityAccount);
			});
		//
		// Cancel requests and request items using that deleting DTO
		requestManager.onDeleteRequestable(roleSystem);

		super.delete(roleSystem, permission);
	}

	@Override
	public SysRoleSystemDto save(SysRoleSystemDto dto, BasePermission... permission) {
		Assert.notNull(dto, "RoleSystem cannot be null!");
		Assert.notNull(dto.getRole(), "Role cannot be null!");
		Assert.notNull(dto.getSystem(), "System cannot be null!");
		Assert.notNull(dto.getSystemMapping(), "System mapping cannot be null!");

		SysSystemMappingDto systemMappingDto = systemMappingService.get(dto.getSystemMapping());

		// Used System mapping has to belong to the System the role is assigned to
		UUID systemUUID = dto.getSystem();
		UUID systemMappingSystemUUID = ((SysSchemaObjectClassDto) lookupService.lookupEmbeddedDto(systemMappingDto,
				SysSystemMapping_.objectClass)).getSystem();
		if (!systemUUID.equals(systemMappingSystemUUID)) {
			throw new ResultCodeException(AccResultCode.FOREIGN_SYSTEM_MAPPING_ASSIGNED,
					ImmutableMap.of("systemUUID", systemUUID, "systemMappingSystemUUID", systemMappingSystemUUID));
		}

		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setRoleId(dto.getRole());
		filter.setSystemId(dto.getSystem());

		List<SysRoleSystemDto> roleSystems = this.find(filter, null).getContent();
		boolean isDuplicated = roleSystems.stream().anyMatch(roleSystem -> {
			return !roleSystem.getId().equals(dto.getId());
		});

		if (isDuplicated) {
			IdmRoleDto roleDto = roleService.get(dto.getRole());
			SysSystemDto systemDto = DtoUtils.getEmbedded(roleSystems.get(0), SysRoleSystem_.system);
			throw new ResultCodeException(AccResultCode.ROLE_SYSTEM_ALREADY_EXISTS,
					ImmutableMap.of("role", roleDto.getCode(), "system", systemDto.getName()));
		}
		
		SysRoleSystemDto roleSystemDto = super.save(dto, permission);
		
		// Cross-domain or no-login role, cannot override an UID attribute!
		SysRoleSystemAttributeFilter systemAttributeFilter = new SysRoleSystemAttributeFilter();
		systemAttributeFilter.setRoleSystemId(roleSystemDto.getId());
		systemAttributeFilter.setInCrossDomainGroupOrIsNoLogin(Boolean.TRUE);
		if (roleSystemAttributeService.count(systemAttributeFilter) > 0) {
			systemAttributeFilter = new SysRoleSystemAttributeFilter();
			systemAttributeFilter.setRoleSystemId(roleSystemDto.getId());
			systemAttributeFilter.setIsUid(Boolean.TRUE);
			if (roleSystemAttributeService.count(systemAttributeFilter) > 0) {
				IdmRoleDto roleDto = roleService.get(roleSystemDto.getRole());
				SysSystemDto systemDto = DtoUtils.getEmbedded(roleSystemDto, SysRoleSystem_.system);
				throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_NO_LOGIN_CANNOT_OVERRIDE_UID,
						ImmutableMap.of("role", roleDto.getCode(), "system", systemDto.getName()));
			}
		}
		return roleSystemDto;
	}

	@Override
	public List<IdmConceptRoleRequestDto> getConceptsForSystem(List<IdmConceptRoleRequestDto> concepts, UUID systemId) {
		// Roles using in concepts
		Set<UUID> roleIds = concepts.stream() //
				.map(IdmConceptRoleRequestDto::getRole) //
				.filter(Objects::nonNull) //
				.distinct() //
				.collect(Collectors.toSet());
		// We have direct roles, but we need sub-roles too. Beware here could be many
		// selects!
		Set<UUID> allSubRoles = Sets.newHashSet(roleIds);
		Map<UUID, Set<UUID>> roleWithSubroles = new HashMap<UUID, Set<UUID>>();

		roleIds.forEach(roleId -> {
			Set<UUID> subRoles = roleCompositionService.findAllSubRoles(roleId).stream() //
					.map(IdmRoleCompositionDto::getSub) //
					.distinct() //
					.collect(Collectors.toSet()); //
			// Put to result map, where key is super role and value set of all sub-roles
			roleWithSubroles.put(roleId, subRoles);
			allSubRoles.addAll(subRoles);
		});

		SysRoleSystemFilter roleSystemFilter = new SysRoleSystemFilter();
		roleSystemFilter.setSystemId(systemId);
		roleSystemFilter.setRoleIds(allSubRoles);

		Set<UUID> roles = this.find(roleSystemFilter, null).getContent() //
				.stream() //
				.map(SysRoleSystemDto::getRole) //
				.distinct() //
				.collect(Collectors.toSet());

		return concepts.stream() //
				.filter(concept -> {
					UUID roleId = concept.getRole();
					if (roleId == null) {
						return false;
					}
					if (roles.contains(roleId)) {
						// Direct role
						return true;
					}
					Set<UUID> subRoles = roleWithSubroles.get(roleId);
					if (subRoles == null) {
						return false;
					}
					// Sub-role
					return roles.stream() //
							.filter(role -> subRoles.contains(role)) //
							.findFirst() //
							.isPresent();

				}).collect(Collectors.toList());
	}

	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}
	

	@Override
	protected SysRoleSystemDto toDto(SysRoleSystem entity, SysRoleSystemDto dto, SysRoleSystemFilter context) {
		SysRoleSystemDto roleSystemDto = super.toDto(entity, dto, context);

		if (context != null
				&& Boolean.TRUE.equals(context.getCheckIfIsInCrossDomainGroup())
				&& roleSystemDto != null
				&& roleSystemDto.getId() != null) {
			SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
			systemGroupSystemFilter.setCrossDomainsGroupsForRoleSystemId(roleSystemDto.getId());
			if (systemGroupSystemService.count(systemGroupSystemFilter) >= 1) {
				// This role-system overriding a merge attribute which is using in
				// active cross-domain group. -> We will set this information to the DTO.
				roleSystemDto.setInCrossDomainGroup(true);
			}
		}
		return roleSystemDto;
	}

	@Override
	protected SysRoleSystemDto internalExport(UUID id) {
		 SysRoleSystemDto roleSystemDto = this.get(id);
		 
		 // We cannot clear all embedded data, because we need to export DTO for connected role.
		 BaseDto roleDto = roleSystemDto.getEmbedded().get(SysRoleSystem_.role.getName());
		 roleSystemDto.getEmbedded().clear();
		 roleSystemDto.getEmbedded().put(SysRoleSystem_.role.getName(), roleDto);
		 
		 return roleSystemDto;
	}
	
	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		Assert.notNull(batch, "Export batch must exist!");
		// Export role-system
		super.export(id, batch);

		ExportDescriptorDto descriptorDto = getExportManager().getDescriptor(batch, this.getDtoClass());
		descriptorDto.setOptional(true);
		descriptorDto.getAdvancedParingFields().add(SysRoleSystem_.role.getName());
		
		// Export role systems
		SysRoleSystemAttributeFilter roleSystemAttributeFilter = new SysRoleSystemAttributeFilter();
		roleSystemAttributeFilter.setRoleSystemId(id);
		List<SysRoleSystemAttributeDto> roleSystemAttributes = roleSystemAttributeService.find(roleSystemAttributeFilter, null).getContent();
		if (roleSystemAttributes.isEmpty()) {
			roleSystemAttributeService.export(ExportManager.BLANK_UUID, batch);
		}
		roleSystemAttributes.forEach(roleSystemAttribute -> {
			roleSystemAttributeService.export(roleSystemAttribute.getId(), batch);
		});
		// Set parent field -> set authoritative mode for override attributes.
		this.getExportManager().setAuthoritativeMode(SysRoleSystemAttribute_.roleSystem.getName(), "systemId", SysRoleSystemAttributeDto.class,
				batch);
		// The override attribute is optional too.
		ExportDescriptorDto descriptorAttributeDto = getExportManager().getDescriptor(batch, SysRoleSystemAttributeDto.class);
		descriptorAttributeDto.setOptional(true);
		descriptorAttributeDto.getAdvancedParingFields().add(SysRoleSystemAttribute_.roleSystem.getName());
	}

	@Override
	protected List<Predicate> toPredicates(Root<SysRoleSystem> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			SysRoleSystemFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		if (filter.getRoleId() != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.role).get(IdmRole_.id), filter.getRoleId()));
		}

		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.system).get(SysSystem_.id), filter.getSystemId()));
		}

		if (filter.getSystemMappingId() != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.systemMapping).get(SysSystemMapping_.id),
					filter.getSystemMappingId()));
		}

		// CreateAccountByDefault
		Boolean createAccountByDefault = filter.getCreateAccountByDefault();
		if(createAccountByDefault != null) {
			predicates.add(builder.equal(root.get(SysRoleSystem_.createAccountByDefault), createAccountByDefault));
		}

		// Return role-system where is uses given attribute mapping
		if (filter.getAttributeMappingId() != null) {
			Subquery<SysRoleSystemAttribute> subquery = query.subquery(SysRoleSystemAttribute.class);
			Root<SysRoleSystemAttribute> subRoot = subquery.from(SysRoleSystemAttribute.class);
			subquery.select(subRoot);

			subquery.where(builder.and( //
					builder.equal(subRoot.get(SysRoleSystemAttribute_.roleSystem), root), // Correlation attribute
					builder.equal(subRoot.get(SysRoleSystemAttribute_.systemAttributeMapping).get(AbstractEntity_.id),
							filter.getAttributeMappingId())));

			predicates.add(builder.exists(subquery));
		}
		
		// Return role-system where is uses given attribute mapping
		if (filter.getAttributeMappingId() != null) {
			Subquery<SysRoleSystemAttribute> subquery = query.subquery(SysRoleSystemAttribute.class);
			Root<SysRoleSystemAttribute> subRoot = subquery.from(SysRoleSystemAttribute.class);
			subquery.select(subRoot);

			subquery.where(builder.and( //
					builder.equal(subRoot.get(SysRoleSystemAttribute_.roleSystem), root), // Correlation attribute
					builder.equal(subRoot.get(SysRoleSystemAttribute_.systemAttributeMapping).get(AbstractEntity_.id),
							filter.getAttributeMappingId())));

			predicates.add(builder.exists(subquery));
		}

		// Get role-systems with cross domains groups for given role (using same merge attribute).
		if (filter.getIsInCrossDomainGroupRoleId() != null) {

			Subquery<SysRoleSystemAttribute> subquery = query.subquery(SysRoleSystemAttribute.class);
			Root<SysRoleSystemAttribute> subRoot = subquery.from(SysRoleSystemAttribute.class);
			subquery.select(subRoot);

			Subquery<SysSystemGroupSystem> subquerySystemGroup = query.subquery(SysSystemGroupSystem.class);
			Root<SysSystemGroupSystem> subRootSystemGroup = subquerySystemGroup.from(SysSystemGroupSystem.class);
			subquerySystemGroup.select(subRootSystemGroup);

			subquerySystemGroup.where(builder.and(
							builder.equal(subRootSystemGroup.get(SysSystemGroupSystem_.mergeAttribute),
									subRoot.get(SysRoleSystemAttribute_.systemAttributeMapping))), // Correlation attribute
					builder.equal(subRootSystemGroup.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.disabled),
							Boolean.FALSE),
					builder.equal(subRootSystemGroup.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.type),
							SystemGroupType.CROSS_DOMAIN));
							
			subquery.where(builder.and( //
					builder.equal(subRoot.get(SysRoleSystemAttribute_.roleSystem), root), // Correlation attribute
					builder.exists(subquerySystemGroup),
					builder.equal(root.get(SysRoleSystem_.role).get(AbstractEntity_.id),
							filter.getIsInCrossDomainGroupRoleId())));

			predicates.add(builder.exists(subquery));
		}

		Set<UUID> ids = filter.getRoleIds();
		if (CollectionUtils.isNotEmpty(ids)) {
			predicates.add(root.get(SysRoleSystem_.role).get(IdmRole_.id).in(ids));
		}
		
		return predicates;
	}

}
