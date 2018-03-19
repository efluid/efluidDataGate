package fr.uem.efluid;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Definition of a <b>dictionnary entry</b>. Specified on any component which can be
 * represented as a parameter <b>table</b>. The table name and functional name must be
 * specified for the <tt>ParameterTable</tt> annotation
 * </p>
 * <p>
 * A <b>filter clause</b> can be also specified : else a default value of "1=1" will be
 * used, which means that all values of the parameter table will be managed.
 * </p>
 * <p>
 * A domain must be specified, using one of these solution :
 * <ul>
 * <li>A domain name can be specified directly for the parameter table with
 * {@link #domainName()}. As it is a refered value, take care of its content</li>
 * <li>For improved consistency, the domain can be also specified using a custom
 * annotation annotated with the meta annotation {@link ParameterDomain} : All
 * <tt>ParameterTable</tt> annotated with it will be associated to the domain specified
 * once for all in {@link ParameterDomain#value()}</li>
 * <li>For simplicity, the domain can be also specified at package level, once again with
 * annotation {@link ParameterDomain} : All <tt>ParameterTable</tt> from the same package
 * will be associated to the domain specified once for all in
 * {@link ParameterDomain#value()}</li>
 * </ul>
 * </p>
 * <p>
 * Properties can be inherited : all annotated inherited properties are "added" to current
 * type definition. But the annotation itself is <b>not</b> inherited
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(CLASS)
@Target(TYPE)
public @interface ParameterTable {

	/**
	 * <p>
	 * Specify the business name for the parameter table (how it will be identified in
	 * Dictionnary management). Mandatory and unique : if the value is not unique, the
	 * dictionary build will fail
	 * </p>
	 * 
	 * @return a specified business name for the parameter table in the dictionary
	 */
	String name();

	/**
	 * <p>
	 * Specify the technical table name for the parameter table. Mandatory. Please use
	 * UPPERCASE value only
	 * </p>
	 */
	String tableName();

	/**
	 * <p>
	 * Custom filter clause to apply at SQL level for data filtering. Default is "1=1",
	 * meaning "get all lines from the table". See dictionary management rules for custom
	 * filter
	 * </p>
	 * </p>
	 * 
	 * @return
	 */
	String filterClause() default "1=1";

	/**
	 * <p>
	 * Domain to associate to the parameter table. Mandatory if not specified with custom
	 * annotation or at package level
	 * </p>
	 * 
	 * @return
	 */
	String domainName() default "";

}
