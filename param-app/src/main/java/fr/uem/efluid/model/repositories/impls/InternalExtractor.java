package fr.uem.efluid.model.repositories.impls;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * <p>
 * Standardized model for managed database value extractor. Produces a payload for each RS
 * line, mapped to key
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public abstract class InternalExtractor<T> implements ResultSetExtractor<Map<String, String>> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(InternalExtractor.class);

	private final DictionaryEntry parameterEntry;
	private final ManagedValueConverter valueConverter;

	/**
	 * @param parameterEntry
	 */
	public InternalExtractor(DictionaryEntry parameterEntry, ManagedValueConverter valueConverter) {
		super();
		this.parameterEntry = parameterEntry;
		this.valueConverter = valueConverter;
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
			T payload = initLineHolder();
			String keyValue = null;
			for (int i = 0; i < count; i++) {
				if (i != keyPos) {
					// Internally specified extractor process
					appendProcessValue(this.valueConverter, payload, columnType[i], columnNames[i], i + 1, rs);

				} else {
					keyValue = rs.getString(i + 1);
				}

			}

			extraction.put(keyValue, getFinalizedPayload(this.valueConverter, payload));

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

	/**
	 * <p>
	 * As the holder for a line payload before finalization depends on internal extraction
	 * process, this method inits the required holder. Called at each line
	 * </p>
	 * 
	 * @return
	 */
	protected abstract T initLineHolder();

	/**
	 * <p>
	 * From current converter, process completion of value payload. Called at each column
	 * except key col
	 * </p>
	 * 
	 * @param currentValueConverter
	 * @param lineHolder
	 * @param type
	 * @param columnName
	 * @param colPosition
	 * @param rs
	 * @throws SQLException
	 */
	protected abstract void appendProcessValue(
			ManagedValueConverter currentValueConverter,
			T lineHolder,
			ColumnType type,
			String columnName,
			int colPosition,
			ResultSet rs) throws SQLException;

	/**
	 * <p>
	 * Finalize the holder for string payload generate. As the holder and the payload
	 * models can depends on extractor process, this abstract method allows to customize
	 * the completion of a line. Called after each line
	 * </p>
	 * 
	 * @param currentValueConverter
	 * @param lineHolder
	 * @return
	 */
	protected abstract String getFinalizedPayload(ManagedValueConverter currentValueConverter, T lineHolder);
}