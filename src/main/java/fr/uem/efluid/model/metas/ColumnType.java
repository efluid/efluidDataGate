package fr.uem.efluid.model.metas;

import java.sql.Types;

/**
 * <p>
 * Dedicated simplified type of column. As we manage the index value in strings, the
 * parsing model can be effective even if limited to these 3 types :
 * <ul>
 * <li>{@link #STRING} means <i>"something we output in a SQL query as a string, like with
 * <code>a.col = 'something'</code>"</i>. Used for varchar, nvarchar, char, and all
 * date-related types, like datatime. Clob should be supported but as we may have a
 * dedicated management of CLOB / BLOB values in index, they are specified as
 * "BINARY".</li>
 * <li>{@link #ATOMIC} means <i>"something we output in a SQL query as an atomic, like
 * with <code>a.col = 1234</code>"</i>. Used for numbers and booleans (in java, nearly all
 * primitive types)</li>
 * <li>{@link #BINARY} means <i>"something we need to manage with a special process in a
 * SQL query as it is not a scalare value inlined in a query"</i>. Used for BLOB (and CLOB
 * ??)</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public enum ColumnType {

	// TODO : If CLOB and BLOC cannot be managed the same way, use a dedicated type "TEXT"
	BINARY('B'),
	ATOMIC('O'),
	STRING('S');

	private final char represent;

	/**
	 * @param represent
	 */
	private ColumnType(char represent) {
		this.represent = represent;
	}

	/**
	 * @return the represent
	 */
	public char getRepresent() {
		return this.represent;
	}

	/**
	 * <p>
	 * Utils for getting the corresponding type on an existing object
	 * </p>
	 * 
	 * @param obj
	 * @return
	 */
	public static ColumnType forObject(Object obj) {
		if (obj instanceof String) {
			return ColumnType.STRING;
		}

		if (obj instanceof byte[]) {
			return ColumnType.BINARY;
		}

		return ColumnType.ATOMIC;
	}

	/**
	 * Shortcut to Type from its represent value
	 * 
	 * @param represent
	 * @return
	 */
	public static ColumnType forRepresent(char represent) {

		if (represent == STRING.represent) {
			return ColumnType.STRING;
		}

		if (represent == ATOMIC.represent) {
			return ColumnType.ATOMIC;
		}

		return BINARY;
	}

	/**
	 * @param type
	 * @return the internal type associated to
	 */
	public static ColumnType forJdbcType(int type) {

		/*
		 * Improved search algorytm based on JDBC SQL Types model, using direct int value
		 * check for range validation. Indeed, Types values are "almost" well organized,
		 * with "big types" in limited ranges.
		 */

		// Explicit identification by type range
		if ((type >= Types.ROWID && type <= Types.DOUBLE) || type == Types.BOOLEAN) {
			return ColumnType.ATOMIC;
		}

		// Anything else remeaning <= Types.ARRAY (2003) ar strings
		if (type <= Types.ARRAY) {
			return ColumnType.STRING;
		}

		// Other (> Types.OTHER) ar large binaries
		return ColumnType.BINARY;
	}
}
