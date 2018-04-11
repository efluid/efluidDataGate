package fr.uem.efluid.utils;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class RuntimeValuesUtils {

	/**
	 * @param o
	 * @return
	 */
	public static UUID dbRawToUuid(Object o) {

		if (o == null) {
			return null;
		}

		// Formal type depends on DB model

		if (o instanceof UUID) {
			return (UUID) o;
		}

		if (o instanceof byte[]) {
			ByteBuffer bb = ByteBuffer.wrap((byte[]) o);
			UUID uuid = new UUID(bb.getLong(), bb.getLong());
			return uuid;
		}

		if (o instanceof String) {
			return loadUUIDFromRaw((String) o);
		}

		throw new ApplicationException(ErrorType.UNSUPPORTED_UUID, "Unsuported UUID def type : " + o.getClass() + " with value " + o);
	}

	/**
	 * <p>
	 * Find a clean uuid from any managed raw UUID value
	 * </p>
	 * 
	 * @param raw
	 * @return
	 */
	public static UUID loadUUIDFromRaw(String raw) {

		if (raw.indexOf('-') == -1) {

			String uuid = new StringBuilder(36)
					.append(raw.substring(0, 7))
					.append('-')
					.append(raw.substring(8, 12))
					.append('-')
					.append(raw.substring(13, 17))
					.append('-')
					.append(raw.substring(18, 22))
					.append('-')
					.append(raw.substring(23))
					.toString();

			return UUID.fromString(uuid.toLowerCase());
		}

		return UUID.fromString(raw);
	}
}
