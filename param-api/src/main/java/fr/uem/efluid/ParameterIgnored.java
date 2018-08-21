package fr.uem.efluid;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Excludes annotated element from automatic embedding as standard property / table value
 * regarding predefined rules.
 * </p>
 * <p>
 * <b>The predefined rules which can be ignored with this annotation are</b> :
 * <ul>
 * <li>Default mapped inherited <code>ParameterTable</code>. A class inheriting another
 * specified as table will be, by default, also specified as a table. Ignoring it will
 * exclude it from mapping</li>
 * <li>Default mapped value in a <code>ParameterTable</code> where
 * <code>useAllFields</code> is true. Any field can be specified as ignored to exclude it
 * from automatic mapping</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
@Inherited
public @interface ParameterIgnored {

	// No content
}
