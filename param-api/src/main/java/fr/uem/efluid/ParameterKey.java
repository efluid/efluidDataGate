package fr.uem.efluid;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specification of the <b>dictionnary entry</b> <b>key</b> from a component class. Can be
 * specified on a field or on a getter method. The column name must be specified.
 * </p>
 * <p>
 * The key is associated to a <tt>ColumnType</tt>, which will be identified automatically
 * regarding the field or return type of the annotated element
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface ParameterKey {

	/**
	 * <p>
	 * Mandatory column name for the key
	 * </p>
	 * 
	 * @return
	 */
	String value();

	/**
	 * <p>
	 * When the type cannot be defined automatically, specify it directly with this
	 * parameter. Useful for "non standard" property types. If not set, will define it
	 * using rules specified in {@link ColumnType#forClass(Class)}
	 * </p>
	 * 
	 * @return
	 */
	ColumnType type() default ColumnType.UNKNOWN;
}
