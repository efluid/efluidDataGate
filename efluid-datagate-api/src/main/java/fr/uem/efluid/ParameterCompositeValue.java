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
 * Specification of a <b>dictionary entry</b> <b>value</b> which is a composite of various
 * column.
 * </p>
 * <p>
 * <b>Supported only with combined <tt>ParameterLink</tt> and
 * <tt>ParameterMapping</tt>.</b>
 * </p>
 * <p>
 * In this case the value is mandatory as the property cannot be identified from annotated
 * field / method. Take care of the order of the column names, which must be the same as
 * referenced Link / mapping "to" columns
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
@Documented
@Retention(RUNTIME)
@Target({FIELD, METHOD})
@Inherited
public @interface ParameterCompositeValue {

    /**
     * @return alias of {@link #names()}
     */
    String[] value() default {};

    /**
     * @return Name of a column mapped by the current {@link ParameterValue}. If not set, will use the field name
     */
    String[] names() default {};

    /**
     * @return When the current parameter table is specified in a <tt>ParameterTableSet</tt> use this attribute to refer the corresponding table for current value
     */
    String[] forTable() default {};
}
