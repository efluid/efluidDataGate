package fr.uem.efluid;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p>
 * In some cases one entity class can be mapped onto multiple database tables. To define
 * all the tables associated to one type, use this <tt>ParameterTableSet</tt> and specify
 * details on each tables. The values and keys can be then specified using the attribute
 * {@link ParameterValue#forTable()}, or can be specified directly into
 * {@link ParameterTable} annotation
 * </p>
 * <p>
 * <b>Here an example of use with <code>forTable</code> reference (or auto-set reference)
 * :</b>
 * 
 * <pre>
 * &#64;ParameterTableSet({
 *       &#64;ParameterTable(tableName="T_TABLE_ONE"),
 *       &#64;ParameterTable(tableName="T_TABLE_TWO"),
 *       &#64;ParameterTable(tableName="T_TABLE_THREE")
 * })
 * public class TypeOnMultipleTablesFirst {
 * 
 *       &#64;ParameterKey // Si pas indiqué => Commun à toutes les tables du set
 *       private Long key;
 *       
 *       &#64;ParameterValue // Si pas indiqué => Commun à toutes les tables du set
 *       private String valueOnAll;
 *       
 *       &#64;ParameterValue(forTable="T_TABLE_ONE")
 *       private String valueA;
 *       
 *       &#64;ParameterValue(forTable="T_TABLE_ONE")
 *       private String valueB;
 *       
 *       &#64;ParameterValue(forTable="T_TABLE_TWO")
 *       private String valueC;
 *       
 *       &#64;ParameterValue(forTable="T_TABLE_TWO")
 *       private String valueD;
 *       
 *       &#64;ParameterValue(forTable="T_TABLE_THREE")
 *       private String valueE;
 *       
 *       &#64;ParameterValue(forTable="T_TABLE_THREE")
 *       private String valueF;
 * </pre>
 * </p>
 * <p>
 * <b>Here another example where the values and keys are specified directly into parameter
 * table annotations:</b>
 * 
 * <pre>
 * &#64;ParameterTableSet({
 *    &#64;ParameterTable(tableName="T_TABLE_ONE", keyField="keyOne", useAllFields=false, values = {
 *       &#64;ParameterValue("valueOnAll"),
 *       &#64;ParameterValue("valueA"),
 *       &#64;ParameterValue("valueB")
 *    }),
 *    &#64;ParameterTable(tableName="T_TABLE_TWO", keyField="keyTwo", keyType = ColumnType.ATOMIC, useAllFields=false, values = {
 *       &#64;ParameterValue("valueOnAll"),
 *       &#64;ParameterValue("valueC"),
 *       &#64;ParameterValue("valueD")
 *    }),
 *    &#64;ParameterTable(tableName="T_TABLE_THREE", keyField="keyThree", useAllFields=false, values = {
 *       &#64;ParameterValue("valueOnAll"),
 *       &#64;ParameterValue("valueE"),
 *       &#64;ParameterValue("valueF")
 *    })
 * })
 * public class TypeOnMultipleTablesSecond {
 * 
 *       private Long keyOne;
 *       
 *       private Long keyTwo;
 *       
 *       private Long keyThree;
 *       
 *       private String valueOnAll;
 *       
 *       private String valueA;
 *       
 *       private String valueB;
 *       
 *       private String valueC;
 *       
 *       private String valueD;
 *       
 *       private String valueE;
 *       
 *       private String valueF;
 * </pre>
 * </p>
 * <p>
 * The domain can be specified at set level with {@link #domainName()}
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface ParameterTableSet {

	/**
	 * <p>
	 * Alias for <code>tables</code>
	 * </p>
	 * 
	 * @return
	 */
	ParameterTable[] value() default {};

	/**
	 * <p>
	 * The specified tables for the current set
	 * </p>
	 * 
	 * @return
	 */
	ParameterTable[] tables() default {};

	/**
	 * <p>
	 * Used instead of {@link ParameterTable#useAllFields()} for similar result on current
	 * type
	 * </p>
	 * 
	 * @return
	 */
	boolean useAllFields() default true;

	/**
	 * <p>
	 * Domain to associate to all of the parameter table of the set. Used as default value
	 * for the table, but can still be overwritten by domain defined in
	 * <tt>ParameterTable</tt> annotation
	 * </p>
	 * 
	 * @return
	 */
	String domainName() default "";
}
