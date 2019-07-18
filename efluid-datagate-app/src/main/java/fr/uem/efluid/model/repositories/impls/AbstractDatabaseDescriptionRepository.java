package fr.uem.efluid.model.repositories.impls;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.metas.ColumnDescription;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.utils.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static fr.uem.efluid.utils.ErrorType.*;

/**
 * <p>
 * Provider of database description using JDBC Metadata model : calls for JDBC standard
 * Metadata extraction, implemented by each vendor-specific driver.
 * </p>
 * <p>
 * The metadata extraction used here is not optimized for performance but for usability
 * using a description model defined in {@link TableDescription} for each identified
 * table. The extraction will try to exclude all system tables. To help identify "valid"
 * tables, a schema can be specified with parameter
 * <code>datagate-efluid.managed-datasource.meta.filterSchema</code> : Only the table of the
 * specified schema will be scanned. This parameter can also be null or "%", but it is
 * recommanded to specify a value.
 * </p>
 * <p>
 * <u>This metadata extraction implements has been tested on</u> :
 * <ul>
 * <li><b>PostgreSQL 10.1</b> (should be OK on all 9.x + versions)</li>
 * <li><b>Oracle 11g</b> (Express edition) with driver OJDBC14 10.x</li>
 * <li><b>Oracle 12c</b> (Express edition) with driver OJDBC8 12.x</li>
 * </ul>
 * Due to over-complicated extraction model used on Oracle JDBC drivers, it is much slower
 * with Oracle databases, but still OK for application needs
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public abstract class AbstractDatabaseDescriptionRepository implements DatabaseDescriptionRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabaseDescriptionRepository.class);

    protected static final String[] TABLES_TYPES = {"TABLE"/* , "VIEW" */};

    @Value("${datagate-efluid.managed-datasource.meta.filter-schema}")
    protected String filterSchema;

    @Autowired
    protected JdbcTemplate managedSource;

    @Autowired
    private ManagedQueriesGenerator generator;

    private Map<String, TableDescription> localCache;

    @Override
    public Collection<TableDescription> getTables() throws ApplicationException {

        // Already loaded
        if (this.localCache != null) {
            LOGGER.info("Use fixed preloaded metadata values from local cache.");
            return this.localCache.values();
        }

        LOGGER.info("Begin metadata extraction from managed database on schema \"{}\".", this.filterSchema);

        // Keep it cached
        Map<String, TableDescription> tables = loadTableMetadata(null);
        this.localCache = tables;

        return tables.values();
    }

    /**
     * Load the metadata for current source database. Read metadata and use only METADATA level inspection to identify the required table values
     *
     * @param filterTable optional (nullable) table name : will process only the specified table if set, else will load all
     * @return loaded table metadatas, mapped to their table names
     */
    private Map<String, TableDescription> loadTableMetadata(@Nullable String filterTable) {

        try {
            long start = System.currentTimeMillis();
            long firstStart = start;
            DatabaseMetaData md = this.managedSource.getDataSource().getConnection().getMetaData();
            LOGGER.debug("Metadata access duration : {} ms", System.currentTimeMillis() - start);

            // Check and signal if database vendor is supported by extractor
            assertVendorSupport(md);

            // Check that, if specified, the filter schema exists in managed DB
            assertSchemaExist(md);

            // 4 metadata queries for completed values
            start = System.currentTimeMillis();
            Map<String, TableDescription> tables = loadCompliantTables(md, filterTable);

            // If filtered, must have found requested table
            if (filterTable != null && tables.size() != 1) {
                throw new ApplicationException(METADATA_WRONG_TABLE, "Table " + filterTable
                        + " not found in metadata process. Wrong ref call", filterTable);
            }

            LOGGER.debug("Metadata loadCompliantTables duration : {} ms", System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            initTablesColumns(md, tables);
            LOGGER.debug("Metadata initTablesColumns duration : {} ms", System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            completeTablesPrimaryKeys(md, tables);
            LOGGER.debug("Metadata completeTablesPrimaryKeys duration : {} ms", System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            completeTablesForeignKeys(md, tables);
            LOGGER.debug("Metadata completeTablesForeignKeys duration : {} ms", System.currentTimeMillis() - start);

            LOGGER.info("Metadata extracted in {}ms from managed database on schema \"{}\", filtering on {}. Found {} tables",
                    System.currentTimeMillis() - firstStart, this.filterSchema,
                    filterTable != null ? filterTable : "no table", tables.size());

            return tables;

        } catch (SQLException e) {
            throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
        }
    }

    /**
     * @param tableName
     * @return
     */
    @Override
    public boolean isTableExists(String tableName) {

        // Check in cache if available
        if (this.localCache != null) {
            return this.localCache.containsKey(tableName);
        }

        try {
            DatabaseMetaData md = this.managedSource.getDataSource().getConnection().getMetaData();
            LOGGER.debug("Checking existance on table {}", tableName);
            try (ResultSet rs = md.getTables(null, null, tableName, TABLES_TYPES)) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
        }
    }

    /**
     * @param tableName
     * @param colNames
     * @param filterClause
     * @return
     */
    @Override
    public boolean isColumnSetHasUniqueValue(String tableName, Collection<String> colNames, String filterClause) {

        try {
            return !this.managedSource.queryForRowSet(this.generator.producesUnicityQuery(tableName, colNames, filterClause)).next();
        } catch (InvalidResultSetAccessException e) {
            throw new ApplicationException(VALUE_CHECK_FAILED,
                    "Cannot extract resultset for column unicity on table " + tableName + ", columns " + colNames, e);
        }
    }

    /**
     * @see fr.uem.efluid.model.repositories.DatabaseDescriptionRepository#refreshAll()
     */
    @Override
    public void refreshAll() {
        LOGGER.info("Metadata cache droped. Will extract fresh data on next call");
        this.localCache = null;
    }

    @Override
    public void refreshTable(String tablename) {

        // Already loaded, force update specified table
        if (this.localCache != null) {
            LOGGER.warn("Update metadata for table {}", tablename);
            this.localCache.remove(tablename);
            this.localCache.put(tablename, loadTableMetadata(tablename).get(tablename));

        }

        // Not initialized yet, force full load
        else {
            LOGGER.warn("No full cache update done yet - force it");
            this.localCache = loadTableMetadata(null);

        }
    }

    /**
     * <p>
     * Based on JDBC spec on getTables :
     *
     * <pre>
     * Each table description has the following columns:
     * 1.TABLE_CAT String => table catalog (may be null)
     * 2.TABLE_SCHEM String => table schema (may be null)
     * 3.TABLE_NAME String => table name
     * 4.TABLE_TYPE String => table type. Typical types are "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     * 5.REMARKS String => explanatory comment on the table
     * 6.TYPE_CAT String => the types catalog (may be null)
     * 7.TYPE_SCHEM String => the types schema (may be null)
     * 8.TYPE_NAME String => type name (may be null)
     * 9.SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (may be null)
     * 10.REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (may be null)
     * </pre>
     * </p>
     *
     * @param md          JDBC metadata holder, as a <tt>DatabaseMetaData</tt>
     * @param filterTable optional (nullable) filter on table name. Will process only the specified table
     * @return map of initialized table identifications as <tt>TableDescription</tt>,
     * mapped to their table name
     * @throws SQLException
     */
    protected Map<String, TableDescription> loadCompliantTables(DatabaseMetaData md, @Nullable String filterTable) throws SQLException {

        Map<String, TableDescription> tables = new HashMap<>();

        try (ResultSet rs = md.getTables(null, this.filterSchema, "%", TABLES_TYPES)) {
            while (rs.next()) {
                String tblName = rs.getString(3);

                if (filterTable == null || tblName.equalsIgnoreCase(filterTable)) {
                    TableDescription desc = new TableDescription();
                    desc.setName(tblName);
                    desc.setView(false);
                    tables.put(tblName, desc);
                }
            }
        }
        return tables;
    }

    /**
     * <p>
     * Columns are extracted in one set only, mixed for all tables.
     *
     * <pre>
     * Each column description has the following columns:
     * 1.TABLE_CAT String => table catalog (may be null)
     * 2.TABLE_SCHEM String => table schema (may be null)
     * 3.TABLE_NAME String => table name
     * 4.COLUMN_NAME String => column name
     * 5.DATA_TYPE int => SQL type from java.sql.Types
     * 6.TYPE_NAME String => Data source dependent type name, for a UDT the type name is fully qualified
     * 7.COLUMN_SIZE int => column size.
     * 8.BUFFER_LENGTH is not used.
     * 9.DECIMAL_DIGITS int => the number of fractional digits. Null is returned for data types where DECIMAL_DIGITS is not applicable.
     * 10.NUM_PREC_RADIX int => Radix (typically either 10 or 2)
     * 11.NULLABLE int => is NULL allowed. ◦ columnNoNulls - might not allow NULL values
     * ◦ columnNullable - definitely allows NULL values
     * ◦ columnNullableUnknown - nullability unknown
     * 12.REMARKS String => comment describing column (may be null)
     * 13.COLUMN_DEF String => default value for the column, which should be interpreted as a string when the value is enclosed in single quotes (may be null)
     * 14.SQL_DATA_TYPE int => unused
     * 15.SQL_DATETIME_SUB int => unused
     * 16.CHAR_OCTET_LENGTH int => for char types the maximum number of bytes in the column
     * 17.ORDINAL_POSITION int => index of column in table (starting at 1)
     * 18.IS_NULLABLE String => ISO rules are used to determine the nullability for a column. ◦ YES --- if the column can include NULLs
     * ◦ NO --- if the column cannot include NULLs
     * ◦ empty string --- if the nullability for the column is unknown
     * 19.SCOPE_CATALOG String => catalog of table that is the scope of a reference attribute (null if DATA_TYPE isn't REF)
     * 20.SCOPE_SCHEMA String => schema of table that is the scope of a reference attribute (null if the DATA_TYPE isn't REF)
     * 21.SCOPE_TABLE String => table name that this the scope of a reference attribute (null if the DATA_TYPE isn't REF)
     * 22.SOURCE_DATA_TYPE short => source type of a distinct type or user-generated Ref type, SQL type from java.sql.Types (null if DATA_TYPE isn't DISTINCT or user-generated REF)
     * 23.IS_AUTOINCREMENT String => Indicates whether this column is auto incremented ◦ YES --- if the column is auto incremented
     * ◦ NO --- if the column is not auto incremented
     * ◦ empty string --- if it cannot be determined whether the column is auto incremented
     * 24.IS_GENERATEDCOLUMN String => Indicates whether this is a generated column ◦ YES --- if this a generated column
     * ◦ NO --- if this not a generated column
     * ◦ empty string --- if it cannot be determined whether this is a generated column
     * </pre>
     * </p>
     *
     * @param md
     * @param descs
     * @return
     * @throws SQLException
     */
    protected void initTablesColumns(
            DatabaseMetaData md,
            Map<String, TableDescription> descs)
            throws SQLException {

        // Get columns for all table
        try (ResultSet rs = md.getColumns(null, this.filterSchema, "%", "%")) {
            while (rs.next()) {
                String tableName = rs.getString(3);

                TableDescription desc = descs.get(tableName);

                // Excludes system table columns, unused columns ...
                if (desc != null) {
                    ColumnDescription col = new ColumnDescription();
                    String colName = rs.getString(4);
                    col.setName(colName);
                    col.setType(ColumnType.forJdbcType(rs.getInt(5)));
                    desc.getColumns().add(col);
                }
            }
        }

    }

    /**
     * <p>
     * <b>Heavy-load</b> : Use a dedicated query for each tables to gather table keys.
     * JDBC metadata acces spec :
     *
     * <pre>
     * Each primary key column description has the following columns:
     * 1.TABLE_CAT String => table catalog (may be null)
     * 2.TABLE_SCHEM String => table schema (may be null)
     * 3.TABLE_NAME String => table name
     * 4.COLUMN_NAME String => column name
     * 5.KEY_SEQ short => sequence number within primary key( a value of 1 represents the first column of the primary key, a value of 2 would represent the second column within the primary key).
     * 6.PK_NAME String => primary key name (may be null)
     * </pre>
     * </p>
     *
     * @param md
     * @param descs
     */
    protected abstract void completeTablesPrimaryKeys(DatabaseMetaData md, Map<String, TableDescription> descs);

    /**
     * <p>
     * Regarding parameter
     * <code>datagate-efluid.managed-datasource.meta.search-fk-type</code>, process one of
     * the different available FK search processes
     * </p>
     *
     * @param md
     * @param descs
     */
    protected abstract void completeTablesForeignKeys(DatabaseMetaData md, Map<String, TableDescription> descs);

    /**
     * @param md
     * @throws SQLException
     */
    protected abstract void assertVendorSupport(DatabaseMetaData md) throws SQLException;

    /**
     * @param desc
     */
    protected static void setColumnAsPk(ColumnDescription desc) {

        // If already set, do not update type
        if (!desc.getType().isPk()) {

            // Distinct PK type for clean query building
            if (desc.getType() == ColumnType.STRING) {
                desc.setType(ColumnType.PK_STRING);
            } else {
                desc.setType(ColumnType.PK_ATOMIC);
            }
        }
    }

    /**
     * @param md
     * @throws SQLException
     */
    private void assertSchemaExist(DatabaseMetaData md) throws SQLException {

        if (this.filterSchema != null && this.filterSchema.length() >= 2) {
            boolean schemaFound = false;
            try (ResultSet rs = md.getSchemas()) {
                while (rs.next()) {
                    if (this.filterSchema.equalsIgnoreCase(rs.getString(1))) {
                        schemaFound = true;
                        break;
                    }
                }
            }
            if (!schemaFound) {
                throw new ApplicationException(METADATA_WRONG_SCHEMA,
                        "Specified schema " + this.filterSchema + " doesn't exist on current managed database");
            }
        }

    }

}
