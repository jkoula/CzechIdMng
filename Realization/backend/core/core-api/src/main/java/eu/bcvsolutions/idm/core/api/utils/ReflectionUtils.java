package eu.bcvsolutions.idm.core.api.utils;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

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
import java.util.stream.Collectors;

/**
 * @author Peter Štrunc <github.com/peter-strunc>
 */
public class ReflectionUtils {

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

    public static String getLowercaseFieldNameFromGetter(Method setter) {
        if (!setter.getName().startsWith("get")) {
            // not a setter
            return null;
        }
        return setter.getName().substring(3).toLowerCase();
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
                .filter(method -> method.getName().startsWith("get"))
                .collect(Collectors.toSet());
    }

    public static void invokeSetter(Object destination, String key, Object value) {
        final Optional<Method> setter = getAllSetterMethods(destination.getClass()).stream()
                .filter(method -> key.equals(getLowercaseFieldNameFromSetter(method)))
                .findFirst();

        setter.ifPresent(method -> {
            try {
                method.invoke(destination, value);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
    }


}
