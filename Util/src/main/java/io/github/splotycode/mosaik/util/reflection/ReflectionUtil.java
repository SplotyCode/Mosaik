package io.github.splotycode.mosaik.util.reflection;

import io.github.splotycode.mosaik.util.AlmostBoolean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ReflectionUtil {

    private static CallerClass callerClazz = new CallerClass();
    private static Unsafe unsafe;

    public static Unsafe getUnsafe() {
        if (unsafe == null) {
            Field f;
            try {
                f = Unsafe.class.getDeclaredField("theUnsafe");
                f.setAccessible(true);
                unsafe = (Unsafe) f.get(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                unsafe = null;
            }
        }
        return unsafe;
    }

    /**
     * @deprecated use {@link ClassCollector} instead
     */
    @Deprecated
    public static boolean validClass(Class<?> clazz, Class<?> other, boolean noAbstraction, boolean disableAnnotation) {
        /*return other.isAssignableFrom(clazz) &&
                !clazz.isInterface() && !clazz.isEnum() &&
                (!noAbstraction || !Modifier.isAbstract(clazz.getModifiers())) &&
                (!disableAnnotation || !clazz.isAnnotationPresent(Disabled.class));*/
        return ClassCollector.newInstance()
                .setAbstracation(AlmostBoolean.fromBoolean(!noAbstraction))
                .setOnlyClasses(true)
                .setNoDisableds(disableAnnotation)
                .setNeedAssignable(other).test(clazz);
    }

    public static Class<?> getCallerClass() {
        return callerClazz.getClassContext()[3];
    }

    public static Class<?> getCallerClass(int deapth) {
        return callerClazz.getClassContext()[3 + deapth];
    }

    public static Class<?>[] getCallerClasses() {
        return callerClazz.getClassContext();
    }

    public static List<Field> getAllFields(Class clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static Set<Method> getAllMethods(Class clazz) {
        Set<Method> methods = new HashSet<>();
        while (clazz != Object.class) {
            collectMethodsInInterfaces(true, clazz, methods);
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
            clazz = clazz.getSuperclass();
        }
        return methods;
    }

    private static void collectMethodsInInterfaces(boolean base, Class clazz, Set<Method> methods) {
        for (Class inter : clazz.getInterfaces()) {
            collectMethodsInInterfaces(false, inter, methods);
        }
        if (!base) {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        }
    }

    public static Field getField(Class clazz, String name) {
        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Method getMethod(Class clazz, String name, Class<?>... parameters) {
        while (clazz != Object.class) {
            try {
                return clazz.getDeclaredMethod(name, parameters);
            } catch (NoSuchMethodException e) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private static class CallerClass extends SecurityManager {

        @Override
        public Class[] getClassContext() {
            return super.getClassContext();
        }

    }

    public static Type[] getGenerics(Class clazz) {
        return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
    }

    public static Type[] getGenerics(Class clazz, int interfaceUpper) {
        return ((ParameterizedType) clazz.getGenericInterfaces()[interfaceUpper]).getActualTypeArguments();
    }

    public static boolean isAssignable(Class<?> parent, Class<?> child) {
        return parent.isAssignableFrom(child) || samePrimitive(parent, child);
    }

    public static boolean samePrimitive(Class<?> one, Class<?> two) {
        return samePrimitive(one, two, "int", "Integer") ||
               samePrimitive(one, two, "float", "Float") ||
               samePrimitive(one, two, "double", "Double") ||
               samePrimitive(one, two, "char", "Character") ||
               samePrimitive(one, two, "long", "Long") ||
               samePrimitive(one, two, "short", "Short") ||
               samePrimitive(one, two, "boolean", "Boolean");
    }

    private static boolean samePrimitive(Class one, Class two, String primiClass, String clazz) {
        String oneName = one.getSimpleName(),
               twoName = two.getSimpleName();
        if (one.isPrimitive() && oneName.equalsIgnoreCase(primiClass) && twoName.equalsIgnoreCase(clazz)) return two.isArray() == one.isArray();
        if (two.isPrimitive() && twoName.equalsIgnoreCase(primiClass) && oneName.equalsIgnoreCase(clazz)) return two.isArray() == one.isArray();
        return false;
    }

    public static boolean methodExists(Class<?> clazz, String method) {
        try {
            clazz.getMethod(method);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static boolean clazzExists(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
