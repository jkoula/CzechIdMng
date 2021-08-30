package eu.bcvsolutions.idm.core.model.service.thin;

import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.thin.IdmRoleThinDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadDtoService;
import eu.bcvsolutions.idm.core.api.service.thin.IdmRoleThinService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.entity.thin.IdmRoleThin;
import eu.bcvsolutions.idm.core.model.repository.thin.IdmRoleThinRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

/**
 * Operations with roles - thin variant:
 * - supports get method only.
 *
 * @author Vít Švanda
 * @since 11.2.0
 */
public class DefaultIdmRoleThinService 
		extends AbstractReadDtoService<IdmRoleThinDto, IdmRoleThin, IdmRoleFilter>
		implements IdmRoleThinService {

	@Autowired
	public DefaultIdmRoleThinService(IdmRoleThinRepository repository) {
		super(repository);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ROLE, IdmRole.class);
	}
	
	@Override
	protected Specification<IdmRoleThin> toCriteria(
			IdmRoleFilter filter, 
			boolean applyFetchMode,
			BasePermission... permission) {
		throw new UnsupportedOperationException("Find methods using criteria are not supported for thin entity. Use get method only.");
	}
	
	@Override
	protected IdmRoleThin checkAccess(IdmRoleThin entity, BasePermission... permission) {
		if (!ObjectUtils.isEmpty(PermissionUtils.trimNull(permission))) {
			throw new UnsupportedOperationException("Check access on thin entity is not supported.");
		}
		return entity;
	}

	@Override
	public boolean supports(Class<?> delimiter) {
		// We need to use equals, because IdmRoleDto is parent of IdmRoleThinDto.
		return getDtoClass().equals(delimiter);
	}
}
