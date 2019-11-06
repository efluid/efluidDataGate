package fr.uem.efluid.generation;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.ParameterTable;
import fr.uem.efluid.ParameterTableSet;
import fr.uem.efluid.ParameterValue;
import fr.uem.efluid.model.ParameterDomainDefinition;
import fr.uem.efluid.model.ParameterTableDefinition;

import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Map;

import static fr.uem.efluid.generation.GenerationUtils.failback;
import static fr.uem.efluid.generation.GenerationUtils.generateFixedUUID;

/**
 * Combined table details search
 *
 * @author elecomte
 * @version 1
 * @since v2.0.0
 */
class PossibleTableAnnotation {

    private final boolean intermediate;
    private Class<?> sourceType;

    private String name;
    private String tableName;
    private String filterClause;
    private String domainName;
    private String keyField;
    private ColumnType keyType;
    private boolean useAllFields;
    private Class<?>[] excludeInheritedFrom;
    private ParameterValue[] values;

    /**
     * Init from a source directly
     */
    PossibleTableAnnotation(ParameterTable paramTable, Class<?> source, boolean intermediate) {

        this.intermediate = intermediate;
        this.sourceType = source;
        this.domainName = paramTable.domainName();
        this.excludeInheritedFrom = paramTable.excludeInheritedFrom();
        this.filterClause = paramTable.filterClause();
        this.keyField = paramTable.keyField();
        this.keyType = paramTable.keyType();
        this.name = paramTable.name();
        this.tableName = GenerationUtils.failback(paramTable.value(), paramTable.tableName());
        this.useAllFields = paramTable.useAllFields();
        this.values = paramTable.values();
    }

    /**
     * Init from a source with a specified existing model
     * existing can be null (will direct init)
     */
    PossibleTableAnnotation(ParameterTable localParamTable, Class<?> source, PossibleTableAnnotation existing, boolean intermediate) {

        this.intermediate = intermediate || Modifier.isAbstract(source.getModifiers());
        this.sourceType = source;

        if (existing == null) {
            this.domainName = localParamTable.domainName();
            this.excludeInheritedFrom = localParamTable.excludeInheritedFrom();
            this.filterClause = localParamTable.filterClause();
            this.keyField = localParamTable.keyField();
            this.keyType = localParamTable.keyType();
            this.name = localParamTable.name();
            this.tableName = failback(localParamTable.value(), localParamTable.tableName());
            this.useAllFields = localParamTable.useAllFields();
            this.values = localParamTable.values();
        }

        // Found existing
        else {

            ParameterTable paramTable = localParamTable != null
                    ? localParamTable
                    : existing.getSourceType().getAnnotation(ParameterTable.class);

            this.domainName = failback(paramTable.domainName(), existing.getDomainName());
            this.excludeInheritedFrom = paramTable.excludeInheritedFrom().length > 0 ? paramTable.excludeInheritedFrom() : existing.getExcludeInheritedFrom();
            this.filterClause = failback(paramTable.filterClause(), existing.getFilterClause());
            this.keyField = failback(paramTable.keyField(), existing.getKeyField());
            this.keyType = paramTable.keyType() != ColumnType.UNKNOWN ? paramTable.keyType() : existing.getKeyType();
            this.name = failback(paramTable.name(), existing.getName());
            this.tableName = failback(paramTable.value(), paramTable.tableName(), existing.getTableName());
            this.useAllFields = paramTable.useAllFields() || existing.useAllFields;
            this.values = paramTable.values().length > 0 ? paramTable.values() : existing.getValues();
        }
    }

    /**
     * Init on a set
     */
    PossibleTableAnnotation(ParameterTable localParamTable, ParameterTableSet paramTableSet, Class<?> source, PossibleTableAnnotation existing, boolean intermediate) {

        this(localParamTable, source, existing, intermediate);

        if (existing != null) {
            ParameterTable paramTable = localParamTable != null
                    ? localParamTable
                    : existing.getSourceType().getAnnotation(ParameterTable.class);

            // Double failover
            this.domainName = failback(paramTable.domainName(), paramTableSet.domainName(), existing.getDomainName()); // 2 failovers
        } else {
            this.domainName = failback(localParamTable.domainName(), paramTableSet.domainName());
        }

    }

    public Class<?> getSourceType() {
        return sourceType;
    }

    String getName() {
        return name;
    }

    String getTableName() {
        return tableName;
    }

    String getFilterClause() {
        return filterClause;
    }

    String getDomainName() {
        return domainName;
    }

    String getKeyField() {
        return keyField;
    }

    ColumnType getKeyType() {
        return keyType;
    }

    boolean isUseAllFields() {
        return useAllFields;
    }

    Class<?>[] getExcludeInheritedFrom() {
        return excludeInheritedFrom;
    }

    ParameterValue[] getValues() {
        return values;
    }

    public boolean isIntermediate() {
        return this.intermediate;
    }

    /**
     * Convert to definition
     */
    ParameterTableDefinition toDefinition(Map<Class<?>, String> annotDomains) {

        ParameterTableDefinition def = new ParameterTableDefinition();
        def.setCreatedTime(LocalDateTime.now());
        def.setUpdatedTime(def.getCreatedTime());
        def.setDomain(new ParameterDomainDefinition()); // Will be merged later

        // Found domain name
        def.getDomain().setName(failback(getDomainName(), annotDomains.get(getSourceType())));

        // Domain is mandatory
        if (def.getDomain().getName() == null) {
            throw new IllegalArgumentException(
                    "No domain found for type " + getName()
                            + ". Need to specify the domain with meta-annotation, with package annotation or with domainName property in @ParameterTable");
        }

        // Init table def
        def.setParameterName(failback(getName(), getSourceType().getSimpleName()));
        def.setTableName(failback(getTableName(), getSourceType().getSimpleName().toUpperCase()));
        def.setWhereClause(getFilterClause());
        def.setUuid(generateFixedUUID(def.getTableName(), ParameterTableDefinition.class));

        return def;
    }

}
