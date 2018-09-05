package fr.uem.efluid;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

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
 * @since v2.0.0
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@Inherited
public @interface ParameterCompositeValue {

	/**
	 * <p>
	 * alias of {@link #names()}
	 * </p>
	 * 
	 * @return
	 */
	String[] value() default {};

	/**
	 * <p>
	 * Name of a column mapped by the current {@link ParameterValue}. If not set, will use
	 * the field name
	 * </p>
	 * 
	 * @return
	 */
	String[] names() default {};

	/**
	 * <p>
	 * When the current parameter table is specified in a <tt>ParameterTableSet</tt> use
	 * this attribute to refer the corresponding table for current value
	 * </p>
	 * 
	 * @return
	 */
	String[] forTable() default {};
}
