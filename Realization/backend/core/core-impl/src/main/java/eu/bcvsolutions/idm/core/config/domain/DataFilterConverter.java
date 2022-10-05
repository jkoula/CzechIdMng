package eu.bcvsolutions.idm.core.config.domain;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ReflectionUtils;
import org.modelmapper.spi.ConditionalConverter;
import org.modelmapper.spi.MappingContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static eu.bcvsolutions.idm.core.api.utils.ReflectionUtils.getAllDeclaredFieldsSuperClassAndInterface;
import static eu.bcvsolutions.idm.core.api.utils.ReflectionUtils.instantiateUsingNoArgConstructor;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class DataFilterConverter implements ConditionalConverter<DataFilter, DataFilter> {

    @Override
    public DataFilter convert(MappingContext<DataFilter, DataFilter> mappingContext) {
        final DataFilter source = mappingContext.getSource();
        final Class<DataFilter> destinationType = mappingContext.getDestinationType();
        final DataFilter destination = instantiateUsingNoArgConstructor(destinationType, new DataFilter(source.getDtoClass()));

        final Collection<Field> alldestSuperFields = getAllDeclaredFieldsSuperClassAndInterface(destinationType);

        // property name -> setter method
        final Map<String, Method> definedDestinationSetterKeys =
                ReflectionUtils.getAllSetterMethods(destinationType).stream()
                        .collect(Collectors.toMap(ReflectionUtils::getLowercaseFieldNameFromSetter, i -> i, (t, t2) -> t2));

        // property name -> getter method
        final Map<String, Method> definedSourceGetters = ReflectionUtils.getAllGetterMethods(mappingContext.getSourceType()).stream()
                .collect(Collectors.toMap(ReflectionUtils::getLowercaseFieldNameFromGetter, i -> i, (t, t2) -> t2));

        /*
        MultiValueMap<String, Object> filterredMap = new LinkedMultiValueMap<>();
        source.getData().entrySet().stream().filter(entry -> {
                    final String toFind = entry.getKey().toLowerCase();
                    return definedDestinationSetterKeys.contains(toFind) || definedDestinationFields.contains(toFind);
                })

                .forEach(entry -> filterredMap.put(entry.getKey(), entry.getValue()));


        destination.putData(filterredMap);


         */
        //now invoke setters. iterating over getters (setters that do not have a corresponding getter will not be invoked)
        definedSourceGetters.entrySet().stream()
                // Only non null attributes
                .filter(entry -> Objects.nonNull(entry.getKey()))
                .forEach(entry -> {
            try {
                ReflectionUtils.invokeSetter(destination, entry.getKey(), entry.getValue().invoke(source));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });


        return destination;
    }


    @Override
    public ConditionalConverter.MatchResult match(Class<?> aClass, Class<?> aClass1) {
        return DataFilter.class.isAssignableFrom(aClass) && DataFilter.class.isAssignableFrom(aClass1) ? ConditionalConverter.MatchResult.FULL : ConditionalConverter.MatchResult.NONE;
    }
}
