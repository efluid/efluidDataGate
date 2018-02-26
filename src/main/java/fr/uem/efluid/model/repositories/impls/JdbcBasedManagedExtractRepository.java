package fr.uem.efluid.model.repositories.impls;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table using JDBC call
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Repository
public class JdbcBasedManagedExtractRepository implements ManagedExtractRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedManagedExtractRepository.class);

	@Autowired
	private JdbcTemplate managedSource;

	@Autowired
	private ManagedValueConverter valueConverter;

	@Autowired
	private ManagedQueriesGenerator queryGenerator;

	/**
	 * @param parameterEntry
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#extractCurrentContent(fr.uem.efluid.model.entities.DictionaryEntry)
	 */
	@Override
	public Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry, Map<String, byte[]> lobs) {

		LOGGER.debug("Extracting values from managed table {}", parameterEntry.getTableName());

		// Get columns for all table
		return this.managedSource.query(
				this.queryGenerator.producesSelectParameterQuery(parameterEntry),
				new InternalExtractor(parameterEntry, this.valueConverter, lobs));
	}

	/**
	 * <p>
	 * Extractor of a Managed parameter table : extract ALL content, for DIFF check
	 * elsewhere
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	private static class InternalExtractor implements ResultSetExtractor<Map<String, String>> {

		private final DictionaryEntry parameterEntry;
		private final ManagedValueConverter valueConverter;
		private final Map<String, byte[]> blobs;

		/**
		 * @param parameterEntry
		 */
		public InternalExtractor(DictionaryEntry parameterEntry, ManagedValueConverter valueConverter, Map<String, byte[]> lobs) {
			super();
			this.parameterEntry = parameterEntry;
			this.valueConverter = valueConverter;
			this.blobs = lobs;
		}

		/**
		 * @param rs
		 * @return
		 * @throws SQLException
		 * @throws DataAccessException
		 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
		 */
		@Override
		public Map<String, String> extractData(ResultSet rs) throws SQLException, DataAccessException {

			Map<String, String> extraction = new HashMap<>();

			final String keyName = this.parameterEntry.getKeyName().toUpperCase();
			ResultSetMetaData meta = rs.getMetaData();

			// Prepare data definition from meta
			final int count = meta.getColumnCount();
			final int last = count - 1;
			String[] columnNames = new String[count];
			ColumnType[] columnType = new ColumnType[count];
			int keyPos = 1;

			// Identify columns
			for (int i = 0; i < count; i++) {
				String colname = meta.getColumnName(i + 1).toUpperCase();
				if (colname.equals(keyName)) {
					keyPos = i;
				}
				columnNames[i] = colname;
				columnType[i] = ColumnType.forJdbcType(meta.getColumnType(i + 1));
			}

			// Process content
			while (rs.next()) {
				StringBuilder payload = new StringBuilder();
				String keyValue = null;
				for (int i = 0; i < count; i++) {
					if (i != keyPos) {
						ColumnType type = columnType[i];

						// Call for binary only if needed
						if (type == ColumnType.BINARY) {
							this.valueConverter.appendBinaryValue(payload, columnNames[i], rs.getBytes(i + 1), i == last, this.blobs);
						}

						// Else basic string extraction
						else {
							this.valueConverter.appendExtractedValue(payload, columnNames[i], rs.getString(i + 1), type,
									i == last);
						}
					} else {
						keyValue = rs.getString(i + 1);
					}
				}

				extraction.put(keyValue, payload.toString());
			}

			return extraction;
		}

	}
}
