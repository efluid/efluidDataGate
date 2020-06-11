package fr.uem.efluid.utils;

import org.junit.Assert;
import org.junit.Test;

public class FormatUtilsTest {

    @Test
    public void testRegexpToTokenSearch(){

        Assert.assertEquals("test*",FormatUtils.regexpToTokenSearch("test.*"));
        Assert.assertEquals("test*", FormatUtils.regexpToTokenSearch("test\\w*"));
        Assert.assertEquals("*test*", FormatUtils.regexpToTokenSearch(".+test\\d?"));
        Assert.assertEquals("*t*t*", FormatUtils.regexpToTokenSearch(".+t[es]{2,4}t\\d?"));

    }
}
