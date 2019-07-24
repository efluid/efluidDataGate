package fr.uem.efluid.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
public class StringSpliterTest {

	private static final String TO_SPLIT = "value;other;other;another;something;etc";

	@Test
	public void testSplit() {
		Assert.assertEquals(6, StringSplitter.split(TO_SPLIT, ';').stream().count());
	}
}
