package fr.uem.efluid.model;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

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
	boolean isString();

	/**
	 * The managed value as a String
	 * 
	 * @return
	 */
	default String getValueAsString() {
		return new String(getValue(), CONTENT_ENCODING);
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
