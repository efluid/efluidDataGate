package fr.uem.efluid;

import org.junit.Test;

/**
 * Test on ParameterValue search (by annotation or with other rules)
 */import static fr.uem.efluid.GeneratorTester.*;
        import static fr.uem.efluid.GeneratorTester.onPackage;

public class AnnotationParameterIgnoredExcludeClass {

    @Test
    public void testIgnoredParameterForClass() {
        GeneratorTester tester = onPackage("fr.uem.efluid.sample.remarks").generate();
        tester.assertFoundDomainsAre("Remarques Efluid", "Entities Efluid");

        tester.assertThatContentWereIdentified();

        tester.assertThatTable("CLASS_TO_IGNORED").doesntExist();
    }

    @Test
    public void testNoIgnoredParameterForClass() {
        GeneratorTester tester = onPackage("fr.uem.efluid.sample.remarks").generate();
        tester.assertFoundDomainsAre("Remarques Efluid", "Entities Efluid");

        tester.assertThatContentWereIdentified();

        tester.assertThatTable("CLASS_NOT_IGNORED").exists();
    }
}
