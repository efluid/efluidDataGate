package fr.uem.efluid.utils;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.uem.efluid.model.Value;

/**
 * Tools for query build used in Managed database access. For backlog identification
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
	 * Using internal templating desc, prepare one value for extraction
	 * 
	 * @param builder
	 * @param colName
	 * @param value
	 * @param stringCol
	 * @param last
	 */
	public static void appendExtractedValue(
			final StringBuilder builder,
			final String colName,
			final byte[] value,
			final boolean stringCol,
			final boolean last) {

		builder.append(colName).append(AFFECT);

		if (stringCol) {
			builder.append(TYPE_STRING).append(TYPE_IDENT).append(B64_ENCODER.encode(value));
		} else {
			builder.append(TYPE_OTHER).append(TYPE_IDENT).append(B64_ENCODER.encode(value));
		}

		if (!last) {
			builder.append(SEPARATOR);
		}
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
	public static Map<String, Object> explodeInternalValue(String internalExtracted) {

		// TODO : Need to define if type need to be kept or not
		return Stream.iterate(internalExtracted, str -> {
			int pos = str.lastIndexOf(SEPARATOR);
			return pos == -1 ? "" : str.substring(0, pos);
		}).map(DecodedValueAffect::new).collect(Collectors.toMap(DecodedValueAffect::getName, DecodedValueAffect::asValue));
	}

	/**
	 * For decoding of an internaly indexed value
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	private static final class DecodedValueAffect {

		private final String name;
		final byte[] value;
		final boolean string;

		private final static Decoder B64_DECODER = Base64.getDecoder();

		/**
		 * @param raw
		 */
		DecodedValueAffect(String raw) {
			// Basic revert of what's done in appendExtractedValue
			int pos = raw.lastIndexOf(AFFECT);
			this.name = raw.substring(0, pos);
			this.value = B64_DECODER.decode(raw.substring(pos + 2));
			this.string = (raw.charAt(pos + 1) == TYPE_STRING);
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return this.name;
		}

		/**
		 * @return the value
		 */
		public Value asValue() {
			return new Value() {

				@Override
				public byte[] getValue() {
					return DecodedValueAffect.this.value;
				}

				@Override
				public boolean isString() {
					return DecodedValueAffect.this.string;
				}

			};
		}
	}
}
