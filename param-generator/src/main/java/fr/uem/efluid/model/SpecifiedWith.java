package fr.uem.efluid.model;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Documentation annotation : identify what is used to specify the entity
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface SpecifiedWith {

	/**
	 * @return
	 */
	Class<? extends Annotation> value();

}
