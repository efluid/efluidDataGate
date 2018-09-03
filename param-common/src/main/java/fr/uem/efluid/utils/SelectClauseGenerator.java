package fr.uem.efluid.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.uem.efluid.model.shared.ExportAwareDictionaryEntry;
import fr.uem.efluid.model.shared.ExportAwareTableLink;
import fr.uem.efluid.model.shared.ExportAwareTableMapping;

/**
 * <p>
 * Root feature for generation of select clause used in Dictionary Entry model
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SelectClauseGenerator {

	public static final String DEFAULT_WHERE_CLAUSE = "1=1";

	protected static final String ITEM_PROTECT = "\"";

	protected static final String CURRENT_TAB_ALIAS = "cur.";

	protected static final String LINK_TAB_ALIAS = "ln";

	public static final String LINK_TAB_REFLAP = LINK_TAB_ALIAS.toUpperCase() + "_";

	protected static final String LINK_VAL_ALIAS_START = " as " + LINK_TAB_ALIAS + "_";

	protected static final String SELECT_CLAUSE_SEP = ", ";

	protected static final String SELECT_CLAUSE_SEP_PROTECT = ITEM_PROTECT + SELECT_CLAUSE_SEP + ITEM_PROTECT;

	protected static final String SELECT_CLAUSE_SEP_NO_PROTECT_ALIAS = SELECT_CLAUSE_SEP + CURRENT_TAB_ALIAS;

	protected static final String SELECT_CLAUSE_SEP_PROTECT_ALIAS = ITEM_PROTECT + SELECT_CLAUSE_SEP_NO_PROTECT_ALIAS + ITEM_PROTECT;

	protected static final String DEFAULT_SELECT_CLAUSE = "*";

	protected static final String KEY_JOIN_SPLITER = " \\/ ";

	protected static final int SELECT_CLAUSE_FIRST_COL_PROTECT = 1 + CURRENT_TAB_ALIAS.length();
	protected static final int SELECT_CLAUSE_FIRST_COL_NO_PROTECT = CURRENT_TAB_ALIAS.length();

	protected final boolean protectColumns;
	private final String selectLinkValueModel;

	/**
	 * @param protectColumns
	 * @param selectLinkValueModel
	 */
	public SelectClauseGenerator(boolean protectColumns) {
		super();
		this.protectColumns = protectColumns;
		this.selectLinkValueModel = generateSelectLinkValue(protectColumns);
	}

	/**
	 * @param selectClause
	 * @return
	 */
	public Collection<String> splitSelectClause(String selectClause, List<? extends ExportAwareTableLink<?>> links,
			Map<String, ? extends ExportAwareDictionaryEntry<?>> allEntries) {

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
	 * Includes link alias set with support for composite keys
	 * </p>
	 * 
	 * @param link
	 * @return
	 */
	protected String createLinkAlias(ExportAwareTableLink<?> link) {
		return null;
	}

	/**
	 * <p>
	 * Includes mapping alias set with support for composite keys
	 * </p>
	 * 
	 * @param mapping
	 * @return
	 */
	protected String createMappingAlias(ExportAwareTableMapping<?> mapping) {
		return null;
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
	 * @param links
	 * @param mappings
	 *            to be used in query building
	 * @param allEntries
	 * @return the select part of the query, ready to be saved
	 */
	public String mergeSelectClause(
			List<String> selectedColumnNames,
			int availableColumnNumber,
			List<? extends ExportAwareTableLink<?>> links,
			List<? extends ExportAwareTableMapping<?>> mappings, // TODO : use this
			Map<String, ? extends ExportAwareDictionaryEntry<?>> allEntries) {

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
	 * <p>
	 * Join columns as simple select, without alias, and with support for "protectColumn".
	 * For example, values {aaa,bbb,ccc} becames <code>"aaa", "bbb", "ccc"</code>
	 * </p>
	 * 
	 * @param columnNames
	 * @return
	 */
	protected String prepareSimpleSelectPart(Collection<String> columnNames) {

		return columnNames.stream()
				.map(s -> this.protectColumns ? "\"" + s + "\"" : s)
				.collect(Collectors.joining(SELECT_CLAUSE_SEP));
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
	protected static boolean hasMappedLinks(List<? extends ExportAwareTableLink<?>> links,
			Map<String, ? extends ExportAwareDictionaryEntry<?>> allEntries) {
		return links != null && links.stream().anyMatch(l -> allEntries.containsKey(l.getTableTo()));
	}

	/**
	 * @param links
	 * @param allEntries
	 * @return
	 */
	private Map<String, String> prepareSelectLinks(List<? extends ExportAwareTableLink<?>> links,
			Map<String, ? extends ExportAwareDictionaryEntry<?>> allEntries) {

		AtomicInteger pos = new AtomicInteger(0);
		Map<String, String> prepared = new HashMap<>();

		links.stream().filter(l -> allEntries.containsKey(l.getTableTo())).sorted(linkOrder()).forEach(
				l -> {

					// Referenced table
					ExportAwareDictionaryEntry<?> dic = allEntries.get(l.getTableTo());

					// Special case (rare) - composite
					if (l.isCompositeKey()) {

						int linkPos = pos.incrementAndGet();
						// Mark as aliased for each column From
						l.columnFroms().forEach(c -> {
							prepared.put(c, prepareSelectLinkColumnAlias(dic, linkPos, c));
						});
					}

					// Standard case
					else {
						prepared.put(l.getColumnFrom(), prepareSelectLinkColumnAlias(dic, pos.incrementAndGet(), l.getColumnFrom()));
					}
				});

		return prepared;
	}

	/**
	 * <p>
	 * For one column ref in a link. Repeated for each column keys in case of composite
	 * keys
	 * </p>
	 * 
	 * @param dic
	 * @param pos
	 * @param columnFrom
	 * @return
	 */
	private String prepareSelectLinkColumnAlias(ExportAwareDictionaryEntry<?> dic, int pos, String columnFrom) {
		// ln%s."%s" as ln_%s
		return String.format(this.selectLinkValueModel, String.valueOf(pos), dic.getKeyName(),
				columnFrom);
	}

	/**
	 * Join selected value
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateSelectLinkValue(boolean columnNamesProtected) {
		return new StringBuilder(LINK_TAB_ALIAS + "%s.").append(columnNamesProtected ? "\"%s\"" : "%s")
				.append(LINK_VAL_ALIAS_START + "%s ").toString();
	}

	protected static <T extends ExportAwareTableLink<?>> Comparator<T> linkOrder() {
		return Comparator.comparing(ExportAwareTableLink::getTableTo);
	}
}
