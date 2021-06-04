package fr.uem.efluid.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class StringSpliterTest {

    private static final String TO_SPLIT = "value;other;other;another;something;etc";

    @Test
    public void testSplit() {
        assertEquals(6, StringSplitter.split(TO_SPLIT, ';').stream().count());
    }
}
