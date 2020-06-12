package fr.uem.efluid.utils;

import org.junit.Assert;
import org.junit.Test;

public class FormatUtilsTest {

    @Test
    public void testRegexpToTokenSearch(){

        Assert.assertEquals("test*",FormatUtils.regexpToTokenSearch("test.*"));
        Assert.assertEquals("te*st",FormatUtils.regexpToTokenSearch("te\\dst"));
        Assert.assertEquals("test*", FormatUtils.regexpToTokenSearch("test\\w*"));
        Assert.assertEquals("*test*", FormatUtils.regexpToTokenSearch(".+test\\d?"));
        Assert.assertEquals("*t*t*", FormatUtils.regexpToTokenSearch(".+t[es]{2,4}t\\d?"));
        Assert.assertEquals("test*__*val", FormatUtils.regexpToTokenSearch("test\\d*__[es]{2,4}\\dval"));

    }
}
