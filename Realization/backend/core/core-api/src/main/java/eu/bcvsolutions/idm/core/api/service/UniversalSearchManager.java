package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.eav.api.service.UniversalSearchType;
import java.util.List;

/**
 * Universal search manager
 *
 * @author Vít Švanda
 * @since 11.3.0
 *
 */
public interface UniversalSearchManager {

	/**
	 * Find all registered universal search types.
	 */
	List<UniversalSearchType<? extends AbstractDto, ? extends BaseFilter>> getSupportedTypes();

	/**
	 * Convert type to DTO.
	 */
	UniversalSearchTypeDto convertUniversalSearchTypeToDto(UniversalSearchType<? extends AbstractDto, ? extends BaseFilter> universalSearchType);
}
