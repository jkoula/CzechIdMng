package eu.bcvsolutions.idm.core.api.rest;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

/**
 * Providing a bean of this type allows clients to provide their custom implementation of filter translation.
 * This interface is a replacement for now obsolete overriding of {@link AbstractReadDtoController#toFilter(MultiValueMap)}
 * which now should not be overriden anymore.
 *
 * @see AbstractReadDtoController#paramsToFilter(MultiValueMap) which calls {@link PluggableFilterTranslator#transform(BaseFilter, MultiValueMap)}
 *      on each bean specified for given {@link BaseFilter} type.
 *
 * Main advantage of using this approach is decoupling of filter translation logic from controller implementation, which
 * also enables module specific translation of filters.
 *
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public interface PluggableFilterTranslator<F extends BaseFilter> {

    public F transform(Optional<F> filter, MultiValueMap<String, Object> parameters);

}
