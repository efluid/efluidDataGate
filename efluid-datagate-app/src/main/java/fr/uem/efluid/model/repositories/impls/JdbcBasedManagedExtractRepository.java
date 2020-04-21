package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.FormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * <b>Main extractor</b> : this component is the entry point for all "READ" operations from the managed table :
 * <ul>
 * <li>Extraction of "current" content</li>
 * <li>Test on the existing content</li>
 * <li>Check on missing joins for "remarks" building</li>
 * </ul>
 * </p>
 * <p>
 * Access to raw data of parameters : can read from Parameter source table using JDBC call
 * </p>
 * <p>
 * Default implements using <tt>JdbcTemplate</tt> with various {@link ResultSetExtractor}
 * depending on required results. SQL query generation is done by a {@link ManagedQueriesGenerator}
 * </p>
 *
 * @author elecomte
 * @version 4
 * @since v0.0.1
 */
@Repository
public class JdbcBasedManagedExtractRepository implements ManagedExtractRepository {

    protected static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedManagedExtractRepository.class);
    protected static final Logger QUERRY_LOGGER = LoggerFactory.getLogger("extractor.queries");

    @Autowired
    private JdbcTemplate managedSource;

    @Autowired
    private ManagedValueConverter valueConverter;

    @Autowired
    private ManagedQueriesGenerator queryGenerator;

    @Autowired
    private DictionaryRepository dictionary;

    @Autowired
    private TableLinkRepository links;

    // TODO : use FeatureManager instead
    @org.springframework.beans.factory.annotation.Value("${datagate-efluid.extractor.show-sql}")
    private boolean showSql;

    // TODO : use FeatureManager instead
    @org.springframework.beans.factory.annotation.Value("${datagate-efluid.extractor.use-label-for-col-name}")
    private boolean useLabelForColNames;

    /**
     * @param parameterEntry current parameter table to process
     * @param tableData      test data result holder
     * @param limit          test limit for temporary extraction
     * @return the estimated content that will be extracted
     * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#testCurrentContent(fr.uem.efluid.model.entities.DictionaryEntry,
     * java.util.List, long)
     */
    @Override
    public long testCurrentContent(DictionaryEntry parameterEntry, List<List<String>> tableData, long limit) {

        String query = this.queryGenerator.producesSelectParameterQuery(
                parameterEntry,
                Collections.emptyList(),
                new HashMap<>());

        LOGGER.debug("Test values from managed table {} with query \"{}\"", parameterEntry.getTableName(), query);

        postProcessQuery(query);

        // Load table content for query + Get total count (if has some)
        Long results = this.managedSource.query(query, new TestRawExtractor(limit, tableData));

        return results != null ? results : 0;
    }

    /**
     * <p>
     * Main extractor !!! this method prepare an extractor query for a specified parameter table,
     * run it and convert all values into payloads for index generation => It extracts the "current" content
     * from the managed database
     * </p>
     * <p>
     * This is one of the most important process of Datagate, used for DIFF and MERGE process. This can be improved for performance and
     * memory
     * </p>
     *
     * @param parameterEntry current parameter table to process
     * @param lobs           holder for all found lobs. Lobs are stored in the given map, associated to an hash, and the payload contains only the specified hash for each lob values
     * @param project        current processing project
     * @return all extracted "current" content, as a map of payload linked to there content key
     * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#extractCurrentContent(fr.uem.efluid.model.entities.DictionaryEntry,
     * java.util.Map, fr.uem.efluid.model.entities.Project)
     */
    @Override
    public Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry, Map<String, byte[]> lobs, Project project) {

        String query = this.queryGenerator.producesSelectParameterQuery(
                parameterEntry,
                this.links.findByDictionaryEntry(parameterEntry),
                this.dictionary.findAllMappedByTableName(project));

        LOGGER.debug("Extracting values from managed table {} with query \"{}\"", parameterEntry.getTableName(), query);

        postProcessQuery(query);

        // Get columns for all table
        Map<String, String> payloads = this.managedSource.query(query,
                new ValueInternalExtractor(parameterEntry, this.valueConverter, this.useLabelForColNames, lobs))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        LOGGER.debug("Extracted values from managed table {} with query \"{}\". Found {} results",
                parameterEntry.getTableName(), query, payloads != null ? payloads.size() : -1);

        return payloads;
    }

    /**
     * @param parameterEntry table to extract
     * @param project        current processing project
     * @return count of unchecked joins regarding the configured rules for database joins
     * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#countCurrentContentWithUncheckedJoins(fr.uem.efluid.model.entities.DictionaryEntry,
     * fr.uem.efluid.model.entities.Project)
     */
    @Override
    public int countCurrentContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project) {

        String query = this.queryGenerator.producesTestJoinParameterQuery(
                parameterEntry,
                this.links.findByDictionaryEntry(parameterEntry),
                this.dictionary.findAllMappedByTableName(project));

        LOGGER.debug("Checking values from managed table {} on unchecked Join with query \"{}\"", parameterEntry.getTableName(), query);

        postProcessQuery(query);

        Integer res = this.managedSource.queryForObject(query, Integer.class);

        LOGGER.debug("Counted values from managed table {} on unchecked join with query \"{}\". Found {} results",
                parameterEntry.getTableName(), query, res);

        return res == null ? 0 : res;
    }

    /**
     * @param parameterEntry table to extract
     * @param project        current processing project
     * @return details of missing join contents regarding the configured database join rules
     * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#extractCurrentMissingContentWithUncheckedJoins(fr.uem.efluid.model.entities.DictionaryEntry,
     * fr.uem.efluid.model.entities.Project)
     */
    @Override
    public Map<String, String> extractCurrentMissingContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project) {

        String query = this.queryGenerator.producesSelectMissingParameterQuery(
                parameterEntry,
                this.links.findByDictionaryEntry(parameterEntry),
                this.dictionary.findAllMappedByTableName(project));

        LOGGER.debug("Extracting missing values from managed table {} on unchecked Join with query \"{}\"",
                parameterEntry.getTableName(), query);

        postProcessQuery(query);

        // Get columns for all table
        Map<String, String> payloads = this.managedSource.query(query, new DisplayInternalExtractor(parameterEntry, this.valueConverter, this.useLabelForColNames));

        LOGGER.debug("Extracted values from managed table {} on unchecked Join with query \"{}\". Found {} results",
                parameterEntry.getTableName(), query, payloads != null ? payloads.size() : -1);

        return payloads;
    }

    /**
     * @param query current extractor query to post-process. Current simply log queries if enabled
     */
    private void postProcessQuery(String query) {

        // Can output query (using a similar logger than the one from Hibernate on
        // show-sql configuration parameter)
        if (this.showSql) {
            QUERRY_LOGGER.info(query);
        }
    }

    /**
     * <p>
     * Extractor of a Managed parameter table : extract ALL content, for DIFF check
     * elsewhere
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    private static class ValueInternalExtractor extends InternalExtractor<StringBuilder> {

        private final Map<String, byte[]> blobs;

        /**
         * @param parameterEntry      the current table spec
         * @param valueConverter      current converter for a payload value
         * @param useLabelForColNames if true will include the column names as payload content prefixes
         * @param lobs                Holder for all processed lob contents
         */
        public ValueInternalExtractor(
                DictionaryEntry parameterEntry,
                ManagedValueConverter valueConverter,
                boolean useLabelForColNames,
                Map<String, byte[]> lobs) {
            super(parameterEntry, valueConverter, useLabelForColNames);
            this.blobs = lobs;
        }

        /**
         * @return holder model for a line
         * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#initLineHolder()
         */
        @Override
        protected StringBuilder initLineHolder() {
            return new StringBuilder();
        }

        /**
         * @param currentValueConverter applied converter for payload content extract / build
         * @param lineHolder            content holder for the current line, as StringBuilder
         * @param type                  current column type
         * @param columnName            current specified column name
         * @param colPosition           current column position
         * @param rs                    current line content
         * @throws SQLException on RS content error
         */
        @Override
        protected void appendProcessValue(
                ManagedValueConverter currentValueConverter,
                StringBuilder lineHolder,
                ColumnType type,
                int nativeType,
                String columnName,
                int colPosition,
                ResultSet rs) throws SQLException {

            // Call for binary only if needed
            if (type == ColumnType.BINARY) {
                currentValueConverter.appendBinaryValue(lineHolder, columnName, rs.getBytes(colPosition), this.blobs);
            }

            // Text are managed as custom type
            else if (type == ColumnType.TEXT) {
                currentValueConverter.appendTextValue(lineHolder, columnName, rs.getString(colPosition), this.blobs);
            }

            // Boolean need full represent of boolean
            else if (type == ColumnType.BOOLEAN) {
                currentValueConverter.appendExtractedValue(lineHolder, columnName, rs.getBoolean(colPosition) ? "true" : "false", type);
            }

            // Temporal need parsing for DB independency
            else if (type == ColumnType.TEMPORAL) {
                currentValueConverter.appendTemporalValue(lineHolder, columnName, rs.getTimestamp(colPosition));
            }

            // Else basic string extraction
            else {
                currentValueConverter.appendExtractedValue(lineHolder, columnName, rs.getString(colPosition), type);
            }
        }

        /**
         * @param currentValueConverter converter
         * @param lineHolder            current line content holder as a list of strings
         * @return completed payload in a merged string
         * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#getFinalizedPayload(fr.uem.efluid.tools.ManagedValueConverter,
         * java.lang.Object)
         */
        @Override
        protected String getFinalizedPayload(ManagedValueConverter currentValueConverter, StringBuilder lineHolder) {
            return currentValueConverter.finalizePayload(lineHolder.toString());
        }
    }

    /**
     * <p>
     * Extractor for direct display of value (used for remark rendering). Result is
     * similar to a standard HrPayload
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    private static class DisplayInternalExtractor extends InternalExtractor<List<Value>> {

        /**
         * @param parameterEntry      the current table spec
         * @param valueConverter      current converter for a payload value
         * @param useLabelForColNames if true will include the column names as payload content prefixes
         */
        public DisplayInternalExtractor(
                DictionaryEntry parameterEntry,
                ManagedValueConverter valueConverter,
                boolean useLabelForColNames) {
            super(parameterEntry, valueConverter, useLabelForColNames);
        }

        /**
         * @return a new holder for a complete line
         * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#initLineHolder()
         */
        @Override
        protected List<Value> initLineHolder() {
            return new ArrayList<>();
        }

        /**
         * Process one column in current line
         *
         * @param currentValueConverter current converter. Defines the type of payload we build
         * @param lineHolder            extracted line holder, as a list of (not encoded) strings
         * @param type                  current column type for payload prefix apply
         * @param columnName            the specified column name, to store in payload
         * @param colPosition           current column position
         * @param rs                    current line content
         */
        @Override
        protected void appendProcessValue(
                ManagedValueConverter currentValueConverter,
                List<Value> lineHolder,
                ColumnType type,
                int nativeType,
                String columnName,
                int colPosition,
                ResultSet rs) throws SQLException {

            String value;

            // Call for binary only if needed
            if (type == ColumnType.BINARY || type == ColumnType.TEXT) {
                value = "BYTES";
            }

            // Boolean need full represent of boolean
            else if (type == ColumnType.BOOLEAN) {
                value = rs.getBoolean(colPosition) ? "true" : "false";
            }

            // Temporal as formated
            else if (type == ColumnType.TEMPORAL) {
                Timestamp ts = rs.getTimestamp(colPosition);
                value = ts != null ? FormatUtils.format(LocalDateTime.ofInstant(ts.toInstant(), ZoneId.systemDefault())) : null;
            }

            // Else basic string extraction
            else {
                value = rs.getString(colPosition);
            }

            if (value != null) {
                lineHolder.add(new DisplayValue(columnName, FormatUtils.toBytes(value), type));
            }
        }

        /**
         * @param currentValueConverter applied converter for payload generation
         * @param lineHolder            current line list of values
         * @return merged payload, processed through the converter
         * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#getFinalizedPayload(fr.uem.efluid.tools.ManagedValueConverter,
         * java.lang.Object)
         */
        @Override
        protected String getFinalizedPayload(ManagedValueConverter currentValueConverter, List<Value> lineHolder) {

            // Removes unseted links
            currentValueConverter.filterInternalValueForLinks(lineHolder);

            return currentValueConverter.displayInternalValue(lineHolder);
        }

        /**
         * @author elecomte
         * @version 1
         * @since v0.0.8
         */
        private static final class DisplayValue implements Value {

            private final String name;
            private final byte[] value;
            private final ColumnType type;

            public DisplayValue(String name, byte[] value, ColumnType type) {
                super();
                this.name = name;
                this.value = value;
                this.type = type;
            }

            /**
             * @return the name
             */
            @Override
            public String getName() {
                return this.name;
            }

            /**
             * @return the value
             */
            @Override
            public byte[] getValue() {
                return this.value;
            }

            /**
             * @return the type
             */
            @Override
            public ColumnType getType() {
                return this.type;
            }

        }

    }

    /**
     * <p>
     * For query validation, dedicated extractor "as a basic table ready to display" of a
     * Resultset.
     * </p>
     * <p>
     * Not immutable, doesn't support asynchronous process. Reserved for preview only (too long values are troncated)
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.8
     */
    private static class TestRawExtractor implements ResultSetExtractor<Long> {

        private final long count;
        private final List<List<String>> holder;
        private static final int LIMIT_STR_VAL = 20;

        public TestRawExtractor(long count, List<List<String>> holder) {
            super();
            this.count = count;
            this.holder = holder;
        }

        @Override
        public Long extractData(ResultSet rs) throws SQLException, DataAccessException {

            ResultSetMetaData meta = rs.getMetaData();

            // Prepare data definition from meta
            final int colCount = meta.getColumnCount();
            long totalCount = 0;
            String[] columnNames = new String[colCount];
            Set<String> existingColumns = new HashSet<>();
            ColumnType[] columnType = new ColumnType[colCount];

            // Identify columns
            for (int i = 0; i < colCount; i++) {

                String colname = meta.getColumnName(i + 1).toUpperCase();

                // Avoid duplicates
                if (!existingColumns.contains(colname)) {
                    columnNames[i] = colname;
                    columnType[i] = ColumnType.forJdbcType(meta.getColumnType(i + 1));
                    existingColumns.add(colname);
                }
            }

            // With removed duplicates empty columns ...
            this.holder.add(
                    Arrays.stream(columnNames)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()));

            // Process content - limited details
            while (rs.next() && totalCount < this.count) {
                List<String> line = new ArrayList<>();
                for (int i = 0; i < colCount; i++) {

                    // Avoid duplicates
                    if (columnNames[i] != null) {
                        extractValue(line, columnType[i], i + 1, rs);
                    }
                }
                this.holder.add(line);
                totalCount++;
            }

            // Bad count ...
            while (rs.next()) {
                totalCount++;
            }

            // Remaining line
            if (totalCount > this.count) {
                totalCount++;
            }

            return totalCount;
        }

        /**
         * <p>
         * A basic flat extractor for common values
         * </p>
         *
         * @param holder      payload content holding list
         * @param type        current column type
         * @param colPosition current column position
         * @param rs          current line
         * @throws SQLException on RS access
         */
        private static void extractValue(
                List<String> holder,
                ColumnType type,
                int colPosition,
                ResultSet rs) throws SQLException {

            // Call for binary only if needed
            if (type == ColumnType.BINARY) {
                holder.add("- BLOB -");
            }

            // Boolean need full represent of boolean
            else if (type == ColumnType.BOOLEAN) {
                holder.add(rs.getBoolean(colPosition) ? "true" : "false");
            }

            // Temporal need parsing for DB independency
            else if (type == ColumnType.TEMPORAL) {
                holder.add(FormatUtils.formatRawDate(rs.getTimestamp(colPosition)));
            }

            // Else basic string extraction
            else {
                // Can be a CLOB, need to limit size
                String val = rs.getString(colPosition);

                // Limit value size if too long for preview
                if (val.length() > LIMIT_STR_VAL) {
                    holder.add(val.substring(0, LIMIT_STR_VAL - 2) + "...");
                } else {
                    holder.add(val);
                }

            }
        }
    }
}
