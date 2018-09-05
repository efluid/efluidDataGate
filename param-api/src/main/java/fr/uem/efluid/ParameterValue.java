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
 * Specification of a <b>dictionary entry</b> <b>value</b> from a component class. Can be
 * specified on a field or on a getter method. The column name can be specified, or a
 * default name will be set (field name or extracted property name from method name).
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
@Inherited
public @interface ParameterValue {

	/**
	 * <p>
	 * Alias for {@link #name()}
	 * </p>
	 * 
	 * @return
	 */
	String value() default "";

	/**
	 * <p>
	 * Name of a column mapped by the current {@link ParameterValue}. If not set, will use
	 * the field name
	 * </p>
	 * 
	 * @return
	 */
	String name() default "";

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
