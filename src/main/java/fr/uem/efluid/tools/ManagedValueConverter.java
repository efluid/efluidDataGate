package fr.uem.efluid.tools;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.ErrorType;
import fr.uem.efluid.utils.StringSplitter;
import fr.uem.efluid.utils.WebUtils;

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
 * @since v0.0.1
 * @version 1
 */
@Component
public class ManagedValueConverter {

	// For internal content
	private final static char AFFECT = '=';
	private final static char SEPARATOR = ',';
	private final static char TYPE_IDENT = '/';

	// For content rendering
	private final static String RENDERING_AFFECT = ":";
	private final static String RENDERING_SEPARATOR = ", ";
	private final static String RENDERING_CHANGED = "=>";
	private final static String MISSING_VALUE = "n/a";

	private final static Encoder B64_ENCODER = Base64.getEncoder();

	private final static Decoder B64_DECODER = Base64.getDecoder();

	private final static String LOB_DIGEST = "SHA-256";

	private final static String LOB_URL_TEMPLATE = "<a href=\"/lob/%s\" download=\"download\">" + ColumnType.BINARY.getDisplayName()
			+ "</a>";

	private final static String LOB_HASH_SEARCH = new StringBuilder(AFFECT).append(ColumnType.BINARY.getRepresent()).append(TYPE_IDENT)
			.toString();

	private final static DateTimeFormatter LDT_FORMATTER = DateTimeFormatter.ofPattern(WebUtils.DATE_TIME_FORMAT);

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
	 * @param type
	 *            type of supported value, as specified in <tt>ColumnType</tt>
	 * @param last
	 *            true if it's the last cell
	 */
	public void appendExtractedValue(
			final StringBuilder builder,
			final String colName,
			final String value,
			final ColumnType type,
			final boolean last) {

		builder.append(colName).append(AFFECT);

		if (value == null) {
			builder.append(type.getRepresent()).append(TYPE_IDENT);
		} else {
			builder.append(type.getRepresent()).append(TYPE_IDENT)
					.append(B64_ENCODER.encodeToString(value.getBytes(Value.CONTENT_ENCODING)));
		}

		if (!last) {
			builder.append(SEPARATOR);
		}
	}

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
	 * @param type
	 *            type of supported value, as specified in <tt>ColumnType</tt>
	 * @param last
	 *            true if it's the last cell
	 */
	public void appendBinaryValue(
			final StringBuilder builder,
			final String colName,
			final byte[] value,
			final boolean last,
			final Map<String, byte[]> lobs) {

		builder.append(colName).append(AFFECT);

		String hash = hashBinary(value);
		lobs.put(hash, value);

		builder.append(ColumnType.BINARY.getRepresent()).append(TYPE_IDENT).append(hash);

		if (!last) {
			builder.append(SEPARATOR);
		}
	}

	/**
	 * <p>
	 * Using internal templating desc and a fixed formating, prepare one TEMPORAL for
	 * extraction when processing content cell after cell, from a resultSet
	 * </p>
	 * 
	 * @param builder
	 *            current line builder. One builder for each content line
	 * @param colName
	 *            name of the cell
	 * @param date
	 *            content of the cell as a <code>java.util.Date</code>
	 * @param last
	 *            true if it's the last cell
	 */
	public void appendTemporalValue(
			final StringBuilder builder,
			final String colName,
			final Date date,
			final boolean last) {

		builder.append(colName).append(AFFECT);

		if (date == null) {
			builder.append(ColumnType.TEMPORAL.getRepresent()).append(TYPE_IDENT);
		} else {
			// Formated date
			builder.append(ColumnType.TEMPORAL.getRepresent()).append(TYPE_IDENT).append(B64_ENCODER.encodeToString(LDT_FORMATTER
					.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())).getBytes(Value.CONTENT_ENCODING)));
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
	public String convertToExtractedValue(final LinkedHashMap<String, Object> lineContent) {

		StringBuilder oneLine = new StringBuilder();

		int remaining = lineContent.size() - 1;

		for (Map.Entry<String, Object> value : lineContent.entrySet()) {
			appendExtractedValue(
					oneLine,
					value.getKey(),
					value.getValue().toString(),
					ColumnType.forObject(value.getValue()),
					remaining == 0);
			remaining--;
		}

		return oneLine.toString();
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

		// TODO : Need to define if we have to keep type or not
		return StringSplitter.split(internalExtracted, SEPARATOR).stream()
				.map(ExpandedValue::new)
				.collect(Collectors.toList());
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
		if (internalExtracted != null && internalExtracted.indexOf(LOB_HASH_SEARCH) > 0) {
			return expandInternalValue(internalExtracted).stream().filter(v -> v.getType() == ColumnType.BINARY)
					.map(v -> v.getValueAsString()).collect(Collectors.toList());
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
	 * modified values and display them this way <code>column:"old"=>"new"</code>, only on
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

		if (activePayload == null) {
			if (existingPayload == null) {
				return null;
			}
			return displayInternalValue(expandInternalValue(existingPayload));
		}

		if (existingPayload == null) {
			return displayInternalValue(expandInternalValue(activePayload));
		}

		return displayModificationRendering(expandInternalValue(activePayload), expandInternalValue(existingPayload));
	}

	/**
	 * @param rawB64
	 * @return
	 */
	public static byte[] decode(String rawB64) {
		return B64_DECODER.decode(rawB64);
	}

	/**
	 * @param rawB64
	 * @return
	 */
	public static String decodeAsString(String rawB64) {
		return new String(decode(rawB64), Value.CONTENT_ENCODING);
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String encodeAsString(String raw) {
		return new String(B64_ENCODER.encode(raw.getBytes(Value.CONTENT_ENCODING)), Value.CONTENT_ENCODING);
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

		return result.stream().collect(Collectors.joining(RENDERING_SEPARATOR));
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

		if (value.getType() == ColumnType.BINARY) {
			try {
				return String.format(LOB_URL_TEMPLATE,
						URLEncoder.encode(new String(value.getValue(), Value.CONTENT_ENCODING), Value.CONTENT_ENCODING.name()));
			} catch (UnsupportedEncodingException e) {
				throw new ApplicationException(ErrorType.VALUE_SHA_UNSUP, "unsupported u encoding type " + LOB_DIGEST, e);
			}
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
			return new String(B64_ENCODER.encode(digest.digest(data)), Value.CONTENT_ENCODING);
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
	 * @since v0.0.1
	 * @version 1
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
			this.value = this.type == ColumnType.BINARY ? raw.substring(pos + 3).getBytes(CONTENT_ENCODING)
					: decode(raw.substring(pos + 3));
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
