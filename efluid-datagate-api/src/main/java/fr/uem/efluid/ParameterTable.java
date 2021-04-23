package fr.uem.efluid;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Definition of a <b>dictionary entry</b>. Specified on any component which can be
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
 * once for all in {@link ParameterDomain#name()} </li>
 * <li>For simplicity, the domain can be also specified at package level, once again with
 * annotation {@link ParameterDomain} : All <tt>ParameterTable</tt> from the same package
 * will be associated to the domain specified once for all in
 * {@link ParameterDomain#name()}</li>
 * </ul>
 * </p>
 * <p>
 * Properties can be inherited : all annotated inherited properties are "added" to current
 * type definition. But the annotation itself is <b>not</b> inherited
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Documented
@Retention(RUNTIME)
@Target({ANNOTATION_TYPE, TYPE})
@Inherited
public @interface ParameterTable {

    String value() default "";

    /**
     * <p>
     * Specify the technical table name for the parameter table. Please use UPPERCASE
     * value only. If not set, will use the parameter name (as specified with
     * {@link #name()} or from annotated type name), UPPERCASE.
     * </p>
     */
    String tableName() default "";

    /**
     * <p>
     * Specify the business name for the parameter table (how it will be identified in
     * Dictionary management). Unique : if the value is not unique, the dictionary build
     * will fail. If not set, will use the annotated type name as parameter name
     * </p>
     *
     * @return a specified business name for the parameter table in the dictionary
     */
    String name() default "";

    /**
     * <p>
     * Custom filter clause to apply at SQL level for data filtering. Default is "1=1",
     * meaning "get all lines from the table". See dictionary management rules for custom
     * filter
     * </p>
     * </p>
     */
    String filterClause() default "1=1";

    /**
     * <p>
     * Domain to associate to the parameter table. Mandatory if not specified with custom
     * annotation or at package level
     * </p>
     */
    String domainName() default "";

    /**
     * <p>
     * Allows to set the <tt>ParameterKey</tt> at table definition level, using a fixed
     * field name. If the field doesn't exist, dictionary definition will fail. The key
     * specified at table level can be overridden by a directly set <tt>ParameterKey</tt>
     * </p>
     * <p>
     * If the key type cannot be found from the field definition, then the generation will
     * fail. Non identifiable key type must be defined with property {@link #keyType()} or
     * by definition of <tt>ParameterKey</tt>
     * </p>
     */
//    String keyField() default "";

    /**
     * <p>
     * When the type for the table-specified key cannot be defined automatically, specify
     * it directly with this parameter. Useful for "non standard" property types. If not
     * set, will define it using rules specified in {@link ColumnType#forClass(Class)}
     * </p>
     */
//    ColumnType keyType() default ColumnType.UNKNOWN;

    ParameterKey[] keys() default {};

    /**
     * <p>
     * If set to true, all the fields of the annotated table definition class are
     * identified as <tt>ParameterValue</tt> with the field name as column name. The
     * <tt>ParameterKey</tt> can be specified
     * </p>
     * <p>
     * Default is true : all fields are automatically mapped. Specified
     * <tt>ParameterValue</tt> add specific features. The annotation
     * <tt>ParameterIgnored</tt> can also be used to ignore some specific fields from
     * automatic use as property value
     * </p>
     */
    boolean useAllFields() default true;

    /**
     * <p>
     * To force exclusion for default fields from specified inherited type.
     * Allows to specify a table mapping on a type will excluding any technical field from,
     * for example, a technical top level type (like with some Efluid ORM model types)
     * </p>
     * <p>On each inheritance, if no fields are specified, then all are excluded. Else only the specified
     * fields will be excluded</p>
     *
     * @return array of inheritance spec to exclude from field inclusion
     */
    ParameterInheritance[] excludeInherited() default {};

    /**
     * <p>
     * When a parameter table is identified with <tt>ParameterTableSet</tt> then the
     * values can be directly identified here
     * </p>
     */
    ParameterValue[] values() default {};
}
