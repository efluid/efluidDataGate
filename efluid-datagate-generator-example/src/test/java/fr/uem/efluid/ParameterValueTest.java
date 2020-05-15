package fr.uem.efluid;

import org.junit.Test;

import static fr.uem.efluid.GeneratorTester.onClasses;

/**
 * Test on ParameterValue search (by annotation or with other rules)
 */
public class ParameterValueTest {

    // TODO : add here all managed

    @Test
    public void testConstantsAreIgnored() {

        var tester = onClasses(
                fr.uem.efluid.tests.ignoreConstants.RootType.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.ignoreConstants.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER")
                .doesntHaveColumns("CONSTANT_PROPERTY");
    }

    @Test
    public void testInheritedConstantsAreIgnored() {

        var tester = onClasses(
                fr.uem.efluid.tests.ignoreConstants.RootType.class,
                fr.uem.efluid.tests.ignoreConstants.InheritedType.class
        ).generate();

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.ignoreConstants.InheritedType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "PROPERTY")
                .doesntHaveColumns("CONSTANT_PROPERTY", "OTHER_CONSTANT");
    }
}
