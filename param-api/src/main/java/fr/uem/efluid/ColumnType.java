package fr.uem.efluid;

import java.security.Timestamp;
import java.sql.Date;
import java.sql.Types;
import java.time.temporal.Temporal;

/**
 * <p>
 * Dedicated simplified type of column. As we manage the index value in strings, the
 * parsing model can be effective even if limited to these 3 types :
 * <ul>
 * <li>{@link #STRING} means <i>"something we output in a SQL query as a string, like with
 * <code>a.col = 'something'</code>"</i>. Used for varchar, nvarchar, char. Clob should be
 * supported but as we may have a dedicated management of CLOB / BLOB values in index,
 * they are specified as "BINARY". <b><font color="green">Tested OK with NVARCHAR(X),
 * VARCHAR(X) and VARCHAR2(X) SQL types</font></b></li>
 * <li>{@link #ATOMIC} means <i>"something we output in a SQL query as an atomic, like
 * with <code>a.col = 1234</code>"</i>. Used for numbers (in java, nearly all primitive
 * types). <b><font color="green">Tested OK with SQL NUMBER, SMALLINT,
 * FLOAT</font></b></li>
 * <li>{@link #BINARY} means <i>"something we need to manage with a special process in a
 * SQL query as it is not a scalare value inlined in a query"</i>. Used for BLOB (and CLOB
 * ??). <b><font color="green">Tested OK for SQL BYTEA and BLOB</font></b></li>
 * <li>{@link #BOOLEAN} for boolean only (required for specific value extraction).
 * <b><font color="green">Tested OK for SQL BOOLEAN (on PGSQL).</font> Number-based
 * boolean (like for Oracle) are specified as <code>ATOMIC</code></b></li>
 * <li>{@link #TEMPORAL} for time / timestamp / date (required for specific value
 * extraction with format : date are formated internaly in ISO string format, then
 * provided in user-specified database-related format in query
 * generation).<b><font color="green">Tested OK for SQL TIMESTAMP</font></b></li>
 * <li>{@link #PK_ATOMIC} means <i>"something with a generated value I shouldn't share
 * with other database instances"</i>. Used to identify the internal PK, which is generaly
 * NOT selected for dictionary entry managing. Identification is not based on type but on
 * METADATA</li>
 * <li>{@link #PK_STRING} is a distinction on PK_ATOMIC when value is managed
 * string-like</li>
 * </ul>
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
public enum ColumnType {

    // TODO : If CLOB and BLOB cannot be managed the same way, use a dedicated type "TEXT"
    BINARY('B', "LOB", false),
    TEXT('X', "TEXT", false),
    ATOMIC('O', "Variable", false),
    STRING('S', "Litteral", false),
    BOOLEAN('1', "Booleen", false),
    TEMPORAL('T', "Temporal", false),
    UNKNOWN('U', "Unknown", false),
    PK_ATOMIC('!', "Identifiant", true),
    PK_STRING('§', "Identifiant", true);

    private final char represent;
    private final String displayName;
    private final boolean pk;

    /**
     * @param represent
     */
    private ColumnType(char represent, String displayName, boolean pk) {
        this.represent = represent;
        this.displayName = displayName;
        this.pk = pk;
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
     * @return the pk
     */
    public boolean isPk() {
        return this.pk;
    }

    /**
     * <p>
     * Utils for getting the corresponding type on an existing object
     * </p>
     *
     * @param obj
     * @return
     */
    public static ColumnType forClass(Class<?> obj) {

        if (obj == String.class || obj == char.class) {
            return ColumnType.STRING;
        }

        if (obj == byte[].class) {
            return ColumnType.BINARY;
        }

        if (obj == boolean.class || obj == Boolean.class) {
            return ColumnType.BOOLEAN;
        }

        if (Temporal.class.isAssignableFrom(obj) || Date.class.isAssignableFrom(obj) || Timestamp.class.isAssignableFrom(obj)) {
            return ColumnType.TEMPORAL;
        }

        return ColumnType.ATOMIC;
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

        if (obj instanceof Temporal) {
            return ColumnType.TEMPORAL;
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

        if (represent == TEXT.represent) {
            return ColumnType.TEXT;
        }

        if (represent == ATOMIC.represent) {
            return ColumnType.ATOMIC;
        }

        if (represent == BOOLEAN.represent) {
            return ColumnType.BOOLEAN;
        }

        if (represent == TEMPORAL.represent) {
            return ColumnType.TEMPORAL;
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

        // A Small range for temporals
        if (type >= Types.DATE && type <= Types.TIMESTAMP) {
            return ColumnType.TEMPORAL;
        }

        // A 1st Small range of binaries
        if (type >= Types.LONGVARBINARY && type <= Types.BINARY) {
            return ColumnType.BINARY;
        }

        // Explicit value for char
        if (type == Types.CHAR) {
            return ColumnType.STRING;
        }

        // Specific for CLOB
        if (type == Types.CLOB) {
            return ColumnType.TEXT;
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
