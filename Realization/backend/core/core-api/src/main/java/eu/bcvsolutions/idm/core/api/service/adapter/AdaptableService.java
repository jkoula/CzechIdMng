package eu.bcvsolutions.idm.core.api.service.adapter;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface AdaptableService<D extends BaseDto, F extends BaseFilter, R> extends ReadDtoService<D, F> {

    <F2 extends BaseFilter> DtoAdapter<D, R> getAdapter(F2 originalFilter);

}
