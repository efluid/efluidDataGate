package fr.uem.efluid.tools;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.model.entities.DictionaryEntry;
import fr.uem.efluid.model.entities.TableLink;
import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;

/**
 * <p>
 * Tools for query build used in Managed database access. Contains query building
 * processes for managed value extraction. Some helpers provides also solution for
 * building stored part of the Managed source details.
 * </p>
 * <p>
 * <b>All query items can be "protected" with double quotes</b> (validated as default rule
 * for Postgres, and supported on Oracle and MS SQL Server). So a select is always like
 * this : <code>select "colA", "colB", "colC" from "Table" where "colA"='truc'</code>.
 * This feature is optional : all behaviors are specified from available
 * <tt>QueryGenerationRules</tt> initialized in Spring context
 * </p>
 * <p>
 * For mapping, a dedicated generation is used when the linked table is itself a parameter
 * table.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class ManagedQueriesGenerator {

	public static final String DEFAULT_WHERE_CLAUSE = "1=1";

	private static final String ITEM_PROTECT = "\"";

	private static final String CURRENT_TAB_ALIAS = "cur.";

	private static final String LINK_TAB_ALIAS = "ln";

	private static final String LINK_TAB_REFLAP = LINK_TAB_ALIAS.toUpperCase() + "_";

	private static final String LINK_VAL_ALIAS_START = " as " + LINK_TAB_ALIAS + "_";

	private static final String SELECT_CLAUSE_SEP = ", ";

	private static final String SELECT_CLAUSE_SEP_PROTECT = ITEM_PROTECT + SELECT_CLAUSE_SEP + ITEM_PROTECT;

	private static final String SELECT_CLAUSE_SEP_NO_PROTECT_ALIAS = SELECT_CLAUSE_SEP + CURRENT_TAB_ALIAS;

	private static final String SELECT_CLAUSE_SEP_PROTECT_ALIAS = ITEM_PROTECT + SELECT_CLAUSE_SEP_NO_PROTECT_ALIAS + ITEM_PROTECT;

	private static final String DEFAULT_SELECT_CLAUSE = "*";

	private static final String AFFECT = "=";

	private static final int SELECT_CLAUSE_FIRST_COL_PROTECT = 1 + CURRENT_TAB_ALIAS.length();
	private static final int SELECT_CLAUSE_FIRST_COL_NO_PROTECT = CURRENT_TAB_ALIAS.length();

	private final boolean protectColumns;

	private final String selectQueryModel;
	private final String insertQueryModel;
	private final String updateQueryModel;
	private final String deleteQueryModel;
	private final String unicityQueryModel;
	private final String selectJoinSubQueryModel;
	private final String updateOrInsertLinkedSubQueryModel;
	private final String selectLinkValueModel;
	private final DateTimeFormatter dbDateFormater;

	/**
	 * Prepare generator regarding the specified rules
	 * 
	 * @param rules
	 */
	public ManagedQueriesGenerator(@Autowired QueryGenerationRules rules) {
		this.protectColumns = rules.isColumnNamesProtected();
		this.selectQueryModel = generateSelectQueryTemplate(rules);
		this.insertQueryModel = generateInsertQueryTemplate(rules);
		this.updateQueryModel = generateUpdateQueryTemplate(rules);
		this.deleteQueryModel = generateDeleteQueryTemplate(rules);
		this.unicityQueryModel = generateUnicityQueryTemplate(rules);
		this.selectJoinSubQueryModel = generateSelectJoinSubQueryTemplate(rules);
		this.updateOrInsertLinkedSubQueryModel = generateUpdateOrInsertLinkedSubQueryTemplate(rules);
		this.selectLinkValueModel = generateSelectLinkValue(rules);
		this.dbDateFormater = DateTimeFormatter.ofPattern(rules.getDatabaseDateFormat());
	}

	/**
	 * @param parameterEntry
	 * @return
	 */
	public String producesSelectParameterQuery(DictionaryEntry parameterEntry, List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		// Need clean select for uses
		String selectClause = consolidateSelectClause(parameterEntry);

		// For inner linked (select contains them already)
		String joinClauses = prepareJoinLinks(links, allEntries);

		return String.format(this.selectQueryModel,
				selectClause,
				parameterEntry.getTableName(),
				joinClauses,
				parameterEntry.getWhereClause(),
				parameterEntry.getKeyName());
	}

	/**
	 * @param selectClause
	 * @return
	 */
	public Collection<String> splitSelectClause(String selectClause, List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		// When links are mapped, use a custom process
		if (hasMappedLinks(links, allEntries)) {
			return Stream.of(selectClause.split(SELECT_CLAUSE_SEP)).map(s -> {

				// It's an alias : will provide the "from" col, embedded in alias
				if (s.indexOf(LINK_TAB_ALIAS) == 0) {
					return s.substring(s.indexOf(LINK_VAL_ALIAS_START) + LINK_VAL_ALIAS_START.length()).trim();
				}

				// Else, use normal process
				if (this.protectColumns) {
					return s.substring(SELECT_CLAUSE_FIRST_COL_PROTECT, s.length() - 1);
				}

				return s;
			}).collect(Collectors.toList());
		}

		if (this.protectColumns) {
			return Arrays.asList(
					selectClause.substring(SELECT_CLAUSE_FIRST_COL_PROTECT, selectClause.length() - 1)
							.split(SELECT_CLAUSE_SEP_PROTECT_ALIAS));
		}

		return Arrays.asList(selectClause.substring(SELECT_CLAUSE_FIRST_COL_NO_PROTECT).split(SELECT_CLAUSE_SEP_NO_PROTECT_ALIAS));
	}

	/**
	 * <p>
	 * Produces the select part of the query when updating the column selection. Switchs
	 * automatically between "identified select", or "*"
	 * </p>
	 * 
	 * @param selectedColumnNames
	 *            the filtered selected column names
	 * @param availableColumnNumber
	 *            the total number of column available for current managed source table
	 * @return the select part of the query, ready to be saved
	 */
	public String mergeSelectClause(List<String> selectedColumnNames, int availableColumnNumber, List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		if (selectedColumnNames.size() == availableColumnNumber) {
			return DEFAULT_SELECT_CLAUSE;
		}

		// Dedicated process if has mapped links for cleaner management
		if (hasMappedLinks(links, allEntries)) {

			StringBuilder select = new StringBuilder();

			Map<String, String> selectLinks = prepareSelectLinks(links, allEntries);
			int last = selectedColumnNames.size() - 1;

			for (String col : selectedColumnNames) {

				String linked = selectLinks.get(col);

				if (linked == null) {
					if (this.protectColumns) {
						select.append(CURRENT_TAB_ALIAS).append(ITEM_PROTECT).append(col).append(ITEM_PROTECT);
					} else {
						select.append(CURRENT_TAB_ALIAS).append(col);
					}
				} else {
					select.append(linked);
				}

				if (last > 0) {
					select.append(SELECT_CLAUSE_SEP);
				}

				last--;
			}

			return select.toString();
		}

		// No linkeds : default select
		if (this.protectColumns) {
			return CURRENT_TAB_ALIAS + ITEM_PROTECT
					+ selectedColumnNames.stream().collect(Collectors.joining(SELECT_CLAUSE_SEP_PROTECT_ALIAS))
					+ ITEM_PROTECT;
		}

		return CURRENT_TAB_ALIAS + selectedColumnNames.stream().collect(Collectors.joining(SELECT_CLAUSE_SEP_NO_PROTECT_ALIAS));
	}

	/**
	 * @param parameterEntry
	 * @param keyValue
	 * @param values
	 * @return
	 */
	public String producesApplyAddQuery(
			DictionaryEntry parameterEntry,
			String keyValue,
			List<Value> values,
			List<String> referedLobs,
			List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {
		// INSERT INTO %s (%) VALUES (%)
		List<Value> combined = new ArrayList<>();
		combined.add(new KeyValue(parameterEntry, keyValue));
		combined.addAll(values);
		return String.format(this.insertQueryModel, parameterEntry.getTableName(), allNames(combined, links, allEntries),
				allValues(combined, referedLobs, links, allEntries));
	}

	/**
	 * @param parameterEntry
	 * @param keyValue
	 * @return
	 */
	public String producesApplyRemoveQuery(DictionaryEntry parameterEntry, String keyValue) {
		// DELETE FROM %s WHERE %s
		return String.format(this.deleteQueryModel, parameterEntry.getTableName(),
				valueAffect(new KeyValue(parameterEntry, keyValue), null));
	}

	/**
	 * @param parameterEntry
	 * @param keyValue
	 * @param values
	 * @return
	 */
	public String producesApplyUpdateQuery(DictionaryEntry parameterEntry,
			String keyValue,
			List<Value> values,
			List<String> referedLobs,
			List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {
		// UPDATE %s SET %s WHERE %s
		return String.format(this.updateQueryModel, parameterEntry.getTableName(), allValuesAffect(values, referedLobs, links, allEntries),
				valueAffect(new KeyValue(parameterEntry, keyValue), null));
	}

	/**
	 * @param parameterEntry
	 * @param keyValue
	 * @return
	 */
	public String producesGetOneQuery(DictionaryEntry parameterEntry, String keyValue) {
		// SELECT %s FROM %s WHERE %s ORDER BY %s (reused query)
		return String.format(this.selectQueryModel, "1", parameterEntry.getTableName(), "",
				valueAffect(new KeyValue(parameterEntry, keyValue), null),
				parameterEntry.getKeyName());
	}

	/**
	 * To check unicity on parameter key
	 * 
	 * @param tablename
	 * @param columnName
	 * @return
	 */
	public String producesUnicityQuery(String tablename, String columnName) {

		// select 1 from "TTABLEOTHERTEST2" group by "ID" HAVING COUNT("ID") > 1
		return String.format(this.unicityQueryModel, tablename, columnName, columnName);
	}

	/**
	 * @param value
	 * @return
	 */
	private String valueAffect(Value value, List<String> lobsKey) {
		if (this.protectColumns) {
			return ITEM_PROTECT + value.getName() + ITEM_PROTECT + AFFECT + value.getTyped(lobsKey, this.dbDateFormater);
		}
		return value.getName() + AFFECT + value.getTyped(lobsKey, this.dbDateFormater);
	}

	/**
	 * @param values
	 * @return
	 */
	private String allNames(List<Value> values,
			List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		Stream<String> names;

		// Dedicated when mapped links exist
		if (hasMappedLinks(links, allEntries)) {

			names = values.stream().map(v -> {

				int lnStart = v.getName().indexOf(LINK_TAB_REFLAP);

				if (lnStart >= 0) {
					String propName = v.getName().substring(lnStart + LINK_TAB_REFLAP.length());

					// Has a link for propName : correct link attribute
					Optional<TableLink> link = links.stream().filter(l -> l.getColumnFrom().equals(propName)).findFirst();
					if (link.isPresent()) {
						return propName;
					}
				}

				return v.getName();

			});

		} else {
			names = values.stream().map(Value::getName);
		}

		if (this.protectColumns) {
			return ITEM_PROTECT + names.collect(Collectors.joining(SELECT_CLAUSE_SEP_PROTECT)) + ITEM_PROTECT;
		}
		return names.collect(Collectors.joining(SELECT_CLAUSE_SEP));
	}

	/**
	 * @param values
	 * @return
	 */
	private String allValuesAffect(
			List<Value> values,
			List<String> lobKeys,
			List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		// col = val

		// Dedicated when mapped links exist
		if (hasMappedLinks(links, allEntries)) {

			return values.stream().map(v -> {

				int lnStart = v.getName().indexOf(LINK_TAB_REFLAP);

				if (lnStart >= 0) {
					String propName = v.getName().substring(lnStart + LINK_TAB_REFLAP.length());
					return propName + AFFECT + linkValueSubSelect(propName, v.getValueAsString(), links, allEntries);
				}

				return valueAffect(v, lobKeys);

			}).collect(Collectors.joining(SELECT_CLAUSE_SEP));

		}

		return values.stream().map(v -> valueAffect(v, lobKeys)).collect(Collectors.joining(SELECT_CLAUSE_SEP));
	}

	/**
	 * @param values
	 * @return
	 */
	private String allValues(
			List<Value> values,
			List<String> lobKeys,
			List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		// Dedicated when mapped links exist
		if (hasMappedLinks(links, allEntries)) {

			return values.stream().map(v -> {

				int lnStart = v.getName().indexOf(LINK_TAB_REFLAP);

				if (lnStart >= 0) {
					return linkValueSubSelect(v.getName().substring(lnStart + LINK_TAB_REFLAP.length()), v.getValueAsString(), links,
							allEntries);
				}

				return v.getTyped(lobKeys, this.dbDateFormater);

			}).collect(Collectors.joining(SELECT_CLAUSE_SEP));
		}

		return values.stream().map(v -> v.getTyped(lobKeys, this.dbDateFormater)).collect(Collectors.joining(SELECT_CLAUSE_SEP));
	}

	/**
	 * 
	 * @param linkedAttr
	 * @param value
	 * @param links
	 * @param allEntries
	 * @return
	 */
	private String linkValueSubSelect(
			String linkedAttr,
			String value,
			List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		TableLink link = links.stream().filter(l -> l.getColumnFrom().equals(linkedAttr)).findFirst().orElseThrow(
				() -> new ApplicationException(ErrorType.REFER_MISS_LINK, "Unknown value link : " + linkedAttr, linkedAttr));

		DictionaryEntry dic = allEntries.get(link.getTableTo());

		// (SELECT "%s" FROM "%s" WHERE %s)
		return String.format(this.updateOrInsertLinkedSubQueryModel, link.getColumnTo(),
				dic.getTableName(), valueAffect(new KeyValue(dic, value), null));
	}

	/**
	 * @param parameterEntry
	 * @return
	 */
	private String consolidateSelectClause(DictionaryEntry parameterEntry) {

		// Basic consolidate => Select all
		if (parameterEntry.getSelectClause() == null || parameterEntry.getSelectClause().length() == 0) {
			return DEFAULT_SELECT_CLAUSE;
		}

		// If keyname not in select clause, need to add it
		if (!parameterEntry.getSelectClause().contains(parameterEntry.getKeyName())) {
			return (this.protectColumns)
					? CURRENT_TAB_ALIAS + ITEM_PROTECT + parameterEntry.getKeyName() + ITEM_PROTECT + SELECT_CLAUSE_SEP
							+ parameterEntry.getSelectClause()
					: CURRENT_TAB_ALIAS + parameterEntry.getKeyName() + SELECT_CLAUSE_SEP + parameterEntry.getSelectClause();
		}

		return parameterEntry.getSelectClause();
	}

	/**
	 * @param links
	 * @param allEntries
	 * @return
	 */
	private Map<String, String> prepareSelectLinks(List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		AtomicInteger pos = new AtomicInteger(0);

		return links.stream().filter(l -> allEntries.containsKey(l.getTableTo())).collect(Collectors.toMap(
				TableLink::getColumnFrom,
				l -> {
					DictionaryEntry dic = allEntries.get(l.getTableTo());
					// ln%s."%s" as ln_%s
					return String.format(this.selectLinkValueModel, String.valueOf(pos.incrementAndGet()), dic.getKeyName(),
							l.getColumnFrom());
				}));
	}

	/**
	 * @param links
	 * @param allEntries
	 * @return
	 */
	private String prepareJoinLinks(List<TableLink> links,
			Map<String, DictionaryEntry> allEntries) {

		AtomicInteger pos = new AtomicInteger(0);

		return links.stream().filter(l -> allEntries.containsKey(l.getTableTo())).map(l -> {
			DictionaryEntry dic = allEntries.get(l.getTableTo());
			String alias = LINK_TAB_ALIAS + pos.incrementAndGet();
			// INNER JOIN "%s" %s on %s."%s" = cur."%s"
			return String.format(this.selectJoinSubQueryModel, dic.getTableName(), alias, alias, l.getColumnTo(), l.getColumnFrom());
		}).collect(Collectors.joining(" "));
	}

	/**
	 * <p>
	 * For the dic entry links, check if some are mapped as dictionary entries : if true,
	 * needs to use refered table key instead of internal id
	 * </p>
	 * 
	 * @param links
	 * @param allEntries
	 *            mapped to table name
	 * @return
	 */
	private static boolean hasMappedLinks(List<TableLink> links, Map<String, DictionaryEntry> allEntries) {
		return links != null && links.stream().anyMatch(l -> allEntries.containsKey(l.getTableTo()));
	}

	/**
	 * Generate the template regarding the rules on protect / not protected
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateSelectQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("SELECT %s FROM ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s")
				.append(" cur %s WHERE %s ORDER BY ")
				.append(rules.isColumnNamesProtected() ? "\"%s\"" : "%s").toString();
	}

	/**
	 * Generate the template regarding the rules on protect / not protected
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateUpdateQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("UPDATE ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" SET %s WHERE %s").toString();
	}

	/**
	 * Generate the template regarding the rules on protect / not protected
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateDeleteQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("DELETE FROM ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" WHERE %s ").toString();
	}

	/**
	 * Prepare join part for linked properties on select queries
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateSelectJoinSubQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("INNER JOIN ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" %s ON %s.")
				.append(rules.isColumnNamesProtected() ? "\"%s\"" : "%s").append(" = cur.")
				.append(rules.isColumnNamesProtected() ? "\"%s\"" : "%s").toString();
	}

	/**
	 * Prepare sub select used for value gathered from linked table on insert or update
	 * queries
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateUpdateOrInsertLinkedSubQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("(SELECT ").append(rules.isColumnNamesProtected() ? "\"%s\"" : "%s").append(" FROM ")
				.append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" WHERE %s)").toString();
	}

	/**
	 * Join selected value
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateSelectLinkValue(QueryGenerationRules rules) {
		return new StringBuilder(LINK_TAB_ALIAS + "%s.").append(rules.isColumnNamesProtected() ? "\"%s\"" : "%s")
				.append(LINK_VAL_ALIAS_START + "%s ").toString();
	}

	/**
	 * Generate the template regarding the rules on protect / not protected
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateUnicityQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("SELECT 1 FROM ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" GROUP BY ")
				.append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" HAVING COUNT(")
				.append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(") > 1").toString();
	}

	/**
	 * Generate the template regarding the rules on protect / not protected
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateInsertQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("INSERT INTO ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s")
				.append(" (%s) VALUES (%s)").toString();
	}

	/**
	 * <p>
	 * Specific model for key : allows to easily specify chained values when the key need
	 * to be added
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	private static class KeyValue implements Value {
		private final String keyName;
		private final String keyValue;
		private final ColumnType keyType;

		/**
		 * Key definition is from dict, key value is associated to currently applied
		 * values
		 * 
		 * @param parameterEntry
		 * @param keyValue
		 */
		KeyValue(DictionaryEntry parameterEntry, String keyValue) {
			this.keyName = parameterEntry.getKeyName();
			this.keyType = parameterEntry.getKeyType();
			this.keyValue = keyValue;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.services.types.Value#getName()
		 */
		@Override
		public String getName() {
			return this.keyName;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.services.types.Value#getValue()
		 */
		@Override
		public byte[] getValue() {
			// Not used here.
			return null;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.services.types.Value#getType()
		 */
		@Override
		public ColumnType getType() {
			return this.keyType;
		}

		/**
		 * @return
		 * @see fr.uem.efluid.services.types.Value#getValueAsString()
		 */
		@Override
		public String getValueAsString() {
			return this.keyValue;
		}

	}

	/**
	 * <p>
	 * Specify the options for query generation : allows to adapt behavior with precise
	 * rules when the DB type needs it. Rules can be configuration-based or preselected
	 * from the DB vendor
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static interface QueryGenerationRules {

		/**
		 * <p>
		 * Do we need to put double quote to protect the column name in SQL query?
		 * </p>
		 * 
		 * @return
		 */
		boolean isColumnNamesProtected();

		/**
		 * <p>
		 * Do we need to put double quote to protect the table name in SQL query ?
		 * </p>
		 * 
		 * @return
		 */
		boolean isTableNamesProtected();

		/**
		 * <p>
		 * The specified date format compliant with managed database for string-based
		 * injection
		 * </p>
		 * 
		 * @return
		 */
		String getDatabaseDateFormat();
	}

}
