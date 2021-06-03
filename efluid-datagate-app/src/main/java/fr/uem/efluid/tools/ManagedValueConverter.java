package fr.uem.efluid.tools;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.uem.efluid.ColumnType.NULL;
import static fr.uem.efluid.ColumnType.TEMPORAL;

/**
 * <p>
 * Tools for query build used in Managed database access. For backlog identification.
 * </p>
 * <p>
 * <b><font color="red">Warning : This version doesn't ignore column diff
 * differences</font></b>
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.1
 */
@Component
public class ManagedValueConverter {

    // For internal content
    private final static char AFFECT = '=';
    private final static char SEPARATOR = ',';
    private final static char TYPE_IDENT = '/';

    static final char NULL_KEY = ' '; // NBSP

    // For composite key support
    final static String KEY_JOIN = " / ";

    // For content rendering
    private final static String RENDERING_AFFECT = ":";
    private final static String RENDERING_SEPARATOR = ", ";
    private final static String RENDERING_CHANGED = "=>";
    private final static String MISSING_VALUE = "n/a";

    private final static String LOB_DIGEST = "SHA-256";

    private final static String LOB_URL_TEMPLATE = "<a href=\"/ui/lob/%s\" download=\"download\">" + ColumnType.BINARY.getDisplayName()
            + "</a>";

    private final static String TXT_URL_TEMPLATE = "<a href=\"/ui/lob/%s\" download=\"download\">" + ColumnType.TEXT.getDisplayName()
            + "</a>";

    private final static String BLOB_HASH_SEARCH = AFFECT + String.valueOf(ColumnType.BINARY.getRepresent()) + TYPE_IDENT;
    private final static String CLOB_HASH_SEARCH = AFFECT + String.valueOf(ColumnType.TEXT.getRepresent()) + TYPE_IDENT;

    private final static DateTimeFormatter LDT_FORMATTER = DateTimeFormatter.ofPattern(FormatUtils.DATE_TIME_FORMAT);

    @org.springframework.beans.factory.annotation.Value("${datagate-efluid.managed-datasource.value.keep-empty}")
    private boolean keepEmptyValues;

    /**
     * <p>
     * As the key value can reference various column (for composite key support) a
     * specific append process is required
     * </p>
     *
     * @param builder
     * @param keyValue
     * @param addition true if it's an addition on an already processed key (for composite keys)
     */
    public void appendExtractedKeyValue(
            final StringBuilder builder,
            final String keyValue,
            final boolean addition) {

        if (addition || builder.length() > 0) {
            builder.append(KEY_JOIN);
        }

        if (keyValue != null) {
            builder.append(keyValue);
        } else {
            builder.append(NULL_KEY);
        }
    }

    /**
     * <p>
     * Using internal templating desc, prepare one value for extraction when processing
     * content cell after cell, from a resultSet
     * </p>
     *
     * @param builder current line builder. One builder for each content line
     * @param colName name of the cell
     * @param value   raw content of the cell
     * @param type    type of supported value, as specified in <tt>ColumnType</tt>
     */
    public void appendExtractedValue(
            final StringBuilder builder,
            final String colName,
            final String value,
            final ColumnType type) {

        if (this.keepEmptyValues || value != null) {

            builder.append(colName).append(AFFECT);

            if (value == null) {
                builder.append(type.getRepresent()).append(TYPE_IDENT);
            } else {
                builder.append(type.getRepresent()).append(TYPE_IDENT)
                        .append(FormatUtils.encodeAsString(value));
            }

            builder.append(SEPARATOR);
        }

        // Nullable value
        else if (type == NULL) {
            builder.append(colName).append(AFFECT).append(type.getRepresent())
                    .append(TYPE_IDENT).append(SEPARATOR);
        }

        // Other are dropped (empty type for example)
    }

    /**
     * <p>
     * Using internal templating desc, prepare one value for extraction when processing
     * content cell after cell, from a resultSet
     * </p>
     *
     * @param builder current line builder. One builder for each content line
     * @param colName name of the cell
     * @param value   raw content of the cell
     * @param lobs    all identified lobs
     */
    public void appendTextValue(
            final StringBuilder builder,
            final String colName,
            final String value,
            final Map<String, byte[]> lobs) {

        if (this.keepEmptyValues || value != null) {
            builder.append(colName).append(AFFECT);

            byte[] bytes = FormatUtils.toBytes(value);
            String hash = hashBinary(bytes);
            lobs.put(hash, bytes);

            builder.append(ColumnType.TEXT.getRepresent()).append(TYPE_IDENT).append(hash).append(SEPARATOR);
        }
    }

