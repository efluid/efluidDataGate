package fr.uem.efluid.model.repositories.impls;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

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

	@org.springframework.beans.factory.annotation.Value("${param-efluid.extractor.show-sql}")
	private boolean showSql;

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

		postProcessQuery(query);

		// Get columns for all table
		Map<String, String> payloads = this.managedSource.query(query,
				new ValueInternalExtractor(parameterEntry, this.valueConverter, lobs));

		LOGGER.debug("Extracted values from managed table {} with query \"{}\". Found {} results",
				parameterEntry.getTableName(), query, Integer.valueOf(payloads.size()));

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

		postProcessQuery(query);

		Integer res = this.managedSource.queryForObject(query, Integer.class);

		LOGGER.debug("Counted values from managed table {} on unchecked join with query \"{}\". Found {} results",
				parameterEntry.getTableName(), query, res);

		return res == null ? 0 : res.intValue();
	}

	/**
	 * @param parameterEntry
	 * @param project
	 * @return
	 * @see fr.uem.efluid.model.repositories.ManagedExtractRepository#extractCurrentMissingContentWithUncheckedJoins(fr.uem.efluid.model.entities.DictionaryEntry,
	 *      fr.uem.efluid.model.entities.Project)
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
		Map<String, String> payloads = this.managedSource.query(query, new DisplayInternalExtractor(parameterEntry, this.valueConverter));

		LOGGER.debug("Extracted values from managed table {} on unchecked Join with query \"{}\". Found {} results",
				parameterEntry.getTableName(), query, Integer.valueOf(payloads.size()));

		return payloads;
	}

	/**
	 * @param query
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
	 * @since v0.0.1
	 * @version 1
	 */
	private static class ValueInternalExtractor extends InternalExtractor<StringBuilder> {

		private final Map<String, byte[]> blobs;

		/**
		 * @param parameterEntry
		 */
		public ValueInternalExtractor(DictionaryEntry parameterEntry, ManagedValueConverter valueConverter, Map<String, byte[]> lobs) {
			super(parameterEntry, valueConverter);
			this.blobs = lobs;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#initLineHolder()
		 */
		@Override
		protected StringBuilder initLineHolder() {
			return new StringBuilder();
		}

		/**
		 * @param currentValueConverter
		 * @param lineHolder
		 * @param type
		 * @param columnName
		 * @param colPosition
		 * @param rs
		 * @throws SQLException
		 * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#appendProcessValue(fr.uem.efluid.tools.ManagedValueConverter,
		 *      java.lang.Object, fr.uem.efluid.ColumnType, java.lang.String, int,
		 *      java.sql.ResultSet)
		 */
		@Override
		protected void appendProcessValue(
				ManagedValueConverter currentValueConverter,
				StringBuilder lineHolder,
				ColumnType type,
				String columnName,
				int colPosition,
				ResultSet rs) throws SQLException {

			// Call for binary only if needed
			if (type == ColumnType.BINARY) {
				currentValueConverter.appendBinaryValue(lineHolder, columnName, rs.getBytes(colPosition), this.blobs);
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
		 * @param currentValueConverter
		 * @param lineHolder
		 * @return
		 * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#getFinalizedPayload(fr.uem.efluid.tools.ManagedValueConverter,
		 *      java.lang.Object)
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
	 * @since v0.0.1
	 * @version 1
	 */
	private static class DisplayInternalExtractor extends InternalExtractor<List<Value>> {

		/**
		 * @param parameterEntry
		 */
		public DisplayInternalExtractor(DictionaryEntry parameterEntry, ManagedValueConverter valueConverter) {
			super(parameterEntry, valueConverter);
		}

		/**
		 * @return
		 * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#initLineHolder()
		 */
		@Override
		protected List<Value> initLineHolder() {
			return new ArrayList<>();
		}

		/**
		 * @param currentValueConverter
		 * @param lineHolder
		 * @param type
		 * @param columnName
		 * @param colPosition
		 * @param rs
		 * @throws SQLException
		 * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#appendProcessValue(fr.uem.efluid.tools.ManagedValueConverter,
		 *      java.lang.Object, fr.uem.efluid.ColumnType, java.lang.String, int,
		 *      java.sql.ResultSet)
		 */
		@Override
		protected void appendProcessValue(
				ManagedValueConverter currentValueConverter,
				List<Value> lineHolder,
				ColumnType type,
				String columnName,
				int colPosition,
				ResultSet rs) throws SQLException {

			String value = null;

			// Call for binary only if needed
			if (type == ColumnType.BINARY) {
				value = "BYTES";
			}

			// Boolean need full represent of boolean
			else if (type == ColumnType.BOOLEAN) {
				value = rs.getBoolean(colPosition) ? "true" : "false";
			}

			// Temporal as formated
			else if (type == ColumnType.TEMPORAL) {
				value = FormatUtils.format(LocalDateTime.ofInstant(rs.getTimestamp(colPosition).toInstant(), ZoneId.systemDefault()));
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
		 * @param currentValueConverter
		 * @param lineHolder
		 * @return
		 * @see fr.uem.efluid.model.repositories.impls.InternalExtractor#getFinalizedPayload(fr.uem.efluid.tools.ManagedValueConverter,
		 *      java.lang.Object)
		 */
		@Override
		protected String getFinalizedPayload(ManagedValueConverter currentValueConverter, List<Value> lineHolder) {

			// Removes unseted links
			currentValueConverter.filterInternalValueForLinks(lineHolder);

			return currentValueConverter.displayInternalValue(lineHolder);
		}

		/**
		 * @author elecomte
		 * @since v0.0.8
		 * @version 1
		 */
		private static final class DisplayValue implements Value {

			private final String name;
			private final byte[] value;
			private final ColumnType type;

			/**
			 * @param name
			 * @param value
			 * @param type
			 */
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
}
