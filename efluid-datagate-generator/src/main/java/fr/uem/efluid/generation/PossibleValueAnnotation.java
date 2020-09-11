package fr.uem.efluid.generation;

import fr.uem.efluid.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * <p>
 * Values are identified in various way :
 * <ul>
 * <li>Fields annotated with ParameterValue if table is not set to automatically
 * include all fields as values</li>
 * <li>All fields excepting the ones annotated with <tt>ParameterIgnored</tt> when the
 * table is set to include all</li>
 * <li>Methods annotated with <tt>ParameterValue</tt></li>
 * <li>Inclusions of other value-related annotation : <tt>ParameterLink</tt> and
 * <tt>ParameterMapping</tt></li>
 * </ul>
 * As the source for values are various and the available information depend on
 * different conditions, <b>this inner component represent a candidate for value +
 * link / mapping process</b>. It includes the required data for value, link and
 * mapping specification, and process differently <tt>Method</tt> and <tt>Field</tt>
 * to produce the same exact properties
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
class PossibleValueAnnotation {

    private final ParameterValue valueAnnot;
    private final ParameterCompositeValue compositeValueAnnot;
    private final String validName;
    private final Class<?> validType;
    private final ParameterLink linkAnnot;
    private final ParameterMapping mappingAnnot;
    private final String[] compositeNames;
    private final Collection<String> forTable;
    private final Class<?> sourceClazz;

    // Detected on method
    PossibleValueAnnotation(Method method, ClassLoader contextClassLoader) {

        this.valueAnnot = method.getAnnotation(ParameterValue.class);
        this.compositeValueAnnot = method.getAnnotation(ParameterCompositeValue.class);
        this.linkAnnot = method.getAnnotation(ParameterLink.class);
        // If not set on ParameterValue annotation, uses method name
        this.validName = prepareValidName(this.valueAnnot, this.linkAnnot ,method.getName());
        this.mappingAnnot = method.getAnnotation(ParameterMapping.class);
        this.validType = this.mappingAnnot != null ? getMappingType(method, contextClassLoader) : method.getReturnType();
        this.compositeNames = prepareCompositeNames(this.compositeValueAnnot);
        this.forTable = prepareValidForTable(this.valueAnnot, this.compositeValueAnnot);
        this.sourceClazz = method.getDeclaringClass();
    }

    // Detected on field
    PossibleValueAnnotation(Field field, ClassLoader contextClassLoader) {

        this.valueAnnot = field.getAnnotation(ParameterValue.class);
        this.compositeValueAnnot = field.getAnnotation(ParameterCompositeValue.class);
        this.linkAnnot = field.getAnnotation(ParameterLink.class);
        // If not set on ParameterValue annotation, uses field name
        this.validName = prepareValidName(this.valueAnnot, this.linkAnnot , field.getName());
        this.mappingAnnot = field.getAnnotation(ParameterMapping.class);
        // Type must be found from generic collection
        this.validType = this.mappingAnnot != null ? getMappingType(field, contextClassLoader) : field.getType();
        this.compositeNames = prepareCompositeNames(this.compositeValueAnnot);
        this.forTable = prepareValidForTable(this.valueAnnot, this.compositeValueAnnot);
        this.sourceClazz = field.getDeclaringClass();

    }

    // Declared directly into ParameterTable annot
    PossibleValueAnnotation(Class<?> declaringClazz, ParameterValue directValueSpec, String tableName) {

        this.valueAnnot = directValueSpec;
        this.compositeValueAnnot = null;
        // If not set on ParameterValue annotation, uses field name
        this.validName = prepareValidName(this.valueAnnot, null, null);
        this.linkAnnot = null;
        this.mappingAnnot = null;
        this.validType = null;
        this.compositeNames = null;
        this.forTable = Arrays.asList(tableName);
        this.sourceClazz = declaringClazz;
    }

    String getValidName() {
        return this.validName;
    }

