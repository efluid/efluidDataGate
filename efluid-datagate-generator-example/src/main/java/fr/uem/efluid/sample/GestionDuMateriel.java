package fr.uem.efluid.sample;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ProjectColor;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
@ParameterDomain(name = "Gestion du materiel", project = @ParameterProject(name = "My project", color = ProjectColor.BLUE))
public @interface GestionDuMateriel {

	// Meta-annotation, no content
}
