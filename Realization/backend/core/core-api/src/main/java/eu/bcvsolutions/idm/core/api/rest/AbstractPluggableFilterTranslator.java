package eu.bcvsolutions.idm.core.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public abstract class AbstractPluggableFilterTranslator<F extends BaseFilter> implements PluggableFilterTranslator<F> {

    private ParameterConverter parameterConverter;

    private final LookupService lookupService;
    private final ObjectMapper objectMapper;
    protected AbstractPluggableFilterTranslator(LookupService lookupService, ObjectMapper objectMapper) {
        this.lookupService = lookupService;
        this.objectMapper = objectMapper;
    }

    protected final ParameterConverter getParameterConverter() {
        if (parameterConverter == null) {
            parameterConverter = new ParameterConverter(lookupService, objectMapper);
        }
        return parameterConverter;
    }

    @Override
    public final F transform(Optional<F> filter, MultiValueMap<String, Object> parameters) {
        return transformInternal(filter.orElse(getEmptyFilter()), parameters);
    }

    protected abstract F transformInternal(F filter, MultiValueMap<String, Object> parameters);

    protected abstract F getEmptyFilter();
}
