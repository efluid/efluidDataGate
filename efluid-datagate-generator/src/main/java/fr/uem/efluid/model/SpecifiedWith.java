package fr.uem.efluid.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * <p>
 * Documentation annotation : identify what is used to specify the entity
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Documented
@Retention(SOURCE)
@Target(TYPE)
public @interface SpecifiedWith {

    /**
     * @return associated API Annotation for entity definition
     */
    Class<? extends Annotation> value();

}
