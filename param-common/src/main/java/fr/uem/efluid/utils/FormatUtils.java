package fr.uem.efluid.utils;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class FormatUtils {

	/** Fixed format */
	// TODO : can depends on database encoding ???
	public final static Charset CONTENT_ENCODING = Charset.forName("utf-8");

	public static final String DATE_FORMAT = "yyyy-MM-dd";

	public static final String TIME_FORMAT = "HH:mm:ss";

	public static final String DATE_TIME_FORMAT = DATE_FORMAT + " " + TIME_FORMAT;

	private static final DateTimeFormatter LDT_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

	private final static Decoder B64_DECODER = Base64.getDecoder();

	private final static Encoder B64_ENCODER = Base64.getEncoder();

	/**
	 * 
	 */
	private FormatUtils() {
		// TODO Auto-generated constructor stub
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
		return new String(decode(rawB64), CONTENT_ENCODING);
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String encode(byte[] raw) {
		return new String(B64_ENCODER.encode(raw), CONTENT_ENCODING);
	}

	/**
	 * @param raw
	 * @return
	 */
	public static String encodeAsString(String raw) {
		return new String(B64_ENCODER.encode(toBytes(raw)), CONTENT_ENCODING);
	}

	/**
	 * <p>
	 * Value with clean encoding
	 * </p>
	 * 
	 * @param value
	 * @return
	 */
	public static byte[] toBytes(String value) {
		return value.getBytes(CONTENT_ENCODING);
	}

	/**
	 * With socle format
	 * 
	 * @param date
	 * @return
	 */
	public static String format(LocalDateTime date) {
		return LDT_FORMATTER.format(date);
	}

}