    Class<?> getValidType() {
        return this.validType;
    }

    /**
     * @return the linkAnnot
     */
    ParameterLink getLinkAnnot() {
        return this.linkAnnot;
    }

    /**
     * @return the mappingAnnot
     */
    ParameterMapping getMappingAnnot() {
        return this.mappingAnnot;
    }

    boolean isComposite() {
        // Rare
        return this.compositeValueAnnot != null;
    }

    String[] getCompositeNames() {
        return this.compositeNames;
    }

    boolean isCompliantTable(String name) {
        return this.forTable == null || this.forTable.contains(name);
    }

    boolean isExcluded(List<ParameterInheritance> inheritances) {

        if (inheritances.size() > 0) {
            return inheritances.stream().anyMatch(t ->
                    t.of() == this.sourceClazz
                            && (t.fields().length == 0 || Arrays.asList(t.fields()).contains(this.validName))
            );
        }

        return false;
    }

    private static Class<?> getMappingType(Method method, ClassLoader contextClassLoader) {

        if (method.getReturnType().equals(Void.TYPE)) {

            throw new IllegalArgumentException("The void method " + method.getName()
                    + " is annotated with @ParameterMapping. Associated type cannot be found, you must set mapping definition on Set Of fields / methods");
        }

        try {
            return getMappingCompliantType(method.getGenericReturnType(), method.getReturnType(), contextClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("The inner generic type for method " + method.getDeclaringClass() + "."
                    + method.getName() + " annotated with @ParameterMapping cannot be initialized", e);
        }
    }

    private static Class<?> getMappingType(Field field, ClassLoader contextClassLoader) {

        try {
            return getMappingCompliantType(field.getGenericType(), field.getType(), contextClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("The inner generic type for field " + field.getDeclaringClass() + "." + field.getName()
                    + " annotated with @ParameterMapping cannot be initialized", e);
        }
    }

    private static Class<?> getMappingCompliantType(Type resultGenericType, Class<?> rawType, ClassLoader contextClassLoader)
            throws ClassNotFoundException {

        if (resultGenericType instanceof ParameterizedType) {
            Type[] args = ((ParameterizedType) resultGenericType).getActualTypeArguments();
            return Class.forName(args[0].getTypeName(), false, contextClassLoader);
        }

        // Basic type
        return rawType;
    }

    private static String prepareValidName(ParameterValue annot, ParameterLink linkAnnot, String holderName) {

        if (annot != null) {
            // Specified name
            if (!"".equals(annot.name())) {
                return annot.name();
            }

            // Value is an alias for name()
            if (!"".equals(annot.value())) {
                return annot.value();
            }
        } else if (linkAnnot != null){
            return linkAnnot.withValueName();
        }

        // If not found from annotation, use holderName
        return holderName.toUpperCase();
    }

    private static Collection<String> prepareValidForTable(ParameterValue valueAnnot, ParameterCompositeValue compAnnot) {

        // Can be from a value annot
        if (valueAnnot != null) {
            if (valueAnnot.forTable().length > 0) {
                return Arrays.asList(valueAnnot.forTable());
            }
        }

        // Or a composite value annot
        if (compAnnot != null) {
            if (compAnnot.forTable().length > 0) {
                return Arrays.asList(compAnnot.forTable());
            }
        }

        return null;
    }

    private static String[] prepareCompositeNames(ParameterCompositeValue compositeValueAnnot) {

        if (compositeValueAnnot != null) {

            if (compositeValueAnnot.names().length != 0) {
                return compositeValueAnnot.names();
            }

            // Support for alias
            if (compositeValueAnnot.value().length != 0) {
                return compositeValueAnnot.value();
            }
        }

        return new String[]{};
    }

    boolean canKeepInType(Class<?> type) {
        return this.sourceClazz.equals(type)
                || (this.valueAnnot == null || !this.valueAnnot.notInherited());
    }
}
