package fr.uem.efluid.utils;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

/**
 * <p>
 * Util bean for item association during some complexe stream grouping, when we need to
 * process something similar to a Map.Entry without having a Map as source
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class Associate<K, V> {

	private final K one;
	private final V two;

	private Associate(K one, V two) {
		this.one = one;
		this.two = two;
	}

	/**
	 * @return the one
	 */
	public K getOne() {
		return this.one;
	}

	/**
	 * @return the two
	 */
	public V getTwo() {
		return this.two;
	}

	/**
	 * @param one
	 * @param two
	 * @return
	 */
	public static <K, V> Associate<K, V> of(K one, V two) {
		return new Associate<>(one, two);
	}

	/**
	 * @param entry
	 * @return
	 */
	public static <K, V> Associate<K, V> of(Map.Entry<K, V> entry) {
		return new Associate<>(entry.getKey(), entry.getValue());
	}

	/**
	 * @param ones
	 * @param two
	 * @return
	 */
	public static <K, V> Stream<Associate<K, V>> onFlatmapOf(Collection<K> ones, V two) {
		return ones.stream().map(k -> new Associate<>(k, two));
	}

}
