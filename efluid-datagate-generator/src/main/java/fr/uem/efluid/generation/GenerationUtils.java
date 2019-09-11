package fr.uem.efluid.generation;

import fr.uem.efluid.*;
import fr.uem.efluid.utils.RuntimeValuesUtils;
import org.reflections.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

/**
 * Some common tools for generation
 */
@SuppressWarnings("unchecked")
class GenerationUtils {

    static String failback(String property, String failover, String failoverFailOver) {
        if (property == null || property.trim().length() == 0) {
            return failback(failover, failoverFailOver);
        }
        return property;
    }

    static String failback(String property, String failover) {
        if (property == null || property.trim().length() == 0) {
            return failover;
        }
        return property;
    }

    static Set<Field> searchFields(Class<?> tableType, boolean useAll) {
        return ReflectionUtils.getAllFields(tableType, f -> isFieldValue(f, useAll));
    }

    static Set<Field> searchAnnotatedFields(Class<?> tableType, Class<? extends Annotation> annot) {
        return ReflectionUtils.getAllFields(tableType, f -> f != null && f.isAnnotationPresent(annot));
    }

    static Set<Method> searchAnnotatedMethods(Class<?> tableType, Class<? extends Annotation> annot) {
        return ReflectionUtils.getAllMethods(tableType, f -> f != null && f.isAnnotationPresent(annot));
    }

    static Set<Method> searchMethods(Class<?> tableType) {
        return ReflectionUtils.getAllMethods(tableType, GenerationUtils::isMethodValue);
    }


    /**
     * <p>
     * Select clause for fields used as value properties in a <tt>ParameterTable</tt>
     * </p>
     *
     * @param field
     * @param useAll
     * @return
     */
    static boolean isFieldValue(Field field, boolean useAll) {

        // All fields When the table is enabled "useAllFields"
        return (useAll

                // If not "useAllFields", keep only @ParameterValue fields or link or
                // mappings
                || (field.isAnnotationPresent(ParameterValue.class) || field.isAnnotationPresent(ParameterCompositeValue.class)
                || field.isAnnotationPresent(ParameterLink.class) || field.isAnnotationPresent(ParameterMapping.class)))

                // Exclude ignored with @ParameterIgnored or specified as key with
                // @ParameterKey
                && !(field.isAnnotationPresent(ParameterIgnored.class) || field.isAnnotationPresent(ParameterKey.class));
    }

    /**
     * <p>
     * Select clause for methods used as value properties in a <tt>ParameterTable</tt>
     * </p>
     *
     * @param method
     * @return
     */
    static boolean isMethodValue(Method method) {
        return
                /*
                 * All methods specified as values, link or mappings, and not ignored or key
                 */
                (method.isAnnotationPresent(ParameterValue.class)
                        || method.isAnnotationPresent(ParameterCompositeValue.class)
                        || method.isAnnotationPresent(ParameterLink.class)
                        || method.isAnnotationPresent(ParameterMapping.class))
                        && !(method.isAnnotationPresent(ParameterIgnored.class)
                        || method.isAnnotationPresent(ParameterKey.class));
    }

    // 565c1448-6c74-4580-9595-6ee58817d985
    // 8 4 4 4 12
    // 32
    static UUID generateFixedUUID(String refValue, Class<?> refType) {
        String refHas = Integer.toHexString(refValue.hashCode());
        String refTyp = Integer.toHexString(refType.getName().hashCode());
        int miss = 32 - (refHas.length() + refTyp.length());
        if (miss < 0) {
            throw new IllegalArgumentException(
                    "Cannot process as UUID values " + refValue + " / " + refType.getName() + ". Got " + refHas + "-" + refTyp);
        }

        char[] complete = new char[miss];
        Arrays.fill(complete, '0');

        String raw = new StringBuilder(32).append(refTyp).append(complete).append(refHas).toString();

        return RuntimeValuesUtils.loadUUIDFromRaw(raw);
    }

}
