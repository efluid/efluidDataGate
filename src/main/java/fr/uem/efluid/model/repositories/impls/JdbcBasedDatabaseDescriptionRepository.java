package fr.uem.efluid.model.repositories.impls;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import fr.uem.efluid.model.metas.ColumnDescription;
import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.model.repositories.DatabaseDescriptionRepository;
import fr.uem.efluid.utils.TechnicalException;

/**
 * <p>
 * Provider of database description using JDBC Metadata model
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Repository
public class JdbcBasedDatabaseDescriptionRepository implements DatabaseDescriptionRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedDatabaseDescriptionRepository.class);

	private static final String[] TABLES_TYPES = { "TABLE", "VIEW" };

	@Autowired
	private JdbcTemplate managedSource;

	@Override
	@Cacheable("metadatas")
	public Collection<TableDescription> getTables() throws TechnicalException {

		try {
			// TODO : check availability on all tested database
			DatabaseMetaData md = this.managedSource.getDataSource().getConnection().getMetaData();

			// 4 metadata queries for completed values
			Map<String, TableDescription> tables = loadCompliantTables(md);
			initTablesColumns(md, tables);
			completeTablesPrimaryKeys(md, tables);
			completeTablesForeignKeys(md, tables);

			LOGGER.info("Metadata extracted from managed database. Found {} tables", Integer.valueOf(tables.size()));

			return tables.values();
		} catch (SQLException e) {
			throw new TechnicalException("Cannot extract metadata", e);
		}
	}

	/**
	 * 
	 * @see fr.uem.efluid.model.repositories.DatabaseDescriptionRepository#refreshAll()
	 */
	@Override
	@CacheEvict("metadatas")
	public void refreshAll() {
		LOGGER.info("Metadata cache droped. Will extract fresh data on next call");
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
	 * @param validTables
	 * @return
	 * @throws SQLException
	 */
	private static void initTablesColumns(
			DatabaseMetaData md,
			Map<String, TableDescription> descs)
			throws SQLException {

		// Get columns for all table
		try (ResultSet rs = md.getColumns(null, null, "%", "%")) {
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
	 * @param table
	 */
	private static void completeTablesPrimaryKeys(DatabaseMetaData md, Map<String, TableDescription> descs) {
		try {
			// Get pk for each tables ...
			for (TableDescription desc : descs.values()) {

				try (ResultSet rs = md.getPrimaryKeys(null, null, desc.getName())) {
					while (rs.next()) {
						String columnName = rs.getString(4);
						desc.getColumns().stream()
								.filter(c -> c.getName().equals(columnName))
								.findFirst()
								.ifPresent(c -> c.setType(ColumnType.PK));
					}
				}
			}

		} catch (SQLException e) {
			throw new TechnicalException("Cannot extract metadata", e);
		}
	}

	/**
	 * <p>
	 * <b>Heavy-load</b> : Use a dedicated query for each tables to gather table FK. JDBC
	 * metadata acces spec :
	 * 
	 * <pre>
	 * Each primary key column description has the following columns: 
	 * 1.PKTABLE_CAT String => primary key table catalog being imported (may be null) 
	 * 2.PKTABLE_SCHEM String => primary key table schema being imported (may be null) 
	 * 3.PKTABLE_NAME String => primary key table name being imported 
	 * 4.PKCOLUMN_NAME String => primary key column name being imported 
	 * 5.FKTABLE_CAT String => foreign key table catalog (may be null) 
	 * 6.FKTABLE_SCHEM String => foreign key table schema (may be null) 
	 * 7.FKTABLE_NAME String => foreign key table name 
	 * 8.FKCOLUMN_NAME String => foreign key column name 
	 * 9.KEY_SEQ short => sequence number within a foreign key( a value of 1 represents the first column of the foreign key, a value of 2 would represent the second column within the foreign key). 
	 * 10.UPDATE_RULE short => What happens to a foreign key when the primary key is updated: ◦ importedNoAction - do not allow update of primary key if it has been imported 
	 * 11.DELETE_RULE short => What happens to the foreign key when primary is deleted. ◦ importedKeyNoAction - do not allow delete of primary key if it has been imported 
	 * 12.FK_NAME String => foreign key name (may be null) 
	 * 13.PK_NAME String => primary key name (may be null) 
	 * 14.DEFERRABILITY short => can the evaluation of foreign key constraints be deferred until commit ◦ importedKeyInitiallyDeferred - see SQL92 for definition
	 * </pre>
	 * </p>
	 * 
	 * @param md
	 * @param table
	 */
	private static void completeTablesForeignKeys(DatabaseMetaData md, Map<String, TableDescription> descs) {
		try {
			// Get fk for each tables ...
			for (TableDescription desc : descs.values()) {
				try (ResultSet rs = md.getImportedKeys(null, null, desc.getName());) {
					while (rs.next()) {
						String columnName = rs.getString(8);
						String destTable = rs.getString(3);
						desc.getColumns().stream()
								.filter(c -> c.getName().equals(columnName))
								.findFirst()
								.ifPresent(c -> c.setForeignKeyTable(destTable));
					}
				}
			}
		} catch (SQLException e) {
			throw new TechnicalException("Cannot extract metadata", e);
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
	 * @param md
	 *            JDBC metadata holder, as a <tt>DatabaseMetaData</tt>
	 * @return map of initialized table identifications as <tt>TableDescription</tt>,
	 *         mapped to their table name
	 * @throws SQLException
	 */
	private static Map<String, TableDescription> loadCompliantTables(DatabaseMetaData md) throws SQLException {

		Map<String, TableDescription> tables = new HashMap<>();

		try (ResultSet rs = md.getTables(null, null, "%", TABLES_TYPES)) {
			while (rs.next()) {
				TableDescription desc = new TableDescription();
				String tblName = rs.getString(3);
				desc.setName(tblName);
				desc.setView("VIEW".equals(rs.getString(4)));
				tables.put(tblName, desc);
			}
		}
		return tables;
	}

}
