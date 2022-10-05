package eu.bcvsolutions.idm.core.config.domain;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ReflectionUtils;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.bcvsolutions.idm.core.api.utils.ReflectionUtils.getAllDeclaredFieldsSuperClassAndInterface;
import static eu.bcvsolutions.idm.core.api.utils.ReflectionUtils.instantiateUsingNoArgConstructor;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class DataFilterToBaseFilterConverter implements ConditionalConverter<DataFilter, BaseFilter> {


    @Override
    public BaseFilter convert(MappingContext<DataFilter, BaseFilter> mappingContext) {
        final DataFilter source = mappingContext.getSource();
        final BaseFilter destination = mappingContext.getDestination() == null ?
                instantiateUsingNoArgConstructor(mappingContext.getDestinationType(), null) :
                mappingContext.getDestination();

        final MultiValueMap<String, Object> data = source.getData();
        data.entrySet().forEach(entry -> ReflectionUtils.invokeSetter(destination, entry.getKey(), entry.getValue()));
        return destination;
    }

    @Override
    public MatchResult match(Class<?> src, Class<?> dest) {
        return DataFilter.class.isAssignableFrom(src) && !DataFilter.class.isAssignableFrom(dest) && BaseFilter.class.isAssignableFrom(dest) ? MatchResult.FULL : MatchResult.NONE;
    }

}
