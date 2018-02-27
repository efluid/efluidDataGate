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
 * <li>{@link #BOOLEAN} for boolean only (required for specific value extraction)</li>
 * <li>{@link #PK} means <i>"something with a generated value I shouldn't share with other
 * database instances"</i>. Used to identify the</li>
 * </ul>
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public enum ColumnType {

	// TODO : If CLOB and BLOC cannot be managed the same way, use a dedicated type "TEXT"
	BINARY('B', "LOB"),
	ATOMIC('O', "Variable"),
	STRING('S', "Literal"),
	BOOLEAN('1', "Booleen"),
	PK('!', "Identifiant");

	private final char represent;
	private final String displayName;

	/**
	 * @param represent
	 */
	private ColumnType(char represent, String displayName) {
		this.represent = represent;
		this.displayName = displayName;
	}

	/**
	 * @return the represent
	 */
	public char getRepresent() {
		return this.represent;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return this.displayName;
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

		if (obj instanceof Boolean) {
			return ColumnType.BOOLEAN;
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

		if (represent == BOOLEAN.represent) {
			return ColumnType.BOOLEAN;
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

		// Explicit codes for boolean
		if (type == Types.BOOLEAN || type == Types.BIT) {
			return ColumnType.BOOLEAN;
		}

		// A 1st Small range of binaries
		if (type >= Types.LONGVARBINARY && type <= Types.BINARY) {
			return ColumnType.BINARY;
		}

		// Explicit identification by type range
		if ((type >= Types.ROWID && type <= Types.DOUBLE)) {
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
