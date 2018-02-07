package fr.uem.efluid.tools;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import fr.uem.efluid.model.metas.ColumnType;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.utils.StringSplitter;

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

		builder.append(type.getRepresent()).append(TYPE_IDENT).append(B64_ENCODER.encodeToString(value.getBytes(Value.CONTENT_ENCODING)));

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
		return values.stream().map(v -> v.getName() + RENDERING_AFFECT + v.getTyped()).collect(Collectors.joining(RENDERING_SEPARATOR));
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
			if (oldOne == null || !oldOne.getValue().equals(newOne.getValue())) {
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
				.append(oldOne != null ? oldOne.getTyped() : MISSING_VALUE)
				.append(RENDERING_CHANGED)
				.append(newOne != null ? newOne.getTyped() : MISSING_VALUE)
				.toString();
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
			this.type = ColumnType.forRepresent(raw.charAt(pos + 1));
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
