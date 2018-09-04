package fr.uem.efluid;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface ParameterTableSet {

	/**
	 * @return
	 */
	ParameterTable[] value();
}
