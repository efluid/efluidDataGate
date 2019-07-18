package fr.uem.efluid;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotation for definition of a functional domain. Can be specified two ways :
 * <ul>
 * <li>As a package annotation : all the items annotated with {@link ParameterTable}
 * within the same package are then automatically supposed to be associated to this
 * domain</li>
 * <li>As a meta-annotation to define a custom "domain - centric" annotation, like in this
 * example :
 * 
 * <pre>
 * &#64;Documented
 * &#64;Retention(CLASS)
 * &#64;Target(TYPE)
 * &#64;ParameterDomain("Gestion du materiel")
 * public &#64;interface GestionDuMateriel {
 * 	// Meta-annotation, no content
 * }
 * </pre>
 * 
 * Then all the items annotated with this custom annotation plus {@link ParameterTable}
 * are automatically associated to the domain</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target({ ANNOTATION_TYPE, PACKAGE })
@Inherited
public @interface ParameterDomain {

	/**
	 * <p>
	 * Mandatory spec of the domain name. Will be created with this exact name
	 * </p>
	 * <p>
	 * Note than in some cases the name can be used to identify an item in the dictionnary
	 * </p>
	 * 
	 * @return
	 */
	String name();

	/**
	 * <p>
	 * Specification of project for current domain. Project are unique by name
	 * </p>
	 * 
	 * @return
	 */
	ParameterProject project();
}
