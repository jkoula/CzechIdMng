package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleSystemFilter;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.service.IdmRoleSystemService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Priority;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Default mock core implementation for idm-role-system service.
 * It is a parent for SysRoleSystemService in Acc module (we need to work with role-system also in the core module).
 *
 * @author Vít Švanda
 * @since 11.2.0
 *
 */
@Service
@Priority(Ordered.LOWEST_PRECEDENCE - 10)
public class MockIdmRoleSystemService
		implements IdmRoleSystemService, ReadDtoService {

	@Override
	public Class getDtoClass() {
		return IdmRoleSystemDto.class;
	}

	@Override
	public Class<? extends BaseEntity> getEntityClass() {
		return null;
	}

	@Override
	public Class getFilterClass() {
		return IdmRoleSystemFilter.class;
	}

	@Override
	public boolean supportsToDtoWithFilter() {
		return false;
	}

	@Override
	public BaseDto get(Serializable id, BasePermission... permission) {
		return null;
	}

	@Override
	public BaseDto get(Serializable id, BaseFilter context, BasePermission... permission) {
		return null;
	}

	@Override
	public Page find(Pageable pageable, BasePermission... permission) {
		return null;
	}

	@Override
	public Page find(BaseFilter filter, Pageable pageable, BasePermission... permission) {
		return null;
	}

	@Override
	public Page<UUID> findIds(Pageable pageable, BasePermission... permission) {
		return null;
	}

	@Override
	public Page<UUID> findIds(BaseFilter filter, Pageable pageable, BasePermission... permission) {
		return null;
	}

	@Override
	public long count(BaseFilter filter, BasePermission... permission) {
		return 0;
	}

	@Override
	public boolean isNew(BaseDto dto) {
		return false;
	}

	@Override
	public Set<String> getPermissions(Serializable id) {
		return null;
	}

	@Override
	public Set<String> getPermissions(BaseDto dto) {
		return null;
	}

	@Override
	public BaseDto checkAccess(BaseDto dto, BasePermission... permission) {
		return null;
	}

	@Override
	public void export(UUID id, IdmExportImportDto batch) {

	}

	@Override
	public boolean supports(Object delimiter) {
		return false;
	}
}

