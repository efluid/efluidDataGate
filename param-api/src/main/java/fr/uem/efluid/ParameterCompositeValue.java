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
	 * Name of the columns mapped by the current {@link ParameterCompositeValue}, and used
	 * as composite
	 * </p>
	 * 
	 * @return
	 */
	String[] value();
}
