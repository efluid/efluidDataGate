package fr.uem.efluid.model.repositories.impls;

import static fr.uem.efluid.utils.ErrorType.METADATA_FAILED;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.metas.TableDescription;
import fr.uem.efluid.utils.ApplicationException;

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
 * @version 1
 */
public class PureJdbcDatabaseDescriptionRepository extends AbstractDatabaseDescriptionRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(PureJdbcDatabaseDescriptionRepository.class);

	@Value("${param-efluid.managed-datasource.meta.search-fk-type}")
	private String searchFkType;

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
		try {
			// Get pk for each tables ...
			for (TableDescription desc : descs.values()) {

				try (ResultSet rs = md.getPrimaryKeys(null, this.filterSchema, desc.getName())) {
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
			throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
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
	@Override
	protected void completeTablesForeignKeys(DatabaseMetaData md, Map<String, TableDescription> descs) {

		try {

			try (ResultSet rs = md.getCatalogs()) {
				while (rs.next()) {
					LOGGER.debug("Catalog : {}", rs.getString(1));

				}
			}

			// Get fk for each tables ...
			for (TableDescription desc : descs.values()) {

				try (ResultSet rs = md.getImportedKeys(null, this.filterSchema, desc.getName());) {
					while (rs.next()) {
						setForeignKey(desc, rs.getString(8), rs.getString(3), rs.getString(4));
					}
				}
			}
		} catch (SQLException e) {
			throw new ApplicationException(METADATA_FAILED, "Cannot extract metadata", e);
		}
	}

	/**
	 * @param md
	 * @throws SQLException
	 * @see fr.uem.efluid.model.repositories.impls.AbstractDatabaseDescriptionRepository#assertVendorSupport(java.sql.DatabaseMetaData)
	 */
	@Override
	protected void assertVendorSupport(DatabaseMetaData md) throws SQLException {
		LOGGER.debug("Using PURE JDBC metadata extractor : All JDBC compliant database are supported");
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

}
