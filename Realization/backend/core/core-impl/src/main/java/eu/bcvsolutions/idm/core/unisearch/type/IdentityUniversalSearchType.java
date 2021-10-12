package eu.bcvsolutions.idm.core.unisearch.type;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractUniversalSearchType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * Universal search for identities.
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = IdentityUniversalSearchType.PROPERTY_SEARCH_TYPE)
@Component(IdentityUniversalSearchType.NAME)
public class IdentityUniversalSearchType extends AbstractUniversalSearchType<IdmIdentityDto, IdmIdentityFilter> {

	public static final String PROPERTY_SEARCH_TYPE =
			ConfigurationService.IDM_PRIVATE_PROPERTY_PREFIX + "core.universal-search-type.identity.enabled";

	public static final String NAME = "identity-universal-search-type";
	@Autowired
	private IdmIdentityService identityService;

	@Override
	public Class<IdmIdentityDto> getOwnerType() {
		return IdmIdentityDto.class;
	}

	protected IdmIdentityFilter createFilter(String text) {
		IdmIdentityFilter filter = new IdmIdentityFilter();
		filter.setText(text);
		return filter;
	}

	@Override
	protected ReadDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
