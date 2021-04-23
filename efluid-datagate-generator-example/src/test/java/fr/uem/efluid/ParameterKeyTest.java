package fr.uem.efluid;

import fr.uem.efluid.tests.inheritance.compositeKeyInTable.TestCompositeInTable;
import org.junit.Test;

import static fr.uem.efluid.GeneratorTester.onClasses;

/**
 * Test on ParameterKey search (by annotation or with other rules)
 */
public class ParameterKeyTest {

    /*
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


     */


    @Test
    public void testKeyAreIgnoredWhenSpecifiedAsNotInheritedOnSimpleChild() {

        var tester = onClasses(
                TestCompositeInTable.class
        ).generate();

        tester.assertThatTable("MY_TABLE_1")
                .wasFoundOn(TestCompositeInTable.class)
                .hasKey("ID", ColumnType.STRING)
                .hasColumns("VALUE");

        tester.assertThatTable("MY_JOIN_TABLE_WITH_OTHER_PROPS")
                .wasFoundOn(TestCompositeInTable.class)
                .hasKey("SOURCE", ColumnType.STRING)
                .hasKey("DEST", ColumnType.ATOMIC)
                .doesntHaveKey("ID")
                .doesntHaveColumns("VALUE")
                .hasColumns("EXT_ONE", "EXT_TWO");
    }

}
