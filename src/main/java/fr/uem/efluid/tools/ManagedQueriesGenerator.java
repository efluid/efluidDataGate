package fr.uem.efluid.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.model.entities.DictionaryEntry;

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
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Component
public class ManagedQueriesGenerator {

	public static final String DEFAULT_WHERE_CLAUSE = "1=1";

	private static final String ITEM_PROTECT = "\"";

	private static final String SELECT_CLAUSE_SEP_NO_PROTECT = ", ";

	private static final String SELECT_CLAUSE_SEP_PROTECT = ITEM_PROTECT + SELECT_CLAUSE_SEP_NO_PROTECT + ITEM_PROTECT;

	private static final String DEFAULT_SELECT_CLAUSE = "*";

	private final boolean protectColumns;

	private final String selectQueryModel;

	/**
	 * Prepare generator regarding the specified rules
	 * 
	 * @param rules
	 */
	public ManagedQueriesGenerator(@Autowired QueryGenerationRules rules) {
		this.protectColumns = rules.isColumnNamesProtected();
		this.selectQueryModel = generateSelectQueryTemplate(rules);
	}

	/**
	 * @param parameterEntry
	 * @return
	 */
	public String consolidateSelectClause(DictionaryEntry parameterEntry) {

		// Basic consolidate => Select all
		if (parameterEntry.getSelectClause() == null) {
			return DEFAULT_SELECT_CLAUSE;
		}

		// If keyname not in select clause, need to add it
		if (!parameterEntry.getSelectClause().contains(parameterEntry.getKeyName())) {
			return (this.protectColumns)
					? ITEM_PROTECT + parameterEntry.getKeyName() + ITEM_PROTECT + SELECT_CLAUSE_SEP_NO_PROTECT
							+ parameterEntry.getSelectClause()
					: parameterEntry.getKeyName() + SELECT_CLAUSE_SEP_NO_PROTECT + parameterEntry.getSelectClause();
		}

		return parameterEntry.getSelectClause();
	}

	/**
	 * @param parameterEntry
	 * @return
	 */
	public String producesSelectParameterQuery(DictionaryEntry parameterEntry) {

		// Need clean select for uses
		String selectClause = consolidateSelectClause(parameterEntry);

		return String.format(this.selectQueryModel,
				selectClause,
				parameterEntry.getTableName(),
				parameterEntry.getWhereClause(),
				parameterEntry.getKeyName());
	}

	/**
	 * @param selectClause
	 * @return
	 */
	public Collection<String> splitSelectClause(String selectClause) {

		if (this.protectColumns) {
			return Arrays.asList(selectClause.substring(1, selectClause.length() - 1).split(SELECT_CLAUSE_SEP_PROTECT));
		}

		return Arrays.asList(selectClause.split(SELECT_CLAUSE_SEP_NO_PROTECT));
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
	public String mergeSelectClause(Stream<String> selectedColumnNames, int availableColumnNumber) {

		if (selectedColumnNames.count() == availableColumnNumber) {
			return DEFAULT_SELECT_CLAUSE;
		}

		if (this.protectColumns) {
			return ITEM_PROTECT + selectedColumnNames.collect(Collectors.joining(SELECT_CLAUSE_SEP_PROTECT)) + ITEM_PROTECT;
		}

		return selectedColumnNames.collect(Collectors.joining(SELECT_CLAUSE_SEP_NO_PROTECT));
	}

	/**
	 * Generate the template regarding the rules on protect / not protected
	 * 
	 * @param rules
	 * @return
	 */
	private static final String generateSelectQueryTemplate(QueryGenerationRules rules) {
		return new StringBuilder("SELECT %s FROM ").append(rules.isTableNamesProtected() ? "\"%s\"" : "%s").append(" WHERE %s ORDER BY ")
				.append(rules.isColumnNamesProtected() ? "\"%s\"" : "%s").toString();
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
	}

}
