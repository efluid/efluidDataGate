package fr.uem.efluid.model.repositories.impls;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.ManagedParametersRepository;
import fr.uem.efluid.utils.ManagedDiffUtils;
import fr.uem.efluid.utils.ManagedQueriesUtils;

/**
 * <p>
 * Access to raw data of parameters : can read from Parameter source table or regenerate
 * from existing index
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Repository
public class JdbcBasedManagedParametersRepository implements ManagedParametersRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(JdbcBasedDatabaseDescriptionRepository.class);

	@Autowired
	private JdbcTemplate managedSource;

	@Autowired
	private IndexRepository coreIndex;

	/**
	 * @param parameterEntry
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedParametersRepository#extractCurrentContent(fr.uem.efluid.model.entities.DictionaryEntry)
	 */
	@Override
	public Map<String, String> extractCurrentContent(DictionaryEntry parameterEntry) {

		LOGGER.debug("Extracting values from managed table {}", parameterEntry.getTableName());

		// Get columns for all table
		return this.managedSource.query(
				ManagedQueriesUtils.producesSelectParameterQuery(parameterEntry),
				new InternalExtractor(parameterEntry));
	}

	/**
	 * Produces the knew content for specified table, from recorded index
	 * 
	 * @param parameterEntry
	 * @return
	 */
	@Override
	@Cacheable("regenerated")
	public Map<String, String> regenerateKnewContent(DictionaryEntry parameterEntry) {

		LOGGER.debug("Regenerating values from local index for managed table {}", parameterEntry.getTableName());

		// Will process backlog by its natural order
		List<IndexEntry> existingBacklog = this.coreIndex.findByDictionaryEntryOrderByTimestamp(parameterEntry);

		// Content for playing back the backlog
		Map<String, String> lines = new ConcurrentHashMap<>(1000);

		for (IndexEntry line : existingBacklog) {

			// Addition : add / update directly
			if (line.getAction() == IndexAction.ADD || line.getAction() == IndexAction.UPDATE) {
				lines.put(line.getKeyValue(), line.getPayload());
			}

			else {
				lines.remove(line.getKeyValue());
			}
		}

		return lines;
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

		/**
		 * @param parameterEntry
		 */
		public InternalExtractor(DictionaryEntry parameterEntry) {
			super();
			this.parameterEntry = parameterEntry;
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
						ManagedDiffUtils.appendExtractedValue(payload, columnNames[i], rs.getString(i + 1), columnType[i], i == last);
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
