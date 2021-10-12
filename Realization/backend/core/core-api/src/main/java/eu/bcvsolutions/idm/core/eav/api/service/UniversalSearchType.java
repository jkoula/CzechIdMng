package eu.bcvsolutions.idm.core.eav.api.service;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.UniversalSearchDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import org.springframework.core.Ordered;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Universal search type
 *
 * @author Vít Švanda
 * @since 11.3.0
 */
public interface UniversalSearchType<E extends AbstractDto,  F extends BaseFilter> extends Ordered{

	long count(String text, BasePermission... permission);

	Page<E> find(String text, Pageable pageable, BasePermission... permission);

	UniversalSearchDto dtoToUniversalSearchDto(AbstractDto dto);

	/**
	 * Bean name / unique identifier (spring bean name).
	 *
	 * @return
	 */
	String getId();

	/**
	 * Class of owner type for this universal search.
	 *
	 * @return
	 */
	Class<E> getOwnerType();

	/**
	 * Returns module of that universal search.
	 *
	 * @return
	 */
	default String getModule() {
		return EntityUtils.getModule(this.getClass());
	}
	
	/**
	 * Order of delegation (in select-box).
	 *
	 * @return
	 */
	@Override
	int getOrder();
	
}
