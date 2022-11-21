package eu.bcvsolutions.idm.core.api.service;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.model.service.util.MultiSourcePagedResource;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface MultiResourceProvider<D extends BaseDto, F extends DataFilter, R> {

    MultiSourcePagedResource<D,F, F, D> getMultiResource();
}
