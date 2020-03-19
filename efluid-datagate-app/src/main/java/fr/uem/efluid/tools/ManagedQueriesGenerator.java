package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.SelectClauseGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * Tools for query build used in Managed database access. Contains query building
 * processes for managed value extraction. Some helpers provides also solution for
 * building stored part of the Managed source details.
 * </p>
 * <p>
 * <b>All query items can be "protected" with double quotes</b> (validated as default rule
 * for Postgres, and supported on Oracle and MS SQL Server). So a select is always like
 * this : <code>select "colA", "colB", "colC" from "Table" where "colA"='truc'</code>.
 * This feature is optional : all behaviors are specified from available
 * <tt>QueryGenerationRules</tt> initialized in Spring context
 * </p>
 * <p>
 * For mapping, a dedicated generation is used when the linked table is itself a parameter
 * table.
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Component
public class ManagedQueriesGenerator extends SelectClauseGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedQueriesGenerator.class);

    private static final String AFFECT = "=";

    private static final String WHERE_CLAUSE_SEP = " AND ";
    private static final String WHERE_CLAUSE_SUB_START = " ( ";
    private static final String WHERE_CLAUSE_SUB_END = " ) ";

    private final String selectQueryModel;
    private final String countQueryModel;
    private final String insertQueryModel;
    private final String updateQueryModel;
    private final String deleteQueryModel;
    private final String unicityQueryModel;
    private final String joinSubQueryModel;
    private final String updateOrInsertLinkedSubQueryModel;
    private final String missingLinkClauseModel;
    private final String nullableKeyJoinSupportModel;
    private final DateTimeFormatter dbDateFormater;

    /**
     * Prepare generator regarding the specified rules
     *
     * @param rules
     */
    public ManagedQueriesGenerator(@Autowired QueryGenerationRules rules) {

        super(rules.isColumnNamesProtected());

        // Prepare templates for query generation
        this.countQueryModel = generateCountQueryTemplate(rules);
        this.selectQueryModel = generateSelectQueryTemplate(rules);
        this.insertQueryModel = generateInsertQueryTemplate(rules);
        this.updateQueryModel = generateUpdateQueryTemplate(rules);
        this.deleteQueryModel = generateDeleteQueryTemplate(rules);
        this.unicityQueryModel = generateUnicityQueryTemplate(rules);
        this.joinSubQueryModel = generateJoinSubQueryTemplate(rules);
        this.updateOrInsertLinkedSubQueryModel = generateUpdateOrInsertLinkedSubQueryTemplate(rules);
        this.missingLinkClauseModel = generateSelectMissingLinkWhereClausePartTemplate(rules);
        this.dbDateFormater = DateTimeFormatter.ofPattern(rules.getDatabaseDateFormat());
        this.nullableKeyJoinSupportModel = generateNullableKeyJoinSupportTemplate(rules);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Initialized query generator with models :");
            LOGGER.debug(" -> countQueryModel => {}", this.countQueryModel);
            LOGGER.debug(" -> selectQueryModel => {}", this.selectQueryModel);
            LOGGER.debug(" -> insertQueryModel => {}", this.insertQueryModel);
            LOGGER.debug(" -> updateQueryModel => {}", this.updateQueryModel);
            LOGGER.debug(" -> deleteQueryModel => {}", this.deleteQueryModel);
            LOGGER.debug(" -> unicityQueryModel => {}", this.unicityQueryModel);
            LOGGER.debug(" -> joinSubQueryModel => {}", this.joinSubQueryModel);
            LOGGER.debug(" -> updateOrInsertLinkedSubQueryModel => {}", this.updateOrInsertLinkedSubQueryModel);
            LOGGER.debug(" -> missingLinkClauseModel => {}", this.missingLinkClauseModel);
            LOGGER.debug(" -> nullableKeyJoinSupportModel => {}", this.nullableKeyJoinSupportModel);
        }
    }

    /**
     * @param parameterEntry
     * @return
     */
    public String producesSelectParameterQuery(DictionaryEntry parameterEntry, Collection<TableLink> links,
                                               Map<String, DictionaryEntry> allEntries, boolean nullableLinkKeys) {

        // Need clean select for uses
        String selectClause = consolidateSelectClause(parameterEntry);

        // For inner linked (select contains them already)
        String joinClauses = prepareJoinLinks(links, allEntries, JoinType.INCLUDE, nullableLinkKeys);

        return String.format(this.selectQueryModel,
                selectClause,
                parameterEntry.getTableName(),
                joinClauses,
                parameterEntry.getWhereClause(),
                parameterEntry.getKeyName());
    }

    /**
     * @param parameterEntry
     * @return
     */
    public String producesTestJoinParameterQuery(DictionaryEntry parameterEntry, List<TableLink> links,
                                                 Map<String, DictionaryEntry> allEntries, boolean nullableLinkKeys) {

        // For inner linked (select contains them already)
        String joinClauses = prepareJoinLinks(links, allEntries, JoinType.MISSING, nullableLinkKeys);

        return String.format(this.countQueryModel,
                parameterEntry.getTableName(),
                joinClauses,
                parameterEntry.getWhereClause());
    }

    /**
     * @param parameterEntry
     * @return
     */
    public String producesSelectMissingParameterQuery(DictionaryEntry parameterEntry, List<TableLink> links,
                                                      Map<String, DictionaryEntry> allEntries, boolean nullableLinkKeys) {

        // Need clean select for uses
        String selectClause = consolidateSelectClauseForMissingLinks(parameterEntry, links);

        // For inner linked (select contains them already)
        String joinClauses = prepareJoinLinks(links, allEntries, JoinType.MISSING, nullableLinkKeys);

        // Custom where clause with reversed select
        String whereClause = prepareReverseJoinLinksWhereClause(parameterEntry.getWhereClause(), links, allEntries);

        return String.format(this.selectQueryModel,
                selectClause,
                parameterEntry.getTableName(),
                joinClauses,
                whereClause,
                parameterEntry.getKeyName());
    }

    /**
     * @param parameterEntry
     * @param keyValue
     * @param values
     * @return
     */
    public String producesApplyAddQuery(
            DictionaryEntry parameterEntry,
            String keyValue,
            List<Value> values,
            List<String> referedLobs,
            List<TableLink> links,
            Map<String, DictionaryEntry> allEntries) {
        // INSERT INTO %s (%) VALUES (%)
        List<Value> combined = new ArrayList<>();

        // Custom behavior for composite
        if (parameterEntry.isCompositeKey()) {
            combined.addAll(prepareKeyValues(parameterEntry, keyValue));
        }

        // Else use standard direct key set
        else {
            combined.add(new KeyValue(parameterEntry.getKeyName(), parameterEntry.getKeyType(), keyValue));
        }

        combined.addAll(values);
        return String.format(this.insertQueryModel, parameterEntry.getTableName(), allNames(combined, links, allEntries),
                allValues(combined, referedLobs, links, allEntries));
    }

    /**
     * @param parameterEntry
     * @param keyValue
     * @return
     */
    public String producesApplyRemoveQuery(DictionaryEntry parameterEntry, String keyValue) {

        // DELETE FROM %s WHERE %s

        String keyWhere;

        // Dedicated "heavy" process for composite
        if (parameterEntry.isCompositeKey()) {
            keyWhere = combinedKeyWhere(prepareKeyValues(parameterEntry, keyValue));
        }

        // Standard process for single (common) key
        else {
            keyWhere = valueAffect(new KeyValue(parameterEntry.getKeyName(), parameterEntry.getKeyType(), keyValue), null);
        }

        return String.format(this.deleteQueryModel, parameterEntry.getTableName(), keyWhere);
    }

    /**
     * @param parameterEntry
     * @param keyValue
     * @param values
     * @return
     */
    public String producesApplyUpdateQuery(DictionaryEntry parameterEntry,
                                           String keyValue,
                                           List<Value> values,
                                           List<String> referedLobs,
                                           List<TableLink> links,
                                           Map<String, DictionaryEntry> allEntries) {

        // UPDATE %s SET %s WHERE %s

        String keyWhere;

        // Dedicated "heavy" process for composite
        if (parameterEntry.isCompositeKey()) {
            keyWhere = combinedKeyWhere(prepareKeyValues(parameterEntry, keyValue));
        }

        // Standard process for single (common) key
        else {
            keyWhere = valueAffect(new KeyValue(parameterEntry.getKeyName(), parameterEntry.getKeyType(), keyValue), null);
        }

        return String.format(this.updateQueryModel, parameterEntry.getTableName(),
                allValuesAffect(values, referedLobs, links, allEntries), keyWhere);
    }

    /**
     * @param parameterEntry
     * @param keyValue
     * @return
     */
    public String producesGetOneQuery(DictionaryEntry parameterEntry, String keyValue) {

        // SELECT %s FROM %s WHERE %s ORDER BY %s (reused query)

        String keyWhere;

        // Dedicated "heavy" process for composite
        if (parameterEntry.isCompositeKey()) {
            keyWhere = combinedKeyWhere(prepareKeyValues(parameterEntry, keyValue));
        }

        // Standard process for single (common) key
        else {
            keyWhere = valueAffect(new KeyValue(parameterEntry.getKeyName(), parameterEntry.getKeyType(), keyValue), null);
        }

        // Ordering by 1st key only
        return String.format(this.selectQueryModel, "1", parameterEntry.getTableName(), "", keyWhere, parameterEntry.getKeyName());
    }

    /**
     * To check unicity on parameter key (support for composite)
     *
     * @param tablename
     * @param columnNames
     * @param filterClause
     * @return
     */
    public String producesUnicityQuery(String tablename, Collection<String> columnNames, String filterClause) {

        // select 1 from "TTYPEMATERIEL" cur where cur."ID"<50 group by "SERIE" HAVING COUNT("SERIE") > 1 ;
        return String.format(this.unicityQueryModel, tablename, filterClause, prepareSimpleSelectPart(columnNames));
    }

    /**
     * @param value
     * @param lobsKey for affect of binary (LOB). Can be null if LOB is not used in value
     * @return
     */
    private String valueAffect(Value value, List<String> lobsKey) {
        if (this.protectColumns) {
            return ITEM_PROTECT + value.getName() + ITEM_PROTECT + AFFECT + value.getTyped(lobsKey, this.dbDateFormater);
        }
        return value.getName() + AFFECT + value.getTyped(lobsKey, this.dbDateFormater);
    }

    /**
     * @param values
     * @return
     */
    private String allNames(List<Value> values,
                            List<TableLink> links,
                            Map<String, DictionaryEntry> allEntries) {

        Stream<String> names;

        // Dedicated when mapped links exist
        if (hasMappedLinks(links, allEntries)) {

            names = values.stream().map(v -> {

                int lnStart = v.getName().indexOf(LINK_TAB_REFLAP);

                if (lnStart >= 0) {
                    String propName = v.getName().substring(lnStart + LINK_TAB_REFLAP.length());

                    // Has a link for propName : correct link attribute
                    Optional<TableLink> link = links.stream().filter(l -> l.getColumnFrom().equals(propName)).findFirst();
                    if (link.isPresent()) {
                        return propName;
                    }
                }

                return v.getName();

            });

        } else {
            names = values.stream().map(Value::getName);
        }

        if (this.protectColumns) {
            return ITEM_PROTECT + names.collect(Collectors.joining(SELECT_CLAUSE_SEP_PROTECT)) + ITEM_PROTECT;
        }
        return names.collect(Collectors.joining(SELECT_CLAUSE_SEP));
    }

    /**
     * @param values
     * @return
     */
    private String allValuesAffect(
            List<Value> values,
            List<String> lobKeys,
            List<TableLink> links,
            Map<String, DictionaryEntry> allEntries) {

        // col = val

        // Dedicated when mapped links exist
        if (hasMappedLinks(links, allEntries)) {

            return values.stream().map(v -> {

                int lnStart = v.getName().indexOf(LINK_TAB_REFLAP);

                if (lnStart >= 0) {
                    String propName = v.getName().substring(lnStart + LINK_TAB_REFLAP.length());
                    return propName + AFFECT + linkValueSubSelect(propName, v.getValueAsString(), links, allEntries);
                }

                return valueAffect(v, lobKeys);

            }).collect(Collectors.joining(SELECT_CLAUSE_SEP));

        }

        return values.stream().map(v -> valueAffect(v, lobKeys)).collect(Collectors.joining(SELECT_CLAUSE_SEP));
    }

    /**
     * @param keys
     * @return
     */
    private String combinedKeyWhere(List<KeyValue> keys) {
        return WHERE_CLAUSE_SUB_START
                + keys.stream().map(v -> valueAffect(v, null)).collect(Collectors.joining(WHERE_CLAUSE_SEP))
                + WHERE_CLAUSE_SUB_END;
    }

    /**
     * @param values
     * @return
     */
    private String allValues(
            List<Value> values,
            List<String> lobKeys,
            List<TableLink> links,
            Map<String, DictionaryEntry> allEntries) {

        // Dedicated when mapped links exist
        if (hasMappedLinks(links, allEntries)) {

            return values.stream().map(v -> {

                int lnStart = v.getName().indexOf(LINK_TAB_REFLAP);

                if (lnStart >= 0) {
                    return linkValueSubSelect(v.getName().substring(lnStart + LINK_TAB_REFLAP.length()), v.getValueAsString(), links,
                            allEntries);
                }

                return v.getTyped(lobKeys, this.dbDateFormater);

            }).collect(Collectors.joining(SELECT_CLAUSE_SEP));
        }

        return values.stream().map(v -> v.getTyped(lobKeys, this.dbDateFormater)).collect(Collectors.joining(SELECT_CLAUSE_SEP));
    }

    /**
     * @param linkedAttr
     * @param value
     * @param links
     * @param allEntries
     * @return
     */
    private String linkValueSubSelect(
            String linkedAttr,
            String value,
            List<TableLink> links,
            Map<String, DictionaryEntry> allEntries) {

        TableLink link = links.stream().filter(l -> l.getColumnFrom().equals(linkedAttr)).findFirst().orElseThrow(
                () -> new ApplicationException(ErrorType.REFER_MISS_LINK, "Unknown value link : " + linkedAttr, linkedAttr));

        DictionaryEntry dic = allEntries.get(link.getTableTo());

        String keyWhere;

        // Dedicated "heavy" process for composite
        if (dic.isCompositeKey()) {
            keyWhere = combinedKeyWhere(prepareKeyValues(dic, value));
        }

        // Standard process for single (common) key
        else {
            keyWhere = valueAffect(new KeyValue(dic.getKeyName(), dic.getKeyType(), value), null);
        }

        // (SELECT "%s" FROM "%s" WHERE %s)
        return String.format(this.updateOrInsertLinkedSubQueryModel, link.getColumnTo(), dic.getTableName(), keyWhere);
    }

    /**
     * @param parameterEntry
     * @return
     */
    private String consolidateSelectClause(DictionaryEntry parameterEntry) {

        // Basic consolidate => Select all
        if (DEFAULT_SELECT_CLAUSE.equals(parameterEntry.getSelectClause())) {
            return DEFAULT_SELECT_CLAUSE;
        }

        // Clean search for key
        String keyRef = createKeysRef(parameterEntry).trim();

        // If keyname not in select clause, need to add it
        if (parameterEntry.getSelectClause() == null || !parameterEntry.getSelectClause().contains(keyRef)) {

            // Need to remove last comma from key select
            if (parameterEntry.getSelectClause() == null && keyRef.endsWith(",")) {
                return keyRef.substring(0, keyRef.length() - 1);
            }

            return keyRef + parameterEntry.getSelectClause();
        }

        return parameterEntry.getSelectClause() != null ? parameterEntry.getSelectClause() : "";
    }

    /**
     * @param parameterEntry
     * @return
     */
    private String createKeysRef(DictionaryEntry parameterEntry) {

        // For most case, use simple key build
        if (!parameterEntry.isCompositeKey()) {
            return this.protectColumns
                    ? CURRENT_TAB_ALIAS + ITEM_PROTECT + parameterEntry.getKeyName() + ITEM_PROTECT + SELECT_CLAUSE_SEP
                    : CURRENT_TAB_ALIAS + parameterEntry.getKeyName() + SELECT_CLAUSE_SEP;
        }

        // For composite, use advanced building from iterator
        return parameterEntry.keyNames()
                .map(k -> this.protectColumns
                        ? CURRENT_TAB_ALIAS + ITEM_PROTECT + k + ITEM_PROTECT
                        : CURRENT_TAB_ALIAS + k)
                .collect(Collectors.joining(SELECT_CLAUSE_SEP)) + SELECT_CLAUSE_SEP;
    }

    /**
     * @param parameterEntry
     * @return
     */
    private String consolidateSelectClauseForMissingLinks(DictionaryEntry parameterEntry, List<TableLink> links) {

        return consolidateSelectClause(parameterEntry) + ", " + links.stream().map(l -> {
            if (this.protectColumns) {
                return CURRENT_TAB_ALIAS + ITEM_PROTECT + l.getColumnFrom() + ITEM_PROTECT;
            }
            return CURRENT_TAB_ALIAS + l.getColumnFrom();
        }).collect(Collectors.joining(", ")) + " ";
    }

    /**
     * @param links
     * @param allEntries
     * @return
     */
    private String prepareJoinLinks(Collection<TableLink> links,
                                    Map<String, DictionaryEntry> allEntries, JoinType type, boolean nullableLinkKeys) {

        AtomicInteger pos = new AtomicInteger(0);

        return links.stream()
                .filter(l -> allEntries.containsKey(l.getTableTo())).sorted(linkOrder())
                .map(l -> {
                    DictionaryEntry dic = allEntries.get(l.getTableTo());
                    String alias = LINK_TAB_ALIAS + pos.incrementAndGet();
                    // INNER JOIN "%s" %s on %s."%s" = cur."%s"
                    // or for test : LEFT OUTER JOIN ....
                    // or whatever join type is required
                    // "%s JOIN " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " %s ON %s";
                    return String.format(
                            this.joinSubQueryModel,
                            type.getValue(),
                            dic.getTableName(),
                            alias,
                            linkJoinOn(alias, l)  + (nullableLinkKeys ? nullableKeyJoinSupport(alias, l) : "")
                    );
                }).collect(Collectors.joining(" "));
    }

    /**
     * <p>
     * Inner part of join, with support for composite key links
     * </p>
     *
     * @param alias
     * @param link
     * @return
     */
    private String linkJoinOn(String alias, TableLink link) {

        // Default - standard single key
        if (!link.isCompositeKey()) {
            return this.protectColumns
                    ? alias + ".\"" + link.getColumnTo() + "\" = cur.\"" + link.getColumnFrom() + "\""
                    : alias + "." + link.getColumnTo() + " = cur." + link.getColumnFrom();
        }

        // Rare - composite key link
        int max = (int) link.columnFroms().count();

        StringBuilder joinOn = new StringBuilder();

        for (int i = 0; i < max; i++) {
            if (this.protectColumns) {
                joinOn.append(alias).append(".\"").append(link.getColumnTo(i)).append("\" = cur.\"").append(link.getColumnFrom(i))
                        .append("\"");
            } else {
                joinOn.append(alias).append(".").append(link.getColumnTo(i)).append(" = cur.").append(link.getColumnFrom(i));
            }

            if (i < max - 1) {
                joinOn.append(" AND ");
            }
        }

        return joinOn.toString();
    }


    private String nullableKeyJoinSupport(String alias, TableLink link) {

        // Default - standard single key
        if (!link.isCompositeKey()) {
            return String.format(this.nullableKeyJoinSupportModel,
                    (this.protectColumns ? alias + ".\"" + link.getColumnTo() + "\"" : alias + "." + link.getColumnTo()) + ", " +
                            (this.protectColumns ? "cur.\"" + link.getColumnFrom() + "\"" : "cur." + link.getColumnFrom()));
        }

        // Rare - composite key link
        int max = (int) link.columnFroms().count();

        StringBuilder linkAlias = new StringBuilder();

        for (int i = 0; i < max; i++) {
            if (this.protectColumns) {
                linkAlias.append(alias).append(".\"").append(link.getColumnTo(i)).append("\", cur.\"").append(link.getColumnFrom(i))
                        .append("\"");
            } else {
                linkAlias.append(alias).append(".").append(link.getColumnTo(i)).append(", cur.").append(link.getColumnFrom(i));
            }

            if (i < max - 1) {
                linkAlias.append(", ");
            }
        }

        return String.format(this.nullableKeyJoinSupportModel,linkAlias.toString());
    }

    /**
     * @param links
     * @param allEntries
     * @return
     */
    private String prepareReverseJoinLinksWhereClause(
            String existingWhereClause,
            List<TableLink> links,
            Map<String, DictionaryEntry> allEntries) {

        AtomicInteger pos = new AtomicInteger(0);

        return existingWhereClause + " AND (" + links.stream()
                .filter(l -> allEntries.containsKey(l.getTableTo())).sorted(linkOrder()).map(l -> {
                    String alias = LINK_TAB_ALIAS + pos.incrementAndGet();
                    return String.format(this.missingLinkClauseModel, alias, l.getColumnTo());
                }).collect(Collectors.joining(" OR ")) + ")";
    }

    /**
     * @param combinedKeyValue
     * @return
     */
    private static List<String> revertCombinedKeyValue(String combinedKeyValue) {

        return Stream.of(combinedKeyValue.split(KEY_JOIN_SPLITER)).collect(Collectors.toList());
    }

    /**
     * <p>
     * Produces the keys for a <tt>DictionaryEntry</tt> as item <tt>KeyValue</tt>
     * </p>
     * <p>
     * Not optimized, should be used only for composite key tables (which is a rare
     * condition)
     * </p>
     *
     * @param parameterEntry
     * @param combinedKeyValue
     * @return
     */
    private static List<KeyValue> prepareKeyValues(DictionaryEntry parameterEntry, String combinedKeyValue) {

        // 3 ordered source of data for key items
        List<String> values = revertCombinedKeyValue(combinedKeyValue);
        List<String> keyNames = parameterEntry.keyNames().collect(Collectors.toList());
        List<ColumnType> keyTypes = parameterEntry.keyTypes().collect(Collectors.toList());

        List<KeyValue> result = new ArrayList<>();

        for (int i = 0; i < keyNames.size(); i++) {
            result.add(new KeyValue(keyNames.get(i), keyTypes.get(i), values.get(i)));
        }

        return result;
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateCountQueryTemplate(QueryGenerationRules rules) {
        return "SELECT count(*) FROM " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") +
                " cur %s WHERE %s";
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateSelectQueryTemplate(QueryGenerationRules rules) {
        return "SELECT %s FROM " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") +
                " cur %s WHERE %s ORDER BY cur." +
                (rules.isColumnNamesProtected() ? "\"%s\"" : "%s");
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateUpdateQueryTemplate(QueryGenerationRules rules) {
        return "UPDATE " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " SET %s WHERE %s";
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateDeleteQueryTemplate(QueryGenerationRules rules) {
        return "DELETE FROM " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " WHERE %s ";
    }

    /**
     * Prepare join part for linked properties on select queries
     *
     * @param rules
     * @return
     */
    private static String generateJoinSubQueryTemplate(QueryGenerationRules rules) {
        // Join type specified on call
        return "%s JOIN " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " %s ON %s";
    }

    /**
     * Prepare sub select used for value gathered from linked table on insert or update
     * queries
     *
     * @param rules
     * @return
     */
    private static String generateUpdateOrInsertLinkedSubQueryTemplate(QueryGenerationRules rules) {
        return "(SELECT " + (rules.isColumnNamesProtected() ? "\"%s\"" : "%s") + " FROM " +
                (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " WHERE %s)";
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateUnicityQueryTemplate(QueryGenerationRules rules) {
        return "SELECT 1 FROM " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") +
                " cur WHERE %s GROUP BY %s HAVING COUNT(*) > 1";
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateInsertQueryTemplate(QueryGenerationRules rules) {
        return "INSERT INTO " + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " (%s) VALUES (%s)";
    }

    /**
     * <p>
     * For select on excluded content on missing ref
     * </p>
     *
     * @param rules
     * @return
     */
    private static String generateSelectMissingLinkWhereClausePartTemplate(QueryGenerationRules rules) {
        return " %s." + (rules.isTableNamesProtected() ? "\"%s\"" : "%s") + " IS NULL ";
    }

    /**
     * Generate the template regarding the rules on protect / not protected
     *
     * @param rules
     * @return
     */
    private static String generateNullableKeyJoinSupportTemplate(QueryGenerationRules rules) {
        return "OR COALESCE(%s) IS NULL";
    }

    /**
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    private enum JoinType {

        INCLUDE("INNER"),
        MISSING("LEFT OUTER");

        private final String value;

        JoinType(String value) {
            this.value = value;
        }

        String getValue() {
            return this.value;
        }
    }

    /**
     * <p>
     * Specific model for key : allows to easily specify chained values when the key need
     * to be added
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    private static class KeyValue implements Value {
        private final String keyName;
        private final ColumnType keyType;
        private final String keyValue;

        /**
         * @param keyName
         * @param keyType
         * @param keyValue
         */
        KeyValue(String keyName, ColumnType keyType, String keyValue) {
            super();
            this.keyName = keyName;
            this.keyType = keyType;
            this.keyValue = keyValue;
        }

        /**
         * @return
         * @see fr.uem.efluid.services.types.Value#getName()
         */
        @Override
        public String getName() {
            return this.keyName;
        }

        /**
         * @return
         * @see fr.uem.efluid.services.types.Value#getValue()
         */
        @Override
        public byte[] getValue() {
            // Not used here.
            return null;
        }

        /**
         * @return
         * @see fr.uem.efluid.services.types.Value#getType()
         */
        @Override
        public ColumnType getType() {
            return this.keyType;
        }

        /**
         * @return
         * @see fr.uem.efluid.services.types.Value#getValueAsString()
         */
        @Override
        public String getValueAsString() {
            return this.keyValue;
        }

    }

    /**
     * <p>
     * Specify the options for query generation : allows to adapt behavior with precise
     * rules when the DB type needs it. Rules can be configuration-based or preselected
     * from the DB vendor
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    public interface QueryGenerationRules {

        /**
         * <p>
         * Do we need to put double quote to protect the column name in SQL query?
         * </p>
         *
         * @return
         */
        boolean isColumnNamesProtected();

        /**
         * <p>
         * Do we need to put double quote to protect the table name in SQL query ?
         * </p>
         *
         * @return
         */
        boolean isTableNamesProtected();

        /**
         * <p>
         * The specified date format compliant with managed database for string-based
         * injection
         * </p>
         *
         * @return
         */
        String getDatabaseDateFormat();
    }

}
