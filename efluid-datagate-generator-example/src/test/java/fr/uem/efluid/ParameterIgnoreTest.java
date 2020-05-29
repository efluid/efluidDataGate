package fr.uem.efluid;

import org.junit.Test;
import static fr.uem.efluid.GeneratorTester.onPackage;

public class ParameterIgnoreTest {

    @Test
    public void testIgnoredParameterIsUsed() {
        GeneratorTester tester = onPackage("fr.uem.efluid.sample.remarks").generate();

        tester.assertFoundDomainsAre("Remarques Efluid", "Entities Efluid");

        tester.assertThatContentWereIdentified();

        tester.assertThatTable("CLASS_TO_IGNORED").doesntExist();
    }

    @Test
    public void testIgnoredParameterIsNotUsed() {
        GeneratorTester tester = onPackage("fr.uem.efluid.sample.remarks").generate();

        tester.assertFoundDomainsAre("Remarques Efluid", "Entities Efluid");

        tester.assertThatContentWereIdentified();

        tester.assertThatTable("CLASS_NOT_IGNORED").exists();
    }
}
