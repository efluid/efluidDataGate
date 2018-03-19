package fr.uem.efluid;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specify a <b>link</b> from a mapped property <b>value</b> (annotated with
 * {@link ParameterValue}) to another table or {@link ParameterTable}. The link can be
 * defined by a java type, which must be annotated itself with
 * <code>&#64;ParameterTable</code>, or by a manually set table name. If type or table
 * name are not set, the linked parameter type will be assumed to be the annotated field /
 * method type.
 * </p>
 * <p>
 * The property annotated with <code>&#64;ParameterLink</code> must be itself annotated
 * with {@link ParameterValue} to be used.
 * </p>
 * <p>
 * See dictionary management rules for details on links
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(CLASS)
@Target({ FIELD, METHOD })
public @interface ParameterLink {

	/**
	 * <p>
	 * Specify the associated parameter table from a class annotated with
	 * {@link ParameterTable}
	 * </p>
	 * 
	 * @return
	 */
	Class<?> toParameter() default Void.class;

	/**
	 * <p>
	 * Instead of {@link #value()}, can be used to define the associated parameter table
	 * directly with the table name
	 * </p>
	 * 
	 * @return
	 */
	String toTableName() default "";

	/**
	 * <p>
	 * Name of a column linked by the current {@link ParameterValue}. Can be any column,
	 * not necessary a mapped column : for example it can be a technical id, when the link
	 * if defined on a foreign key
	 * </p>
	 * 
	 * @return
	 */
	String toColumn();
}
