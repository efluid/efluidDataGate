package fr.uem.efluid.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fr.uem.efluid.stubs.TesterWithIndependentTransaction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository.Extraction;
import fr.uem.efluid.model.repositories.ManagedRegenerateRepository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {IntegrationTestConfig.class})
@Transactional(isolation = Isolation.READ_UNCOMMITTED)
public class ManagedParametersRepositoryIntegrationTest {

    @Autowired
    private TesterWithIndependentTransaction tester;

    @Autowired
    private ManagedExtractRepository extracted;

    @Autowired
    private ManagedRegenerateRepository regenerated;


    @Test
    public void testExtractCurrentContentLow() {
        this.tester. setupDatabase("diff7");
        try (Extraction extraction = this.extracted.extractCurrentContent(this.tester.dict(), new HashMap<>(),this.tester.proj())) {
            Map<String, String> raw = extraction.stream()
                    .collect(Collectors.toMap(ContentLine::getKeyValue, ContentLine::getPayload));
            this.tester.assertDbContentIs(raw, "diff7/actual.csv");
        }
    }

    @Test
    public void testRegenerateKnewContentUltraLow() {
        this.tester.setupDatabase("diff10");
        Map<String, String> raw = this.regenerated.regenerateKnewContent(this.tester.dict());
        this.tester.assertDbContentIs(raw, "diff10/knew.csv");
    }

    @Test
    public void testRegenerateKnewContentLow() {
        this.tester.setupDatabase("diff7");
        Map<String, String> raw = this.regenerated.regenerateKnewContent(this.tester.dict());
        this.tester.assertDbContentIs(raw, "diff7/knew.csv");
    }

    @Test
    public void testExtractCurrentContentHeavy() {
        this.tester. setupDatabase("diff8");
        try (Extraction extraction = this.extracted.extractCurrentContent( this.tester.dict(), new HashMap<>(),this.tester.proj())) {
            Map<String, String> raw = extraction.stream()
                    .collect(Collectors.toMap(ContentLine::getKeyValue, ContentLine::getPayload));
            this.tester.assertDbContentIs(raw, "diff8/actual.csv");
        }
    }

    @Test
    public void testRegenerateKnewContentHeavy() {
        this.tester.setupDatabase("diff8");
        Map<String, String> raw = this.regenerated.regenerateKnewContent(this.tester.dict());
        this.tester.assertDbContentIs(raw, "diff8/knew.csv");
    }
}
