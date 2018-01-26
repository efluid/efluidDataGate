package fr.uem.efluid.utils;

import java.util.Base64;
import java.util.Base64.Encoder;

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
	private final static char TYPE_OTHER = 'O';
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
}
