package fr.uem.efluid.services.types;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.utils.FormatUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * <p>
 * Generic holder for one parameter value, associated to its parameter name
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public interface Value {

    DateTimeFormatter INTERNAL_LDT_FORMATTER = DateTimeFormatter.ofPattern(FormatUtils.DATE_TIME_FORMAT);

    String TO_DATE_CONVERT_BEGIN = "to_date (";

    String TO_DATE_CONVERT_END = ", 'DD-MM-YYYY HH24:MI:SS')";

    char TYPED_STRING_PROTECT = '\'';

    String INJECT_OF_LOB = "?";

    String NULL_VALUE = "null";

    /**
     * @return parameter name
     */
    String getName();

    /**
     * Internally hold value, for processing
     *
     * @return
     */
    byte[] getValue();

    /**
     * @return true if value is a natural string representation (depends on database
     * column type)
     */
    ColumnType getType();

    /**
     * The managed value as a String
     *
     * @return
     */
    default String getValueAsString() {
        byte[] value = getValue();
        if (value == null) {
            return null;
        }
        return new String(value, FormatUtils.CONTENT_ENCODING);
    }

    /**
     * Real content status for a Value, if content is <strong>really</strong> null
     *
     * @return true if <strong>really</strong> null
     */
    default boolean isNull() {
        // Real content can be hold by value or valueAsStr, need to check both
        return getValue() == null && getValueAsString() == null;
    }

    /**
     * @param lobKeys
     * @param dbTemporalFormater
     * @return
     */
    default String getTyped(List<String> lobKeys, DateTimeFormatter dbTemporalFormater) {

        switch (getType()) {
            case NULL:
                return NULL_VALUE;
            case STRING:
            case PK_STRING:
                // Support for null key
                String value = getValueAsString();
                return value != null
                        ? TYPED_STRING_PROTECT + value.replace("'", "''") + TYPED_STRING_PROTECT
                        : "null";
            case TEMPORAL:
                LocalDateTime internal = LocalDateTime.parse(getValueAsString(), INTERNAL_LDT_FORMATTER);
                return TO_DATE_CONVERT_BEGIN + TYPED_STRING_PROTECT + dbTemporalFormater.format(internal) + TYPED_STRING_PROTECT + TO_DATE_CONVERT_END;
            case BINARY:
            case TEXT:
                if (lobKeys != null) {
                    lobKeys.add(getValueAsString());
                    return INJECT_OF_LOB;
                }
            default:
                return getValueAsString();
        }
    }

    /**
     * @return
     */
    default String getTypedForDisplay() {

        if (getType() == ColumnType.STRING || getType() == ColumnType.PK_STRING) {
            return TYPED_STRING_PROTECT + getValueAsString() + TYPED_STRING_PROTECT;
        }

        return getValueAsString();
    }

    /**
     * Simple helper for list of values to get it as a LinkedHashMap (hordered map)
     *
     * @param values
     * @return
     */
    static LinkedHashMap<String, Value> mapped(List<Value> values) {

        LinkedHashMap<String, Value> mapped = new LinkedHashMap<>();

        values.forEach(v -> mapped.put(v.getName(), v));

        return mapped;
    }
}
