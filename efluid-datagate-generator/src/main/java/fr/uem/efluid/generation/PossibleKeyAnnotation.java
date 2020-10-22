package fr.uem.efluid.generation;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * <p>
 * Candidate for key identification. PRocess name / type detection for available field
 * / method or direct annotation values
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
class PossibleKeyAnnotation extends PossibleItem {

    private final ParameterKey keyAnnot;
    private final String validName;
    private final ColumnType validType;
    private final Collection<String> forTable;

    PossibleKeyAnnotation(Method method) {
        this(method, null);
    }

    PossibleKeyAnnotation(Method method, String name) {

        super(method.getDeclaringClass());
        this.keyAnnot = method.getAnnotation(ParameterKey.class);
        // If not set on ParameterValue annotation, uses specified name, and then failback on method name
        this.validName = this.keyAnnot != null && !"".equals(this.keyAnnot.value()) ? this.keyAnnot.value().toUpperCase()
                : (name != null ? name.toUpperCase() : method.getName().toUpperCase());
        this.validType = this.keyAnnot != null && ColumnType.UNKNOWN != this.keyAnnot.type() ? this.keyAnnot.type()
                : ColumnType.forClass(method.getReturnType());
        this.forTable = this.keyAnnot != null && this.keyAnnot.forTable().length > 0 ? Arrays.asList(this.keyAnnot.forTable())
                : null;
    }

    PossibleKeyAnnotation(Field field) {
        super(field.getDeclaringClass());
        this.keyAnnot = field.getAnnotation(ParameterKey.class);
        // If not set on ParameterValue annotation, uses field name
        this.validName = this.keyAnnot != null && !"".equals(this.keyAnnot.value()) ? this.keyAnnot.value().toUpperCase()
                : field.getName().toUpperCase();
        // Type must be found from generic collection
        this.validType = this.keyAnnot != null && ColumnType.UNKNOWN != this.keyAnnot.type() ? this.keyAnnot.type()
                : ColumnType.forClass(field.getType());
        this.forTable = this.keyAnnot != null && this.keyAnnot.forTable().length > 0 ? Arrays.asList(this.keyAnnot.forTable())
                : null;
    }

    PossibleKeyAnnotation(Class<?> declaringClazz, PossibleTableAnnotation possibleTableAnnotation) {
        super(declaringClazz);
        this.keyAnnot = null;
        this.validName = possibleTableAnnotation.getKeyField().toUpperCase();
        this.validType = possibleTableAnnotation.getKeyType();
        this.forTable = Collections.singletonList(possibleTableAnnotation.getValidName());
    }

    /**
     * @return the validName
     */
    public String getValidName() {
        return this.validName;
    }

    /**
     * @return the validType
     */
    public ColumnType getValidType() {
        return this.validType;
    }

    /**
     * @param name
     * @return
     */
    public boolean isCompliantTable(String name) {
        return this.forTable == null || this.forTable.contains(name);
    }


    boolean canKeepInType(Class<?> type) {
        return getSourceClazz().equals(type)
                || (this.keyAnnot == null || !this.keyAnnot.notInherited());
    }
}
