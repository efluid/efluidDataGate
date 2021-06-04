package fr.uem.efluid.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatUtilsTest {

    @Test
    public void testRegexpToTokenSearch() {

        assertEquals("test*", FormatUtils.regexpToTokenSearch("test.*"));
        assertEquals("te*st", FormatUtils.regexpToTokenSearch("te\\dst"));
        assertEquals("test*", FormatUtils.regexpToTokenSearch("test\\w*"));
        assertEquals("*test*", FormatUtils.regexpToTokenSearch(".+test\\d?"));
        assertEquals("*t*t*", FormatUtils.regexpToTokenSearch(".+t[es]{2,4}t\\d?"));
        assertEquals("test*__*val", FormatUtils.regexpToTokenSearch("test\\d*__[es]{2,4}\\dval"));

    }
}
