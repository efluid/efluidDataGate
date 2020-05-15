package fr.uem.efluid.tests.autoUpload;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ProjectColor;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
@ParameterDomain(name = "My Domain", project = @ParameterProject(name = "My project", color = ProjectColor.BLUE))
public @interface MyDomain {

	// Meta-annotation, no content
}
