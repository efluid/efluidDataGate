package fr.uem.efluid.model.repositories.impls;

import static fr.uem.efluid.utils.ErrorType.METADATA_FAILED;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.metas.ColumnDescription;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;

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
 * <code>param-efluid.managed-datasource.meta.filterSchema</code> : Only the table of the
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
 * @since v0.0.1
 * @version 2
 */
public class OracleDatabaseDescriptionRepository extends AbstractDatabaseDescriptionRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(OracleDatabaseDescriptionRepository.class);

	private static final String ORACLE_VENDOR = "Oracle";

	// Possible values for param-efluid.managed-datasource.meta.search-fk-type
	private static final String SEARCH_FK_TYPE_ORACLE_NAME = "oracle-by-name";
	private static final String SEARCH_FK_TYPE_ORACLE_DETAILS = "oracle-by-details";
	private static final String SEARCH_FK_TYPE_DISABLED = "disabled";

	private static final String ORACLE_FK_SEARCH_BY_CONSTRAINT_NAME = "select c.table_name as from_table,"
			+ " a.column_name as from_col, "
			+ " c.r_constraint_name as dest_constraint, "
			+ " a.position "
			+ "from all_constraints c"
			+ " inner join all_cons_columns a on a.constraint_name = c.constraint_name and a.OWNER = c.OWNER "
			+ "where c.OWNER = ? and c.constraint_type = 'R'";

	private static final String ORACLE_PK_SEARCH = "select c.table_name, a.column_name, a.position from all_constraints c "
			+ " inner join all_cons_columns a on a.constraint_name = c.constraint_name "
			+ "	where c.OWNER = ? and c.constraint_type = 'P'";

	private static final String ORACLE_PK_ENDING_FOR_FK_SEARCH = "_PK";

	private static final String ORACLE_FK_SEARCH_BY_CONSTRAINT_DETAILS = "select c.table_name as from_table, a.column_name as from_col, p.table_name as dest_table, p.column_name as dest_col "
			+ "from all_constraints c "
			+ "inner join all_cons_columns a on a.constraint_name = c.constraint_name and a.OWNER = c.OWNER "
			+ "inner join all_cons_columns p on p.constraint_name = c.r_constraint_name and p.OWNER = c.OWNER and p.position = a.position "
			+ "where c.OWNER = ? and c.constraint_type = 'R'";

	@Value("${param-efluid.managed-datasource.meta.search-fk-type}")
	private String searchFkType;

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
	@Override
	protected void initTablesColumns(
			DatabaseMetaData md,
			Map<String, TableDescription> descs)
			throws SQLException {

		Set<String> processedTables = new HashSet<>();
		int max = descs.size();

		// Use own connection with local closure
		try (Connection con = this.managedSource.getDataSource().getConnection();) {

			try (PreparedStatement stat = con
					.prepareStatement("SELECT COLUMN_NAME, DATA_TYPE, TABLE_NAME FROM USER_TAB_COLUMNS")) {

				// Get columns for all table
				try (ResultSet rs = stat.executeQuery()) {

					int prevSize = 0;

					while (rs.next()) {
						String tableName = rs.getString(3);
						TableDescription desc = descs.get(tableName);
						if (desc != null) {
							ColumnDescription col = new ColumnDescription();
							col.setName(rs.getString(1));
							col.setType(typeByOracleTypeName(rs.getString(2)));
							desc.getColumns().add(col);

							if (LOGGER.isDebugEnabled()) {
								processedTables.add(tableName);
								if (prevSize != processedTables.size() && processedTables.size() % 50 == 0) {
									LOGGER.debug("Extracting columns : ~ {}% done",
											Integer.valueOf(new BigDecimal((100 * processedTables.size() / max)).intValue()));
								}
								prevSize = processedTables.size();
							}
						}
					}
				}
			} catch (SQLException e) {
				throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
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
	@Override
	protected void completeTablesPrimaryKeys(DatabaseMetaData md, Map<String, TableDescription> descs) {

		Set<String> processedTables = new HashSet<>();
		int max = descs.size();

		try {

			// Use own connection with local closure
			try (Connection con = this.managedSource.getDataSource().getConnection();
					PreparedStatement stat = con.prepareStatement(ORACLE_PK_SEARCH);) {

				// Filtering by schema is mandatory
				stat.setString(1, this.filterSchema);

				try (ResultSet rs = stat.executeQuery()) {
					int prevSize = 0;

					while (rs.next()) {
						String tableName = rs.getString(1);
						TableDescription desc = descs.get(tableName);
						if (desc != null) {
							String columnName = rs.getString(2);
							int columnPosition = rs.getInt(3);
							desc.getColumns().stream()
									.filter(c -> c.getName().equals(columnName))
									.findFirst()
									.ifPresent(c -> {
										setColumnAsPk(c);
										c.setPosition(columnPosition); // PK position
									});

							if (LOGGER.isDebugEnabled()) {
								processedTables.add(tableName);
								if (prevSize != processedTables.size() && processedTables.size() % 100 == 0) {
									LOGGER.debug("Extracting PKs : ~ {}% done",
											Integer.valueOf(new BigDecimal((100 * processedTables.size() / max)).intValue()));
								}
								prevSize = processedTables.size();
							}
						}
					}
				}
			}

		} catch (SQLException e) {
			throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
		}
	}

	/**
	 * <p>
	 * Regarding parameter
	 * <code>param-efluid.managed-datasource.meta.search-fk-type</code>, process one of
	 * the different available FK search processes
	 * </p>
	 * 
	 * @param md
	 * @param table
	 */
	@Override
	protected void completeTablesForeignKeys(DatabaseMetaData md, Map<String, TableDescription> descs) {

		// Check is a supported param
		assertFkParameterModelIsCompliantWithDatabaseVendor(md);

		switch (this.searchFkType) {
		case SEARCH_FK_TYPE_ORACLE_DETAILS:
			LOGGER.info("FK search is done \"by details\" on oracle database with custom process. "
					+ "If still too slow, and if PK names are normalised, try with parameter"
					+ " param-efluid.managed-datasource.meta.search-fk-type ={}", SEARCH_FK_TYPE_ORACLE_NAME);
			completeTablesForeignKeysOracleByConstraintDetails(md, descs);
			break;
		case SEARCH_FK_TYPE_ORACLE_NAME:
			LOGGER.info("FK search is done \"by name\" on oracle database with custom process. If the "
					+ "PK names are not normalized (NAME_PK), some FK can be missed");
			completeTablesForeignKeysOracleByConstraintName(md, descs);
			break;
		default:
			LOGGER.info("FK search value is disabled (parameter "
					+ "param-efluid.managed-datasource.meta.search-fk-type specified as {}). No FK search in metadata",
					SEARCH_FK_TYPE_DISABLED);
			break;
		}

	}

	/**
	 * @param md
	 * @throws SQLException
	 * @see fr.uem.efluid.model.repositories.impls.AbstractDatabaseDescriptionRepository#assertVendorSupport(java.sql.DatabaseMetaData)
	 */
	@Override
	protected void assertVendorSupport(DatabaseMetaData md) throws SQLException {
		/*
		 * On Oracle, metadata extraction is slower : It is based on a complexe data
		 * extraction with various SP. So with a "fresh" db, it can be very slow on first
		 * calls, until precaching works. If no schema is specified, metadata needs to
		 * filter over 5000+ SYTEM tables ...
		 */

		String realVendor = md.getDatabaseProductName();

		if (realVendor.equalsIgnoreCase(ORACLE_VENDOR)) {

			// No Schema = very very slow
			if (this.filterSchema == null || this.filterSchema.length() < 2) {
				LOGGER.warn("On Oracle database, the call of Metadata description can be very slow (minutes long) "
						+ "if no schema filtering is specified. Please wait for metadata completion, or update "
						+ "configuration with a fixed param-efluid.managed-datasource.meta.filterSchema parameter");
			}

			// Always slower than PGSql
			else {
				LOGGER.info("Accessing metadata on Oracle database. Process can be quite slow (10 / 20 seconds long)"
						+ " if many tables exist on specified schema \"{}\"", this.filterSchema);
			}
		} else {
			throw new ApplicationException(ErrorType.METADATA_WRONG_TYPE, "Unsupported database vendor for specified type. Found "
					+ realVendor + " but can support only oracle in current extractor");
		}

	}

	/**
	 * @param md
	 */
	private void assertFkParameterModelIsCompliantWithDatabaseVendor(DatabaseMetaData md) {

		try {
			switch (this.searchFkType) {
			case SEARCH_FK_TYPE_ORACLE_DETAILS:
			case SEARCH_FK_TYPE_ORACLE_NAME:
				if (!md.getDatabaseProductName().equalsIgnoreCase(ORACLE_VENDOR)) {
					throw new ApplicationException(METADATA_FAILED,
							"Parameter param-efluid.managed-datasource.meta.search-fk-type can use value \""
									+ SEARCH_FK_TYPE_ORACLE_DETAILS + "\" or \"" + SEARCH_FK_TYPE_ORACLE_NAME
									+ "\" only if the current database is Oracle");
				}
				break;

			default:
				// Other are permited
				break;
			}
		} catch (SQLException e) {
			throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
		}
	}

	/**
	 * <p>
	 * Seach for FK on Oracle DB using a custom query, using all_constraints and
	 * all_cons_columns views. Should find any FK, but slower thant the "by name" process
	 * </p>
	 * 
	 * @param md
	 * @param descs
	 */
	private void completeTablesForeignKeysOracleByConstraintDetails(DatabaseMetaData md, Map<String, TableDescription> descs) {

		// Use own connection with local closure
		try (Connection con = this.managedSource.getDataSource().getConnection();
				PreparedStatement stat = con.prepareStatement(ORACLE_FK_SEARCH_BY_CONSTRAINT_DETAILS);) {

			// Filtering by schema is mandatory
			stat.setString(1, this.filterSchema);

			try (ResultSet rs = stat.executeQuery()) {
				while (rs.next()) {

					// 1 : from_table
					// 2 : from_col
					// 3 : dest_table
					// 4 : dest_col

					TableDescription desc = descs.get(rs.getString(1));

					if (desc != null) {

						if (desc.getName().equals("T_REFERER_WITH_NAT_COMPO")) {
							System.out.println("gotcha");
						}

						setForeignKey(desc, rs.getString(2), rs.getString(3), rs.getString(4));
					}
				}
			}
		} catch (SQLException e) {
			throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
		}
	}

	/**
	 * <p>
	 * Seach for FK on Oracle DB using a custom query, using all_constraints and
	 * all_cons_columns views and processing normalized PK names. Should be the fastest
	 * process, but can miss some FK if the PK names are not normalized. If it is the
	 * case, try the "by details" process
	 * </p>
	 * 
	 * @param md
	 * @param descs
	 */
	private void completeTablesForeignKeysOracleByConstraintName(DatabaseMetaData md, Map<String, TableDescription> descs) {

		// Use own connection with local closure
		try (Connection con = this.managedSource.getDataSource().getConnection();
				PreparedStatement stat = con.prepareStatement(ORACLE_FK_SEARCH_BY_CONSTRAINT_NAME);) {

			// Filtering by schema is mandatory
			stat.setString(1, this.filterSchema);

			try (ResultSet rs = stat.executeQuery()) {
				while (rs.next()) {

					// 1 : from_table
					// 2 : from_col
					// 3 : dest_constraint (PK of dest table)

					TableDescription desc = descs.get(rs.getString(1));

					if (desc != null) {

						String columnName = rs.getString(2);
						String destConstraint = rs.getString(3);
						int position = rs.getInt(4);

						// Use the PK name = 1st part is dest table name.
						String destTable = destConstraint.substring(0, destConstraint.length() - ORACLE_PK_ENDING_FOR_FK_SEARCH.length());

						TableDescription destDesc = descs.get(destTable);

						if (destDesc != null) {

							// Search for the PK on the dest table (on same position)
							destDesc.getColumns().stream()
									.filter(c -> c.getType().isPk() && c.getPosition() == position)
									.findFirst() // Then apply FK if found
									.ifPresent(c -> setForeignKey(desc, columnName, destTable, c.getName()));
						}
					}
				}
			}
		} catch (SQLException e) {
			throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
		}
	}

	/**
	 * @param desc
	 * @param columnName
	 * @param destTable
	 * @param destColumn
	 */
	private static void setForeignKey(
			TableDescription desc,
			String columnName,
			String destTable,
			String destColumn) {

		LOGGER.debug("Apply FK to table {} : column {} refers column {} of table {}", desc.getName(), columnName, destColumn, destTable);

		desc.getColumns().stream()
				.filter(c -> c.getName().equals(columnName))
				.findFirst()
				.ifPresent(c -> {
					c.setForeignKeyTable(destTable);
					c.setForeignKeyColumn(destColumn);
				});
	}

	/**
	 * @param typename
	 * @return
	 */
	private static ColumnType typeByOracleTypeName(String typename) {

		int typeSizeStart = typename.indexOf('(');

		// Must ignore size part
		String typeraw = typeSizeStart > 0 ? typename.substring(0, typeSizeStart) : typename;

		switch (typeraw) {
		case "VARCHAR2":
		case "CHAR":
			return ColumnType.STRING;
		case "TIMESTAMP":
		case "DATE":
			return ColumnType.TEMPORAL;
		case "NUMBER":
		case "FLOAT":
			return ColumnType.ATOMIC;
		case "BLOB":
		case "CLOB":
		case "RAW":
			return ColumnType.BINARY;
		default:
			LOGGER.debug("Unknown type {}", typeraw);
			return ColumnType.UNKNOWN;
		}
	}

}
