package eu.bcvsolutions.idm.core.config.domain;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ReflectionUtils;
import org.modelmapper.MappingException;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.bcvsolutions.idm.core.api.utils.ReflectionUtils.instantiateUsingNoArgConstructor;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class BaseFilterConverter implements ConditionalConverter<BaseFilter, BaseFilter> {

    @Override
    public BaseFilter convert(MappingContext<BaseFilter, BaseFilter> mappingContext) {
        final BaseFilter source = mappingContext.getSource();
        final Class<BaseFilter> destinationType = mappingContext.getDestinationType();
        final BaseFilter destination = instantiateUsingNoArgConstructor(destinationType, null);

        // property name -> getter method
        final Map<String, Method> definedSourceGetters = ReflectionUtils.getAllGetterMethods(mappingContext.getSourceType()).stream()
                .collect(Collectors.toMap(ReflectionUtils::getLowercaseFieldNameFromGetter, i -> i, (t, t2) -> t2));

        //now invoke setters. iterating over getters (setters that do not have a corresponding getter will not be invoked)
        definedSourceGetters.entrySet().stream()
                // Only non null attributes
                .filter(entry -> Objects.nonNull(entry.getKey()))
                .forEach(entry -> {
            try {
                ReflectionUtils.invokeSetter(destination, entry.getKey(), entry.getValue().invoke(source));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MappingException(Collections.emptyList());
            }
                });

        return destination;
    }


    @Override
    public ConditionalConverter.MatchResult match(Class<?> aClass, Class<?> aClass1) {
        return BaseFilter.class.isAssignableFrom(aClass) && BaseFilter.class.isAssignableFrom(aClass1) ? MatchResult.FULL : ConditionalConverter.MatchResult.NONE;
    }
}