    /**
     * <p>
     * Using internal templating desc, prepare one value for extraction when processing
     * content cell after cell, from a resultSet
     * </p>
     *
     * @param builder current line builder. One builder for each content line
     * @param colName name of the cell
     * @param value   raw content of the cell
     * @param lobs    all identified lobs
     */
    public void appendBinaryValue(
            final StringBuilder builder,
            final String colName,
            final byte[] value,
            final Map<String, byte[]> lobs) {

        if (this.keepEmptyValues || value != null) {
            builder.append(colName).append(AFFECT);

            String hash = hashBinary(value);
            lobs.put(hash, value);

            builder.append(ColumnType.BINARY.getRepresent()).append(TYPE_IDENT).append(hash).append(SEPARATOR);
        }
    }

    /**
     * <p>
     * Using internal templating desc and a fixed formating, prepare one TEMPORAL for
     * extraction when processing content cell after cell, from a resultSet
     * </p>
     *
     * @param builder current line builder. One builder for each content line
     * @param colName name of the cell
     * @param date    content of the cell as a <code>java.util.Date</code>
     */
    public void appendTemporalValue(
            final StringBuilder builder,
            final String colName,
            final Date date) {

        if (this.keepEmptyValues || date != null) {
            builder.append(colName).append(AFFECT);

            if (date == null) {
                builder.append(ColumnType.TEMPORAL.getRepresent()).append(TYPE_IDENT);
            } else {
                // Formated date
                builder.append(ColumnType.TEMPORAL.getRepresent()).append(TYPE_IDENT)
                        .append(FormatUtils.encodeAsString(LDT_FORMATTER.format(FormatUtils.toLdt(date))));
            }

            builder.append(SEPARATOR);
        }
    }

    /**
     * <p>
     * To produce the exact same extracted value format than with "normal RS-based
     * operation", but from a raw map of values (useful for testing). Provides directly
     * the string result.
     * </p>
     *
     * @param lineContent content in a List of Value
     * @return
     */
    public String convertToExtractedValue(final List<Value> lineContent) {

        StringBuilder oneLine = new StringBuilder();

        for (Value value : lineContent) {
            appendExtractedValue(
                    oneLine,
                    value.getName(),
                    value.getValueAsString(),
                    value.getType());
        }

        return finalizePayload(oneLine.toString());
    }

    /**
     * <p>
     * To produce the exact same extracted value format than with "normal RS-based
     * operation", but from a raw map of values (useful for testing). Provides directly
     * the string result.
     * </p>
     *
     * @param lineContent content in a LinkedHashMap (to keep insertion order)
     * @return extracted value from provided objects
     */
    public String convertToExtractedValue(final LinkedHashMap<String, Object> lineContent) {

        StringBuilder oneLine = new StringBuilder();

        for (Map.Entry<String, Object> value : lineContent.entrySet()) {
            ColumnType type = ColumnType.forObject(value.getValue());
            // Clean support for any temporal
            if (type == TEMPORAL) {
                appendTemporalValue(
                        oneLine,
                        value.getKey(),
                        // Manage any kind of temporal (date, datetime ...)
                        Date.from(LocalDateTime.parse(value.getValue().toString())
                                .atZone(ZoneId.systemDefault()).toInstant()));
            } else {
                appendExtractedValue(
                        oneLine,
                        value.getKey(),
                        value.getValue().toString(),
                        ColumnType.forObject(value.getValue()));
            }
        }

        return finalizePayload(oneLine.toString());
    }

    /**
     * <p>
     * Concatened values can have a last trailing "," joiner, to remove
     * </p>
     *
     * @param rawPayload
     * @return
     */
    public String finalizePayload(String rawPayload) {
        int last = rawPayload.length() - 1;
        if (last == -1) {
            return null;
        }
        if (rawPayload.charAt(last) == SEPARATOR) {
            return rawPayload.substring(0, last);
        }
        return rawPayload;
    }

