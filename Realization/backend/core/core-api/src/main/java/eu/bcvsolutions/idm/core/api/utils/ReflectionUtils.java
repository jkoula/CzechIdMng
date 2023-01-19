package eu.bcvsolutions.idm.core.api.utils;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import org.springframework.context.annotation.Primary;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class ReflectionUtils {

    public static final String GETTER_REGEX_PATTERN = "^(is|get)([A-Z].*)";
    public static final Pattern GETTER_COMPILED_PATTERN = Pattern.compile(GETTER_REGEX_PATTERN);

    public static Set<Method> getAllDeclaredMethodsSuperClassAndInterface(Class<?> clazz) {
        Set<Method> result = new HashSet<>(List.of(clazz.getDeclaredMethods()));

        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(clazz)) {
            result.addAll(getAllDeclaredMethodsSuperClassAndInterface(superclass));
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            result.addAll(getAllDeclaredMethodsSuperClassAndInterface(iface));
        }

        return result;
    }

    public static String getLowercaseFieldNameFromSetter(Method setter) {
        if (!setter.getName().startsWith("set")) {
            // not a setter
            return null;
        }
        return setter.getName().substring(3).toLowerCase();
    }

    public static String getLowercaseFieldNameFromGetter(Method getter) {
        Matcher matcher = GETTER_COMPILED_PATTERN.matcher(getter.getName());
        return matcher.find() ? matcher.group(2).toLowerCase() : null;
    }

    public static Collection<Field> getAllDeclaredFieldsSuperClassAndInterface(Class<?> clazz) {
        Set<Field> result = new HashSet<>(List.of(clazz.getDeclaredFields()));

        final Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && !superclass.equals(clazz)) {
            result.addAll(getAllDeclaredFieldsSuperClassAndInterface(superclass));
        }

        for (Class<?> iface : clazz.getInterfaces()) {
            result.addAll(getAllDeclaredFieldsSuperClassAndInterface(iface));
        }

        return result;
    }

    public static boolean isZeroArgConstructor(Constructor<?> c) {
        return c.getParameterCount() == 0;
    }

    public static <E> E instantiateUsingNoArgConstructor(Class<E> type, E defaultValue) {
        return Arrays.stream(type.getConstructors()).filter(ReflectionUtils::isZeroArgConstructor).map(constructor -> {
            try {
                return (E) constructor.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }).findFirst().orElse(defaultValue);
    }

    public static Collection<Method> getAllSetterMethods(Class<?> clazz) {
        return getAllDeclaredMethodsSuperClassAndInterface(clazz).stream()
                .filter(method -> method.getName().startsWith("set"))
                .collect(Collectors.toSet());
    }

    public static Collection<Method> getAllGetterMethods(Class<?> clazz) {
        return getAllDeclaredMethodsSuperClassAndInterface(clazz).stream()
                .filter(method -> method.getName().matches(GETTER_REGEX_PATTERN))
                .collect(Collectors.toSet());
    }

    public static void invokeSetter(Object destination, String key, Object value) {
        final Optional<Method> setter = getAllSetterMethods(destination.getClass()).stream()
                .filter(method -> key.toLowerCase().equals(getLowercaseFieldNameFromSetter(method)))
                .findFirst();

        setter.ifPresent(method -> {
            try {
                method.invoke(destination, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public static Object invokeGetter(Object o, String key) {
        final Optional<Method> getter = getAllGetterMethods(o.getClass()).stream()
                .filter(method -> key.toLowerCase().equals(getLowercaseFieldNameFromGetter(method)))
                .findFirst();

        return getter.map(method -> {
            try {
                return method.invoke(o);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }).orElse(null);
    }

    public static <A> A translateFilter(DataFilter filter, Class<A> filterClass) {
        final A innerfilter = ReflectionUtils.instantiateUsingNoArgConstructor(filterClass, null);
        if (filter == null) {
            return innerfilter;
        }
        // Set values using data map
        final MultiValueMap<String, Object> data = filter.getData();
        data.forEach((key, value) -> invokeSetter(innerfilter, key, toSingleValue(value)));

        // Set values using getters
        // This is a fallback for filters which combine DataFilter with internal fields
        ReflectionUtils.getAllGetterMethods(filter.getClass())
                .forEach(getter -> {
                    final String getterKey = getLowercaseFieldNameFromGetter(getter);
                    final Object valueToSet = invokeGetter(filter, getterKey);
                    invokeSetter(innerfilter, getterKey, valueToSet);
                });

        return innerfilter;
    }

    public static <E> E resolveMultipleServices(E s1, E s2) {
        if (s1.getClass().isAnnotationPresent(Primary.class)) {
            return s1;
        }
        return s2;
    }

    private static Object toSingleValue(List<Object> value) {
        return value.stream().findFirst().orElse(null);
    }
}
