package fr.uem.efluid;

import org.junit.Test;

import static fr.uem.efluid.GeneratorTester.onClasses;

/**
 * Test on ParameterKey search (by annotation or with other rules)
 */
public class ParameterLinkTest {

    @Test
    public void testLinkAutoOnAnnotatedField() {

        assertSameSimpleLinkResult(fr.uem.efluid.tests.findLinks.LinkAutoOnAnnotatedField.class);
    }

    @Test
    public void testLinkAutoOnNotAnnotatedField() {

        assertSameSimpleLinkResult(fr.uem.efluid.tests.findLinks.LinkAutoOnNotAnnotatedField.class);
    }

    @Test
    public void testLinkInTableSet() {

        var tester = onClasses(
                fr.uem.efluid.tests.findLinks. LinkInTableSet.class,
                fr.uem.efluid.tests.findLinks. LinkedType.class
        ).generate();

        tester.assertThatTable("LINKED")
                .wasFoundOn(fr.uem.efluid.tests.findLinks.LinkedType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("WHEN", "DATA");

        tester.assertThatTable("SOURCE_ONE")
                .wasFoundOn(fr.uem.efluid.tests.findLinks. LinkInTableSet.class)
                .hasKey("ID", ColumnType.ATOMIC)
                .hasColumns("ASSOCIATED_ID", "OTHER")
                .doesntHaveColumns("ASSOCIATED")
                .hasLinkForColumn("ASSOCIATED_ID").with("LINKED", "KEY");

        tester.assertThatTable("SOURCE_TWO")
                .wasFoundOn(fr.uem.efluid.tests.findLinks. LinkInTableSet.class)
                .hasKey("ID", ColumnType.ATOMIC)
                .hasColumns("OTHER")
                .doesntHaveColumns("ASSOCIATED", "ASSOCIATED_ID")
                .doesntHaveLinkForColumn("ASSOCIATED_ID");
    }

    @Test
    public void testLinkSpecifiedOnAnnotatedField() {

        assertSameSimpleLinkResult(fr.uem.efluid.tests.findLinks.LinkSpecifiedOnAnnotatedField.class);
    }

    @Test
    public void testLinkSpecifiedOnSpecifiedValue() {

        assertSameSimpleLinkResult(fr.uem.efluid.tests.findLinks.LinkSpecifiedOnSpecifiedValue.class);
    }

    private void assertSameSimpleLinkResult(Class<?> linkSource){

        var tester = onClasses(
                linkSource,
                fr.uem.efluid.tests.findLinks. LinkedType.class
        ).generate();

        tester.assertThatTable("LINKED")
                .wasFoundOn(fr.uem.efluid.tests.findLinks.LinkedType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("WHEN", "DATA");

        tester.assertThatTable("SOURCE")
                .wasFoundOn(linkSource)
                .hasKey("ID", ColumnType.ATOMIC)
                .hasColumns("ASSOCIATED_ID", "OTHER")
                .doesntHaveColumns("ASSOCIATED")
                .hasLinkForColumn("ASSOCIATED_ID").with("LINKED", "KEY");
    }


}
