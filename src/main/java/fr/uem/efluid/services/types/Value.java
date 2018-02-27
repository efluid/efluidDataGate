package fr.uem.efluid.services.types;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

import fr.uem.efluid.model.metas.ColumnType;

/**
 * <p>
 * Generic holder for one parameter value, associated to its parameter name
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public interface Value {

	char TYPED_STRING_PROTECT = '\'';

	String INJECT_OF_LOB = "?";

	/** Fixed format */
	// TODO : can depends on database encoding ???
	Charset CONTENT_ENCODING = Charset.forName("utf-8");

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

	// TODO : need to decide if typed or not
	/**
	 * @return true if value is a natural string representation (depends on database
	 *         column type)
	 */
	ColumnType getType();

	/**
	 * The managed value as a String
	 * 
	 * @return
	 */
	default String getValueAsString() {
		return new String(getValue(), CONTENT_ENCODING);
	}

	/**
	 * @param val
	 * @return
	 */
	default String getTyped(List<String> lobKeys) {

		if (getType() == ColumnType.STRING) {
			return TYPED_STRING_PROTECT + getValueAsString() + TYPED_STRING_PROTECT;
		}

		if (getType() == ColumnType.BINARY) {
			lobKeys.add(getValueAsString());
			return INJECT_OF_LOB;
		}

		return getValueAsString();
	}

	/**
	 * @param val
	 * @return
	 */
	default String getTypedForDisplay() {

		if (getType() == ColumnType.STRING) {
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
