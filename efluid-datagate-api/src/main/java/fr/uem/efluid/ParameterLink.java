package fr.uem.efluid;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Specify a <b>link</b> from a mapped property <b>value</b> (annotated with
 * <tt>ParameterValue</tt> to another table or <tt>ParameterTable</tt>. The link can be
 * defined by a java type, which must be annotated itself with
 * <code>&#64;ParameterTable</code>, or by a manually set table name. If type or table
 * name are not set, the linked parameter type will be assumed to be the annotated field /
 * method type.
 * </p>
 * <p>
 * The property annotated with <code>&#64;ParameterLink</code> must be itself annotated
 * with <tt>ParameterValue</tt> to be used.
 * </p>
 * <p>
 * In case of "composite-key" used for link association, you simply have to annotate each
 * "from" column field with a link referencing the same table / entity
 * </p>
 * <p>
 * See dictionary management rules for details on links
 * </p>
 *
 * @author elecomte
 * @version 3
 * @since v0.0.1
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD})
@Inherited
public @interface ParameterLink {

    /**
     * <p>
     * Specify the associated parameter table from a class annotated with
     * {@link ParameterTable}
     * </p>
     */
    Class<?> toParameter() default Void.class;

    /**
     * <p>Allows to specify a custom "from" column name when the link is specified on a field not directly annotated with @ParameterValue
     * (for example when the value is specified in @ParameterTable <code>values</code>)
     * </p><p>
     * <b>Will be ignored if a @ParameterValue is specified on the field direclty !</b>
     * </p>
     *
     * @return optional "forced" name for the corresponding value
     */
    String withValueName() default "";

    /**
     * <p>
     * Define the associated parameter table
     * directly with the table name
     * </p>
     */
    String toTableName() default "";

    /**
     * <p>
     * Name of a columns linked by the current {@link ParameterValue} or
     * {@link ParameterCompositeValue}. Can be any column, not necessary a mapped column :
     * for example it can be a technical id, when the link if defined on a foreign key. If
     * not specified, will use the identified KEY from the mapped parameter table.
     * </p>
     * <p>
     * In case of composite key with <tt>ParameterCompositeValue</tt>, the referenced
     * column names must be in the same order than the composite ref
     * </p>
     */
    String[] toColumn() default {};

    /**
     * <p>
     * Optional link name to use
     * </p>
     */
    String name() default "";

}