    /**
     * <p>
     * Removes duplicate when links ref + col refs are specified
     * </p>
     *
     * @param values
     */
    public void filterInternalValueForLinks(List<Value> values) {

        Set<String> linkValueNames = values.stream()
                .filter(v -> v.getName().startsWith(SelectClauseGenerator.LINK_TAB_REFLAP))
                .map(v -> v.getName().substring(SelectClauseGenerator.LINK_TAB_REFLAP.length()))
                .collect(Collectors.toSet());

        for (Value value : values) {
            if (linkValueNames.contains(value.getName())) {
                values.remove(value);
            }
        }
    }

    /**
     * <p>
     * HR rendering of extracted values
     * </p>
     *
     * @param values
     * @return
     */
    public String displayInternalValue(List<Value> values) {
        return values.stream().map(v -> v.getName() + RENDERING_AFFECT + renderValue(v)).collect(Collectors.joining(RENDERING_SEPARATOR));
    }

    /**
     * <p>
     * For user display or for commit apply / rollback, we need to be able to revert the
     * internal model of value in a more convenient form
     * </p>
     *
     * @param internalExtracted
     * @return
     */
    public List<Value> expandInternalValue(String internalExtracted) {

        if (internalExtracted != null && !"".equals(internalExtracted)) {
            return StringSplitter.split(internalExtracted, SEPARATOR).stream()
                    .map(ExpandedValue::new)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * Process only value names, without processing value content, for faster analysis
     *
     * @param internalExtracted a payload from an index entry or a diff preparation
     * @return all column names referenced in index entry payload, as a stream for inlined processing
     */
    public Stream<String> expandInternalValueNames(String internalExtracted) {

        if (internalExtracted != null && !"".equals(internalExtracted)) {
            return StringSplitter.split(internalExtracted, SEPARATOR).stream()
                    .map(v -> v.substring(0, v.indexOf(AFFECT)).intern());
        }

        return Stream.empty();
    }

    /**
     * <p>
     * From the raw value from a Diff line, get only the hash codes for the used lobs (for
     * values of type "Binary"). Needs to expand the raw value to check each Values
     * </p>
     *
     * @param internalExtracted
     * @return the used hashes
     */
    public List<String> extractUsedBinaryHashs(String internalExtracted) {

        // To avoid useless expand, check if a hash is used in internal
        // (double index search is still faster than regexp process in this case)
        if (internalExtracted != null
                && (internalExtracted.indexOf(BLOB_HASH_SEARCH) > 0 || internalExtracted.indexOf(CLOB_HASH_SEARCH) > 0)) {
            return expandInternalValue(internalExtracted).stream()
                    .filter(v -> v.getType() == ColumnType.BINARY || v.getType() == ColumnType.TEXT)
                    .map(Value::getValueAsString).collect(Collectors.toList());
        }

        return null;
    }

    /**
     * <p>
     * Produces a "human readable" payload, combining extracted data from current index
     * entry payload and previous payload. The details of value is parsed and converted to
     * an easy to read rendering, using the format
     * <code>column_name:"value",column2:1234...</code> (similar to extracted value). It
     * combines also a diff rendering, where one of the values provided can be empty : it
     * is processed as a "diff result", like this :
     * <ul>
     * If activePayload is empty and existing is not, it is seen as a deletion. The
     * rendering will be just the existing payload content.</li>
     * <li>If activePayload is not empty and existing is, it is seen as an addition : the
     * active payload is displayed.</li>
     * <li>If both are present, it is seen as a modification. It will detect then the
     * modified values and display them this way <code>column:"old"=&gt;"new"</code>, only on
     * modified columns.</li>
     * </ul>
     * </p>
     * <p>
     * It's an expensive method, so use it only on limited rendering (summary of index
     * content, or one a limited set of existing index)
     * </p>
     *
     * @param activePayload
     * @param existingPayload
     * @return
     */
    public String convertToHrPayload(String activePayload, String existingPayload) {

        if (!StringUtils.hasText(activePayload)) {
            if (!StringUtils.hasText(existingPayload)) {
                return null;
            }
            return displayInternalValue(expandInternalValue(existingPayload));
        }

        if (!StringUtils.hasText(existingPayload)) {
            return displayInternalValue(expandInternalValue(activePayload));
        }

        return displayModificationRendering(expandInternalValue(activePayload), expandInternalValue(existingPayload));
    }

    /**
     * <p>
     * For 2 provided payloads, build a human readable change set. Some values can be
     * missing, manage it as a "precise diff".
     * </p>
     *
     * @param newOnes
     * @param existings
     * @return
     */
    private static String displayModificationRendering(List<Value> newOnes, List<Value> existings) {

        final Map<String, Value> existingsMapped = existings.stream().collect(Collectors.toMap(Value::getName, v -> v));
        final List<String> result = new ArrayList<>();

        // Some can be missing
        for (Value newOne : newOnes) {
            Value oldOne = existingsMapped.remove(newOne.getName());
            if (oldOne == null || !Arrays.equals(oldOne.getValue(), newOne.getValue())) {
                result.add(changedValue(oldOne, newOne));
            }
        }

        // Process also remaining old ones, which are removed columns
        for (Value oldOne : existingsMapped.values()) {
            result.add(changedValue(oldOne, null));
        }

        // Nullify empty HR
        if (result.isEmpty()) {
            return null;
        }

        return String.join(RENDERING_SEPARATOR, result);
    }

    /**
     * <p>
     * Build rendering <code>col:"old"=>"new"</code> for modification rendering
     * </p>
     *
     * @param oldOne
     * @param newOne
     * @return
     */
    private static String changedValue(Value oldOne, Value newOne) {
        String name = oldOne != null ? oldOne.getName() : newOne.getName();
        return new StringBuilder(name)
                .append(RENDERING_AFFECT)
                .append(oldOne != null ? renderValue(oldOne) : MISSING_VALUE)
                .append(RENDERING_CHANGED)
                .append(newOne != null ? renderValue(newOne) : MISSING_VALUE)
                .toString();
    }

    /**
     * Rendering is not a "natural" display on binary types
     *
     * @param value
     * @return
     */
    private static String renderValue(Value value) {

        if (value.getType() == ColumnType.BINARY || value.getType() == ColumnType.TEXT) {
            return String.format(value.getType() == ColumnType.BINARY ? LOB_URL_TEMPLATE : TXT_URL_TEMPLATE, FormatUtils.encode(value.getValue()));
        }

        return value.getTypedForDisplay();
    }

    /**
     * <p>
     * For binaries, use a hash + B64 (not intended to be reversible) reprensentation for
     * diff analysis
     * </p>
     *
     * @param data
     * @return
     */
    private static String hashBinary(byte[] data) {

        if (data == null) {
            return "";
        }

        try {
            // Digest is not TS
            MessageDigest digest = MessageDigest.getInstance(LOB_DIGEST);

            // Hash + B64 (UTF8 encoded)
            return FormatUtils.encode(digest.digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new ApplicationException(ErrorType.VALUE_SHA_UNSUP, "unsupported digest type " + LOB_DIGEST, e);
        }

    }

    /*
     * ###################################################################################
     * ########## VALUE PROTECTOR APPENDERS. ONE METHOD FOR EACH SUPPORTED TYPE ##########
     * ###################################################################################
     */

    /**
     * <p>
     * For decoding of an internaly indexed value, with access to a convening
     * <tt>Value</tt>.
     * </p>
     *
     * @author elecomte
     * @version 1
     * @since v0.0.1
     */
    private static final class ExpandedValue implements Value {

        private final String name;
        private final byte[] value;
        private final ColumnType type;

        /**
         * <p>
         * Do the real decoding process
         * </p>
         *
         * @param raw
         */
        ExpandedValue(String raw) {
            // Basic revert of what's done in appendExtractedValue
            int pos = raw.indexOf(AFFECT);
            this.name = raw.substring(0, pos).intern();
            this.type = ColumnType.forRepresent(raw.charAt(pos + 1));
            // Binary stay always rendered as B64 hash
            switch (this.type) {
                case NULL:
                    this.value = null;
                    break;
                case BINARY:
                case TEXT:
                    this.value = raw.substring(pos + 3).getBytes(FormatUtils.CONTENT_ENCODING);
                    break;
                default:
                    this.value = FormatUtils.decode(raw.substring(pos + 3));
            }
        }

        /**
         * @return
         */
        @Override
        public byte[] getValue() {
            return this.value;
        }

        /**
         * @return
         */
        @Override
        public String getName() {
            return this.name;
        }

        /**
         * @return the type
         */
        @Override
        public ColumnType getType() {
            return this.type;
        }

        /**
         * @return
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.name + "=" + (this.type == ColumnType.STRING ? "\"" : "") + getValueAsString()
                    + (this.type == ColumnType.STRING ? "\"" : "");
        }
    }
}
