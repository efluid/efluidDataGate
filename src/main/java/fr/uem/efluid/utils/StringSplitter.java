package fr.uem.efluid.utils;

import java.util.Iterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>
 * Because we need great performances on data processing, use a very basic splitter
 * compliant with Stream processing
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public final class StringSplitter implements Iterator<String> {

	private final String source;
	private final char splitChar;
	private int pos = 0;

	/**
	 * @param source
	 */
	private StringSplitter(String source, char splitChar) {
		super();
		this.source = source;
		this.splitChar = splitChar;
	}

	/**
	 * @return
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.pos > -1;
	}

	/**
	 * @return
	 * @see java.util.Iterator#next()
	 */
	@Override
	public String next() {
		int next = searchNext();
		String found = next == -1 ? this.source.substring(this.pos) : this.source.substring(this.pos, next);
		this.pos = next >= 0 ? next + 1 : next;
		return found;
	}

	/**
	 * @param source
	 * @param splitChar
	 * @return
	 */
	public static StringSplitter split(String source, char splitChar) {
		return new StringSplitter(source, splitChar);
	}

	/**
	 * @return
	 */
	public Stream<String> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(this, 0), false);
	}

	/**
	 * @return
	 */
	private int searchNext() {
		return this.source.indexOf(this.splitChar, this.pos);
	}
}
