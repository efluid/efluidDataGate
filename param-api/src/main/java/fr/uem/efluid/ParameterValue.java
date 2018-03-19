package fr.uem.efluid;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specification of a <b>dictionnary entry</b> <b>value</b> from a component class. Can be
 * specified on a field or on a getter method. The column name must be specified.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface ParameterValue {

	/**
	 * <p>
	 * Name of a column mapped by the current {@link ParameterValue}. Mandatory
	 * </p>
	 * 
	 * @return
	 */
	String value();
}
