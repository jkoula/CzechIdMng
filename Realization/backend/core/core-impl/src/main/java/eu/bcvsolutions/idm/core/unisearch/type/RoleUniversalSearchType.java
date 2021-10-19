package eu.bcvsolutions.idm.core.unisearch.type;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractUniversalSearchType;
import eu.bcvsolutions.idm.core.model.entity.IdmRole_;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/**
 *
 * Universal search for roles.
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = RoleUniversalSearchType.PROPERTY_SEARCH_TYPE)
@Component(RoleUniversalSearchType.NAME)
public class RoleUniversalSearchType extends AbstractUniversalSearchType<IdmRoleDto, IdmRoleFilter> {

	public static final String PROPERTY_SEARCH_TYPE =
				ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.universal-search-type.role.enabled";

	public static final String NAME = "role-universal-search-type";
	@Autowired
	private IdmRoleService service;

	@Override
	public Class<IdmRoleDto> getOwnerType() {
		return IdmRoleDto.class;
	}

	@Override
	protected Pageable getPageable(Pageable pageable) {
		Sort sort = new Sort(Sort.Direction.ASC, IdmRole_.code.getName());

		return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
	}

	protected IdmRoleFilter createFilter(String text) {
		IdmRoleFilter filter = new IdmRoleFilter();
		filter.setText(text);
		return filter;
	}

	@Override
	protected ReadDtoService<IdmRoleDto, IdmRoleFilter> getService() {
		return service;
	}

	@Override
	public int getOrder() {
		return 20;
	}
}
