package fr.uem.efluid;

import fr.uem.efluid.tests.inheritance.exclude.all.SomeLinkedType;
import org.junit.Test;

import static fr.uem.efluid.GeneratorTester.onClasses;
import static fr.uem.efluid.GeneratorTester.onPackage;

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

    @Test
    public void testValueAreIgnoredWhenSpecifiedAsNotInheritedOnSimpleChild() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onValue.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeSimple.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "TO_EXCLUDE");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeSimple.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "PROPERTY")
                .doesntHaveColumns("TO_EXCLUDE");
    }

    @Test
    public void testValueAreIgnoredWhenSpecifiedAsNotInheritedOnSubChild() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onValue.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeSimple.class,
                fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeSubChild.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "TO_EXCLUDE");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeSimple.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "PROPERTY");

        tester.assertThatTable("T_INHERITED_SUB")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeSubChild.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "SUB_PROPERTY")
                .doesntHaveColumns("TO_EXCLUDE", "PROPERTY");
    }

    @Test
    public void testValueAreIgnoredWhenSpecifiedAsNotInheritedOnSet() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onValue.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeWithSet.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "TO_EXCLUDE");

        tester.assertThatTable("MY_TYPE")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeWithSet.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "PROPERTY")
                .doesntHaveColumns("TO_EXCLUDE");

        tester.assertThatTable("MY_TYPE_CHILD")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onValue.InheritedTypeWithSet.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "PROPERTY")
                .doesntHaveColumns("TO_EXCLUDE");
    }

    @Test
    public void testValueAreIgnoredWhenSpecifiedForTypeInheritanceAllFields() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildAllFields.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "PROPERTY", "SECOND");

        tester.assertThatTable("T_INHERITED_SUB_ALL")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildAllFields.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "SUB_PROPERTY")
                .doesntHaveColumns("PROPERTY", "SECOND");
    }

    @Test
    public void testValueAreIgnoredWhenSpecifiedForTypeInheritanceSpecifiedFields() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildSpecifiedFields.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "PROPERTY", "SECOND");

        tester.assertThatTable("T_INHERITED_SUB_SPEC")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildSpecifiedFields.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("PROPERTY", "SECOND", "SUB_PROPERTY")
                .doesntHaveColumns("VALUE", "OTHER");
    }

    @Test
    public void testValueAreIgnoredWhenSpecifiedForTypeInheritanceMixed() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildMixed.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "PROPERTY", "SECOND");

        tester.assertThatTable("T_INHERITED_SUB_MIXED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildMixed.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "PROPERTY", "SUB_PROPERTY")
                .doesntHaveColumns("SECOND", "OTHER");
    }

    @Test
    public void testValueAreIgnoredWhenSpecifiedForTypeInheritanceOnSet() {

        var tester = onClasses(
                fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class,
                fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildOnSet.class
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("T_INHERITED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSimple.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER", "PROPERTY", "SECOND");

        tester.assertThatTable("T_INHERITED_SUB_1")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildOnSet.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("OTHER", "PROPERTY", "SECOND", "SUB_PROPERTY")
                .doesntHaveColumns("VALUE");

        tester.assertThatTable("T_INHERITED_SUB_2")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.onChildType.InheritedTypeSubChildOnSet.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("OTHER", "PROPERTY", "SECOND", "SUB_PROPERTY")
                .doesntHaveColumns("VALUE");
    }


    @Test
    public void testValueAreIgnoredWhenSpecifiedForTypeInheritanceAll() {

        var tester = onPackage(
                fr.uem.efluid.tests.inheritance.exclude.all.RootType.class.getPackageName()
        ).generate();

        tester.assertThatTable("T_ROOT")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.all.RootType.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasColumns("VALUE", "OTHER");

        tester.assertThatTable("LINKED")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.all.SomeLinkedType.class);

        tester.assertThatTable("T_SUB_A")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.all.SubTypeA.class)
                .hasKey("KEY", ColumnType.STRING)
                .hasLinkForColumn("LINKEDTYPE_KEY_A").with("LINKED", "KEY")
                .hasColumns("VALUE", "OTHER", "ALLOWED", "CARD", "LINKEDTYPE_KEY_A");

        tester.assertThatTable("T_SUB_SUB_A")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.all.SubSubTypeA.class)
                .hasKey("INNERKEY", ColumnType.ATOMIC)
                .hasColumns("SUBALLOWED", "SUBCARD")
                .doesntHaveColumns("VALUE", "OTHER", "ALLOWED", "CARD", "LINKEDTYPE_KEY_A")
                .doesntHaveLinkForColumn("LINKEDTYPE_KEY_A");

        tester.assertThatTable("T_SUB_B")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.all.SubTypeB.class)
                .hasKey("KEYB", ColumnType.ATOMIC)
                .hasColumns("PROPERTY", "SECOND", "LINKEDTYPE_KEY_B")
                .hasLinkForColumn("LINKEDTYPE_KEY_B").with("LINKED", "KEY")
                .doesntHaveColumns("VALUE", "OTHER");

        tester.assertThatTable("T_SUB_SUB_B")
                .wasFoundOn(fr.uem.efluid.tests.inheritance.exclude.all.SubSubTypeB.class)
                .hasKey("KEYB", ColumnType.ATOMIC)
                .hasColumns("PROPERTY", "SECOND", "BPROP", "BSEC", "LINKEDTYPE_KEY_B")
                .hasLinkForColumn("LINKEDTYPE_KEY_B").with("LINKED", "KEY")
                .doesntHaveColumns("VALUE", "OTHER");

    }
}
