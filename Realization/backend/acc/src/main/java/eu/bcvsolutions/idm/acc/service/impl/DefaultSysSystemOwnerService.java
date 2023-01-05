package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.acc.dto.SysSystemOwnerDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemOwnerFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwner;
import eu.bcvsolutions.idm.acc.entity.SysSystemOwner_;
import eu.bcvsolutions.idm.acc.repository.SysSystemOwnerRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemOwnerService;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.ExportDescriptorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * System owners
 *
 * @author Roman Kucera
 * @since 13.0.0
 */
@Service
public class DefaultSysSystemOwnerService extends AbstractEventableDtoService<SysSystemOwnerDto, SysSystemOwner, SysSystemOwnerFilter>
		implements SysSystemOwnerService {

	@Autowired
	public DefaultSysSystemOwnerService(SysSystemOwnerRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(AccGroupPermission.SYSTEMOWNER, getEntityClass());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<SysSystemOwnerDto> findBySystem(UUID systemId, Pageable pageable, BasePermission... permission) {
		Assert.notNull(systemId, "System identifier is required.");

		SysSystemOwnerFilter filter = new SysSystemOwnerFilter();
		filter.setSystem(systemId);

		return find(filter, pageable, permission);
	}

	@Override
	protected SysSystemOwnerDto internalExport(UUID id) {
		SysSystemOwnerDto dto = this.get(id);

		// Advanced pairing
		// We cannot clear all embedded data, because we need to export DTO for
		// connected owners.
		BaseDto ownerDto = dto.getEmbedded().get(SysSystemOwner_.owner.getName());
		dto.getEmbedded().clear();
		dto.getEmbedded().put(SysSystemOwner_.owner.getName(), ownerDto);

		return dto;
	}

	@Override
	public void export(UUID id, IdmExportImportDto batch) {
		Assert.notNull(batch, "Export batch must exist!");
		super.export(id, batch);

		// Advanced pairing
		ExportDescriptorDto descriptorDto = getExportManager().getDescriptor(batch, this.getDtoClass());
		descriptorDto.getAdvancedParingFields().add(SysSystemOwner_.owner.getName());
	}

	@Override
	protected List<Predicate> toPredicates(Root<SysSystemOwner> root, CriteriaQuery<?> query, CriteriaBuilder builder,
										   SysSystemOwnerFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// system
		UUID system = filter.getSystem();
		if (system != null) {
			predicates.add(builder.equal(root.get(SysSystemOwner_.system).get(AbstractEntity_.id), system));
		}
		// owner
		UUID owner = filter.getOwner();
		if (owner != null) {
			predicates.add(builder.equal(root.get(SysSystemOwner_.owner).get(AbstractEntity_.id), owner));
		}

		return predicates;
	}
}
