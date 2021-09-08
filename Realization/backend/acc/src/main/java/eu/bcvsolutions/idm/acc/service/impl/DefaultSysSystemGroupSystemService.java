package eu.bcvsolutions.idm.acc.service.impl;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemGroupType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemGroupSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemGroupSystemFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute_;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping_;
import eu.bcvsolutions.idm.acc.entity.SysSystemGroupSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemGroupSystem_;
import eu.bcvsolutions.idm.acc.entity.SysSystemGroup_;
import eu.bcvsolutions.idm.acc.entity.SysSystem_;
import eu.bcvsolutions.idm.acc.repository.SysSystemGroupSystemRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemGroupSystemService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import java.util.List;
import java.util.UUID;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * System groups-system relation service
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Service("sysSystemGroupSystemService")
public class DefaultSysSystemGroupSystemService
		extends AbstractEventableDtoService<SysSystemGroupSystemDto, SysSystemGroupSystem, SysSystemGroupSystemFilter>
		implements SysSystemGroupSystemService {


	@Autowired
	private SysSystemAttributeMappingService attributeMappingService;
	
	@Autowired
	public DefaultSysSystemGroupSystemService(SysSystemGroupSystemRepository repository,
											  EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEMGROUP, getEntityClass());
	}

	@Override
	@Transactional
	public SysSystemGroupSystemDto saveInternal(SysSystemGroupSystemDto dto) {
		UUID mergeAttributeId = dto.getMergeAttribute();
		UUID systemId = dto.getSystem();
		if (mergeAttributeId != null) {
			Assert.notNull(systemId, "System ID cannot be null for save a merge attribute!");
			SysSystemAttributeMappingFilter systemAttributeMappingFilter = new SysSystemAttributeMappingFilter();
			systemAttributeMappingFilter.setId(mergeAttributeId);
			systemAttributeMappingFilter.setSystemId(systemId);
			systemAttributeMappingFilter.setOperationType(SystemOperationType.PROVISIONING);
			systemAttributeMappingFilter.setEntityType(SystemEntityType.IDENTITY);

			long count = attributeMappingService.count(systemAttributeMappingFilter);
			Assert.isTrue(count ==1, "Merge attribute was not found for given system.");
		}
		return super.saveInternal(dto);
	}

	@Override
	public List<SysSystemGroupSystemDto> getSystemsInCrossDomainGroup(SysSystemDto system) {
		Assert.notNull(system, "System cannot be null!");
		Assert.notNull(system.getId(), "System ID cannot be null!");

		SysSystemGroupSystemFilter systemGroupSystemFilter = new SysSystemGroupSystemFilter();
		systemGroupSystemFilter.setOthersSystemsInGroupSystemId(system.getId());
		systemGroupSystemFilter.setGroupType(SystemGroupType.CROSS_DOMAIN);
		systemGroupSystemFilter.setDisabled(Boolean.FALSE);
		
		return this.find(systemGroupSystemFilter, null).getContent();
	}

	@Override
	protected List<Predicate> toPredicates(Root<SysSystemGroupSystem> root, CriteriaQuery<?> query, CriteriaBuilder builder, SysSystemGroupSystemFilter filter) {

		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		// System
		if (filter.getSystemId() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.system).get(SysSystem_.id), filter.getSystemId()));
		}

		// System group
		if (filter.getSystemGroupId() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.id), filter.getSystemGroupId()));
		}
		
		// Disable group
		if (filter.getDisabled() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.disabled), filter.getDisabled()));
		}
		// Group type
		if (filter.getGroupType() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.type), filter.getGroupType()));
		}

		// Merge attribute by schema attribute code.
		if (filter.getMergeAttributeCode() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.mergeAttribute).get(SysSystemAttributeMapping_.schemaAttribute).get(SysSchemaAttribute_.name), filter.getMergeAttributeCode()));
		}
		
		// Merge attribute by ID.
		if (filter.getMergeMappingAttributeId() != null) {
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.mergeAttribute).get(SysSystemAttributeMapping_.id), filter.getMergeMappingAttributeId()));
		}

		// Get all systems in same groups as given system.
		if (filter.getOthersSystemsInGroupSystemId() != null) {

			Subquery<SysSystemGroupSystem> subquery = query.subquery(SysSystemGroupSystem.class);
			Root<SysSystemGroupSystem> subRoot = subquery.from(SysSystemGroupSystem.class);
			subquery.select(subRoot);
			subquery.where(
					builder.and(
							builder.equal(root.get(SysSystemGroupSystem_.systemGroup), subRoot.get(SysSystemGroupSystem_.systemGroup)), // correlation attr
							builder.equal(subRoot.get(SysSystemGroupSystem_.system).get(SysSystem_.id), filter.getOthersSystemsInGroupSystemId())
					)
			);
			predicates.add(builder.exists(subquery));
		}
		
		// Get cross domains groups for given role-system (using same merge attribute).
		if (filter.getCrossDomainsGroupsForRoleSystemId() != null) {

			Subquery<SysRoleSystemAttribute> subquery = query.subquery(SysRoleSystemAttribute.class);
			Root<SysRoleSystemAttribute> subRoot = subquery.from(SysRoleSystemAttribute.class);
			subquery.select(subRoot);
			subquery.where(
					builder.and(
							builder.equal(root.get(SysSystemGroupSystem_.mergeAttribute), subRoot.get(SysRoleSystemAttribute_.systemAttributeMapping)), // correlation attr
							builder.equal(subRoot.get(SysRoleSystemAttribute_.roleSystem).get(SysRoleSystem_.id), filter.getCrossDomainsGroupsForRoleSystemId())
					)
			);
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.disabled), Boolean.FALSE));
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.type), SystemGroupType.CROSS_DOMAIN));
			predicates.add(builder.exists(subquery));
		}
		
		// Get cross domains groups for given role (using same merge attribute).
		if (filter.getCrossDomainsGroupsForRoleId() != null) {

			Subquery<SysRoleSystemAttribute> subquery = query.subquery(SysRoleSystemAttribute.class);
			Root<SysRoleSystemAttribute> subRoot = subquery.from(SysRoleSystemAttribute.class);
			subquery.select(subRoot);
			subquery.where(
					builder.and(
							builder.equal(root.get(SysSystemGroupSystem_.mergeAttribute), subRoot.get(SysRoleSystemAttribute_.systemAttributeMapping)), // correlation attr
							builder.equal(subRoot.get(SysRoleSystemAttribute_.roleSystem).get(SysRoleSystem_.role).get(IdmRole_.id), filter.getCrossDomainsGroupsForRoleId())
					)
			);
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.disabled), Boolean.FALSE));
			predicates.add(builder.equal(root.get(SysSystemGroupSystem_.systemGroup).get(SysSystemGroup_.type), SystemGroupType.CROSS_DOMAIN));
			predicates.add(builder.exists(subquery));
		}
		

		return predicates;
	}
}
