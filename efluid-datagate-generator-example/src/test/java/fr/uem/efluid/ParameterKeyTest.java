package fr.uem.efluid;

import org.junit.jupiter.api.Test;

import static fr.uem.efluid.GeneratorTester.onClasses;

/**
 * Test on ParameterKey search (by annotation or with other rules)
 */
public class ParameterKeyTest {

    @Test
    public void testKeyAreIgnoredWhenSpecifiedAsNotInheritedOnSimpleChild() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onKey.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onKey.InheritedTypeSimple.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onKey.RootType.class)
                .hasKey("KEYONE", ColumnType.STRING)
                .hasKey("KEYTWO", ColumnType.ATOMIC)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onKey.InheritedTypeSimple.class)
                .hasKey("KEYONE", ColumnType.STRING)
                .doesntHaveKey("KEYTWO")
                .hasColumns("VALUE", "OTHER", "PROPERTY");
    }

    @Test
    public void testKeyAreIgnoredWhenSpecifiedAsNotInheritedOnChildWithSet() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onKey.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onKey.InheritedTypeWithSet.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onKey.RootType.class)
                .hasKey("KEYONE", ColumnType.STRING)
                .hasKey("KEYTWO", ColumnType.ATOMIC)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("MY_TYPE")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onKey.InheritedTypeWithSet.class)
                .hasKey("KEYONE", ColumnType.STRING)
                .doesntHaveKey("KEYTWO")
                .hasColumns("VALUE", "OTHER", "PROPERTY");

        tester.assertThatTable("MY_TYPE_CHILD")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onKey.InheritedTypeWithSet.class)
                .hasKey("KEYONE", ColumnType.STRING)
                .doesntHaveKey("KEYTWO")
                .hasColumns("VALUE", "OTHER", "PROPERTY");
    }

}
