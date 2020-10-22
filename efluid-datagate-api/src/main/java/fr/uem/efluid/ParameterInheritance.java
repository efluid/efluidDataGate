package fr.uem.efluid;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Definition of an inheritance adaptation, generally for exclusion needs
 * Allows to select the inherited type to adapt for, and the fields from this type (default select all fields,
 * or the list of fields to ignore can be specified)
 *
 * @author elecomte
 * @version 1
 * @since v2.0.7
 */
@Documented
@Inherited
@Retention(RUNTIME)
public @interface ParameterInheritance {

    // Use self to specify an exclusion of ALL inherited types
    Class<?> ALL = ALL.class;

    /**
     * Specified type for inheritance. Mandatory
     *
     * @return class of the inheritance source config (can be anywhere in the hierarchy)
     */
    Class<?> of();

    /**
     * Specify a select of fields for this inheritance specification. If at least one is set, then only this field is selected.
     * If none is specified and for example this inheritance spec is for exclusion, this will exclude all fields
     *
     * @return list of specified fields to select for this inheritance specification. If none defined, then all of them will be selected
     */
    String[] fields() default {};

    class ALL {

    }
}
