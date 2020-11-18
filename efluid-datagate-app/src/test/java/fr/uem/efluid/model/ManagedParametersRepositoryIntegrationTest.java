package fr.uem.efluid.model;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.repositories.KnewContentRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository;
import fr.uem.efluid.model.repositories.ManagedExtractRepository.Extraction;
import fr.uem.efluid.stubs.TesterWithIndependentTransaction;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Transactional
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest(classes = {IntegrationTestConfig.class})
@Sql(scripts = "classpath:drop_all.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ManagedParametersRepositoryIntegrationTest {

    @Autowired
    private TesterWithIndependentTransaction tester;

    @Autowired
    private ManagedExtractRepository extracted;

    @Autowired
    private KnewContentRepository knewContents;

    @Test
    public void testExtractCurrentContentLow() {
        this.tester.setupDatabase("diff7");
        try (Extraction extraction = this.extracted.extractCurrentContent(this.tester.dict(), new HashMap<>(), this.tester.proj())) {
            Map<String, String> raw = extraction.stream()
                    .collect(Collectors.toMap(ContentLine::getKeyValue, ContentLine::getPayload));
            this.tester.assertDbContentIs(raw, "diff7/actual.csv");
        }
    }

    @Test
    public void testRegenerateKnewContentUltraLow() {
        this.tester.setupDatabase("diff10");
        Collection<String> keys = this.knewContents.knewContentKeys(this.tester.dict());
        Map<String, String> raw = this.knewContents.knewContentForKeys(this.tester.dict(), keys);
        this.tester.assertDbContentIs(raw, "diff10/knew.csv");
    }

    @Test
    public void testRegenerateKnewContentLow() {
        this.tester.setupDatabase("diff7");
        Collection<String> keys = this.knewContents.knewContentKeys(this.tester.dict());
        Map<String, String> raw = this.knewContents.knewContentForKeys(this.tester.dict(), keys);
        this.tester.assertDbContentIs(raw, "diff7/knew.csv");
    }

    @Test
    public void testExtractCurrentContentHeavy() {
        this.tester.setupDatabase("diff8");
        try (Extraction extraction = this.extracted.extractCurrentContent(this.tester.dict(), new HashMap<>(), this.tester.proj())) {
            Map<String, String> raw = extraction.stream()
                    .collect(Collectors.toMap(ContentLine::getKeyValue, ContentLine::getPayload));
            this.tester.assertDbContentIs(raw, "diff8/actual.csv");
        }
    }

    @Test
    public void testRegenerateKnewContentHeavy() {
        this.tester.setupDatabase("diff8");
        Collection<String> keys = this.knewContents.knewContentKeys(this.tester.dict());
        final Map<String, String> raw = new HashMap<>();

        AtomicInteger groupCnt = new AtomicInteger(0);

        // Need to process by step for large content
        keys.stream().collect(Collectors.partitioningBy(t -> {
            if (groupCnt.incrementAndGet() > 1000) {
                groupCnt.set(0);
                return true;
            }
            return false;
        })).values().forEach(i -> raw.putAll(this.knewContents.knewContentForKeys(this.tester.dict(), i)));

        this.tester.assertDbContentIs(raw, "diff8/knew.csv");
    }
}
