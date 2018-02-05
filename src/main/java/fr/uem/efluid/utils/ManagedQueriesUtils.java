package fr.uem.efluid.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * <p>
 * Tools for query build used in Managed database access. Contains query building
 * processes for managed value extraction. Some helpers provides also solution for
 * building stored part of the Managed source details.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ManagedQueriesUtils {

	public static final String DEFAULT_WHERE_CLAUSE = "1=1";

	private static final String SELECT_CLAUSE_SEP = ", ";

	private static final String DEFAULT_SELECT_CLAUSE = "*";

	private static final String SELECT_QUERY_MODEL = "SELECT %s FROM \"%s\" WHERE %s ORDER BY \"%s\"";

	/**
	 * @param parameterEntry
	 * @return
	 */
	public static String consolidateSelectClause(DictionaryEntry parameterEntry) {

		// Basic consolidate => Select all
		if (parameterEntry.getSelectClause() == null) {
			return DEFAULT_SELECT_CLAUSE;
		}

		// If keyname not in select clause, need to add it
		if (!parameterEntry.getSelectClause().contains(parameterEntry.getKeyName())) {
			return parameterEntry.getKeyName() + SELECT_CLAUSE_SEP + parameterEntry.getSelectClause();
		}

		return parameterEntry.getSelectClause();
	}

	/**
	 * @param parameterEntry
	 * @return
	 */
	public static String producesSelectParameterQuery(DictionaryEntry parameterEntry) {

		// Need clean select for uses
		String selectClause = consolidateSelectClause(parameterEntry);

		return String.format(SELECT_QUERY_MODEL,
				selectClause,
				parameterEntry.getTableName(),
				parameterEntry.getWhereClause(),
				parameterEntry.getKeyName());
	}

	/**
	 * @return
	 */
	public static Collector<CharSequence, ?, String> collectToSelectClause() {
		return Collectors.joining(SELECT_CLAUSE_SEP);
	}

	/**
	 * @param selectClause
	 * @return
	 */
	public static Collection<String> splitSelectClause(String selectClause) {
		return Arrays.asList(selectClause.split(SELECT_CLAUSE_SEP));
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
	public static String mergeSelectClause(Stream<String> selectedColumnNames, int availableColumnNumber) {
		return selectedColumnNames.count() == availableColumnNumber ? DEFAULT_SELECT_CLAUSE
				: selectedColumnNames.collect(ManagedQueriesUtils.collectToSelectClause());
	}
}
