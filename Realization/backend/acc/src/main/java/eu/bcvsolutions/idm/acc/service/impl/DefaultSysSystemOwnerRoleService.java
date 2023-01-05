package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerRoleDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerRoleFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwnerRole;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwnerRole_;
import eu.bcvsolutions.idm.acc.repository.SysSystemOwnerRoleRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerRoleService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ExportDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * System owners - by role
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Service
public class DefaultSysSystemOwnerRoleService extends AbstractEventableDtoService<SysSystemOwnerRoleDto, SysSystemOwnerRole, SysSystemOwnerRoleFilter>
		implements SysSystemOwnerRoleService {

	@Autowired
	public DefaultSysSystemOwnerRoleService(SysSystemOwnerRoleRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEMOWNERROLE, getEntityClass());
	}

	@Override
	protected SysSystemOwnerRoleDto internalExport(UUID id) {
		SysSystemOwnerRoleDto dto = this.get(id);

		// Advanced pairing
		// We cannot clear all embedded data, because we need to export DTO for
		// connected owner.
		BaseDto owenDto = dto.getEmbedded().get(SysSystemOwnerRole_.ownerRole.getName());
		dto.getEmbedded().clear();
		dto.getEmbedded().put(SysSystemOwnerRole_.ownerRole.getName(), owenDto);

		return dto;
	}

	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		Assert.notNull(batch, "Export batch must exist!");
		super.export(id, batch);

		// Advanced pairing
		ExportDescriptorDto descriptorDto = getExportManager().getDescriptor(batch, this.getDtoClass());
		descriptorDto.getAdvancedParingFields().add(SysSystemOwnerRole_.ownerRole.getName());
	}

	@Override
	protected List<Predicate> toPredicates(Root<SysSystemOwnerRole> root, CriteriaQuery<?> query, CriteriaBuilder builder,
										   SysSystemOwnerRoleFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// system
		UUID system = filter.getSystem();
		if (system != null) {
			predicates.add(builder.equal(
					root.get(SysSystemOwnerRole_.system).get(AbstractEntity_.id),
					system)
			);
		}
		// owner role
		UUID ownerRole = filter.getOwnerRole();
		if (ownerRole != null) {
			predicates.add(builder.equal(root.get(SysSystemOwnerRole_.ownerRole).get(AbstractEntity_.id), ownerRole));
		}

		return predicates;
	}
}
