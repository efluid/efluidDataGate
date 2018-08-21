package fr.uem.efluid;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * Specify a <b>map</b> (N-N link) for a field to another parameter table, defined through
 * a mapping table. The mapping table and table associated with current property must be
 * set.
 * </p>
 * <p>
 * The property annotated with <code>&#64;ParameterMap</code> must be itself annotated
 * with {@link ParameterValue} to be used.
 * </p>
 * <p>
 * See dictionary management rules for details on links and maps
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface ParameterMapping {

	/**
	 * <p>
	 * Specify the associated parameter table from a Collection of class annotated with
	 * {@link ParameterTable}.
	 * </p>
	 * <p>
	 * For example, if the annotation <code>@ParameterMap</code> is specified as this :
	 * 
	 * <pre>
	 * &#64;ParameterValue("ENT_KEY")
	 * private String key;
	 * 
	 * &#64;ParameterMap(mapTableName = "MY_SUBS", toColumn = "SUB_ID")
	 * private Set&lt;SubEntity&gt; entities;
	 * </pre>
	 * 
	 * Then it will use the parameter table specified for type <code>SubEntity</code>,
	 * with the id "ID", and manage it as a mapped parameter through mapping table
	 * "my_subs" with columns "ENT_ID" and "SUB_ID" used for N-N association
	 * </p>
	 * </p>
	 * 
	 * @return
	 */
	Class<?> toParameter() default Void.class;

	/**
	 * <p>
	 * The N-N mapping table name. Mandatory. The table should have at least two columns :
	 * one referring current parameter ("from" column) and one referring the mapped_with
	 * property ("to" column). It is possible to use default rules for these column names,
	 * or to specify them with properties {@link #mapColumnFrom()} and
	 * {@link #mapColumnTo()}
	 * </p>
	 * 
	 * @return
	 */
	String mapTableName();

	/**
	 * <p>
	 * In the N-N mapping table, specify the name of the associated "from" column if
	 * different than the current property column name. If not set, will use current
	 * property value rules for column naming
	 * </p>
	 * 
	 * @return
	 */
	String mapColumnFrom() default "";

	/**
	 * <p>
	 * In the N-N mapping table, specify the name of the associated "to" column,
	 * associated with current property, if different than the one defined with
	 * {@link #toColumn()}
	 * </p>
	 * 
	 * @return
	 */
	String mapColumnTo() default "";

	/**
	 * <p>
	 * Define the associated parameter table directly with the table name, if cannot be
	 * found from associated type. Corresponds to the mapped table associated with current
	 * property by the mapping (N-N) table
	 * </p>
	 * 
	 * @return
	 */
	String toTableName() default "";

	/**
	 * <p>
	 * Name of a column linked by the current {@link ParameterValue} and mapped with the
	 * current property. Can be any column, not necessary a mapped column : for example it
	 * can be a technical id, when the link if defined on a foreign key. If not specified,
	 * will use the identified KEY from the mapped parameter table. Take care that the KEY
	 * is not always the natural ID used in table mapping
	 * </p>
	 * 
	 * @return
	 */
	String toColumn() default "";
}
