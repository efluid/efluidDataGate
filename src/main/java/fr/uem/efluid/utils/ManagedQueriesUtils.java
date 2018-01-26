package fr.uem.efluid.utils;

import fr.uem.efluid.model.entities.DictionaryEntry;

/**
 * Tools for query build used in Managed database access. For backlog identification
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ManagedQueriesUtils {

	private static final String SELECT_CLAUSE_SEP = ", ";

	/**
	 * @param parameterEntry
	 * @return
	 */
	public static String consolidateSelectClause(DictionaryEntry parameterEntry) {

		// Basic consolidate => Select all
		if (parameterEntry.getSelectClause() == null) {
			return "*";
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

		return String.format("SELECT %s FROM %s WHERE %s ORDER BY %s",
				selectClause,
				parameterEntry.getTableName(),
				parameterEntry.getWhereClause(),
				parameterEntry.getKeyName());
	}
}
