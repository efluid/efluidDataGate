package fr.uem.efluid.utils;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.uem.efluid.services.types.Value;

/**
 * <p>
 * Tools for query build used in Managed database access. For backlog identification.
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ManagedDiffUtils {

	private final static char AFFECT = '=';
	private final static char SEPARATOR = ',';
	private final static char TYPE_STRING = 'S';
	private final static char TYPE_OTHER = 'O'; // TODO : other types ?
	private final static char TYPE_IDENT = '/';

	private final static Encoder B64_ENCODER = Base64.getEncoder();

	/**
	 * <p>
	 * Using internal templating desc, prepare one value for extraction when processing
	 * content cell after cell, from a resultSet
	 * </p>
	 * 
	 * @param builder
	 *            current line builder. One builder for each content line
	 * @param colName
	 *            name of the cell
	 * @param value
	 *            raw content of the cell
	 * @param stringCol
	 *            true if value is a string
	 * @param last
	 *            true if it's the last cell
	 */
	public static void appendExtractedValue(
			final StringBuilder builder,
			final String colName,
			final String value,
			final boolean stringCol,
			final boolean last) {

		builder.append(colName).append(AFFECT);

		if (stringCol) {
			appendStringValue(builder, value);
		} else {
			appendOtherValue(builder, value);
		}

		if (!last) {
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
	 * @param lineContent
	 *            content in a LinkedHashMap (to keep insertion order)
	 * @return
	 */
	public static String convertToExtractedValue(final LinkedHashMap<String, Object> lineContent) {

		StringBuilder oneLine = new StringBuilder();

		int remaining = lineContent.size() - 1;

		for (Map.Entry<String, Object> value : lineContent.entrySet()) {
			appendExtractedValue(
					oneLine,
					value.getKey(),
					value.getValue().toString(),
					value.getValue() instanceof String,
					remaining == 0);
			remaining--;
		}

		return oneLine.toString();
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
	public static List<Value> expandInternalValue(String internalExtracted) {

		// TODO : Need to define if we have to keep type or not
		return StringSplitter.split(internalExtracted, SEPARATOR).stream()
				.map(ExpandedValue::new)
				.collect(Collectors.toList());
	}

	/*
	 * ###################################################################################
	 * ########## VALUE PROTECTOR APPENDERS. ONE METHOD FOR EACH SUPPORTED TYPE ##########
	 * ###################################################################################
	 */

	/**
	 * @param builder
	 * @param value
	 */
	private static void appendStringValue(final StringBuilder builder, final String value) {
		builder.append(TYPE_STRING).append(TYPE_IDENT).append(B64_ENCODER.encodeToString(value.getBytes(Value.CONTENT_ENCODING)));
	}

	/**
	 * @param builder
	 * @param value
	 */
	private static void appendOtherValue(final StringBuilder builder, final String value) {
		builder.append(TYPE_OTHER).append(TYPE_IDENT).append(B64_ENCODER.encodeToString(value.getBytes(Value.CONTENT_ENCODING)));
	}

	/**
	 * <p>
	 * For decoding of an internaly indexed value, with access to a convening
	 * <tt>Value</tt>.
	 * </p>
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	private static final class ExpandedValue implements Value {

		private final String name;
		private final byte[] value;
		private final boolean string;

		private final static Decoder B64_DECODER = Base64.getDecoder();

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
			this.value = B64_DECODER.decode(raw.substring(pos + 3));
			this.string = (raw.charAt(pos + 1) == TYPE_STRING);
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
		public boolean isString() {
			return this.string;
		}

		/**
		 * @return
		 */
		@Override
		public String getName() {
			return this.name;
		}

		/**
		 * @return
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.name + "=" + (this.isString() ? "\"" : "") + getValueAsString() + (this.isString() ? "\"" : "");
		}
	}
}
