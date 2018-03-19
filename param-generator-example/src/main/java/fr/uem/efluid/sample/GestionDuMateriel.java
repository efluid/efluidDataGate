package fr.uem.efluid.sample;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import fr.uem.efluid.ParameterDomain;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
@ParameterDomain("Gestion du materiel")
public @interface GestionDuMateriel {

	// Meta-annotation, no content
}
