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

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.model.repositories.DictionaryRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.TableLinkRepository;
import fr.uem.efluid.tools.ManagedQueriesGenerator;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table using JDBC call
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 2
 */
@Repository
public class JdbcBasedManagedExtractRepository implements ManagedExtractRepository {

	protected static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedManagedExtractRepository.class);

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

	/**
	 * @param parameterEntry
	 * @param lobs
	 * @param project
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#extractCurrentContent(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      java.util.Map, fr.uem.efluid.model.entities.Project)
	 */
	@Override
	public Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry, Map<String, byte[]> lobs, Project project) {

		String query = this.queryGenerator.producesSelectParameterQuery(
				parameterEntry,
				this.links.findByDictionaryEntry(parameterEntry),
				this.dictionary.findAllMappedByTableName(project));

		LOGGER.debug("Extracting values from managed table {} with query \"{}\"", parameterEntry.getTableName(), query);

		// Get columns for all table
		Map<String, String> payloads = this.managedSource.query(query, new InternalExtractor(parameterEntry, this.valueConverter, lobs));

		LOGGER.debug("Extracted values from managed table {} with query \"{}\". Found {} results", parameterEntry.getTableName(), query,
				Integer.valueOf(payloads.size()));

		return payloads;
	}

	/**
	 * @param parameterEntry
	 * @param project
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#countCurrentContentWithUncheckedJoins(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      fr.uem.efluid.model.entities.Project)
	 */
	@Override
	public int countCurrentContentWithUncheckedJoins(DictionaryEntry parameterEntry, Project project) {

		String query = this.queryGenerator.producesTestJoinParameterQuery(
				parameterEntry,
				this.links.findByDictionaryEntry(parameterEntry),
				this.dictionary.findAllMappedByTableName(project));

		LOGGER.debug("Checking values from managed table {} on unchecked Join with query \"{}\"", parameterEntry.getTableName(), query);

		Integer res = this.managedSource.queryForObject(query, Integer.class);

		LOGGER.debug("Counted values from managed table {} on unchecked join with query \"{}\". Found {} results",
				parameterEntry.getTableName(), query, res);

		return res == null ? 0 : res.intValue();
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

			int totalSize = 1;
			Map<String, String> extraction = new HashMap<>();

			final String keyName = this.parameterEntry.getKeyName().toUpperCase();
			ResultSetMetaData meta = rs.getMetaData();

			// Prepare data definition from meta
			final int count = meta.getColumnCount();
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
							this.valueConverter.appendBinaryValue(payload, columnNames[i], rs.getBytes(i + 1), this.blobs);
						}

						// Boolean need full represent of boolean
						else if (type == ColumnType.BOOLEAN) {
							this.valueConverter.appendExtractedValue(payload, columnNames[i], rs.getBoolean(i + 1) ? "true" : "false",
									type);
						}

						// Temporal need parsing for DB independency
						else if (type == ColumnType.TEMPORAL) {
							this.valueConverter.appendTemporalValue(payload, columnNames[i], rs.getTimestamp(i + 1));
						}

						// Else basic string extraction
						else {
							this.valueConverter.appendExtractedValue(payload, columnNames[i], rs.getString(i + 1), type);
						}
					} else {
						keyValue = rs.getString(i + 1);
					}

				}

				extraction.put(keyValue, this.valueConverter.finalizePayload(payload.toString()));

				// Only on debug : alert on large data load
				if (LOGGER.isDebugEnabled()) {
					if (totalSize % 100000 == 0) {
						LOGGER.debug("Large data extraction - table \"{}\" - extracted {} items", this.parameterEntry.getTableName(),
								Integer.valueOf(totalSize));
					}
					totalSize++;
				}
			}

			return extraction;
		}

	}
}
