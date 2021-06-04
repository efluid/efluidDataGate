package fr.uem.efluid.services;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.CommitState;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.services.types.PilotedCommitPreparation;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.stubs.TesterWithIndependentTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Transactional
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = {IntegrationTestConfig.class})
public class PrepareDiffServiceIntegrationTest {

    @Autowired
    private PrepareIndexService service;

    @Autowired
    private TesterWithIndependentTransaction tester;


    @Test
    public void testProcessDiffNoIndex() {

        this.tester.setupDatabase("diff7");
        PilotedCommitPreparation<PreparedIndexEntry> preparation = new PilotedCommitPreparation<>(CommitState.LOCAL);
        this.service.completeLocalDiff(
                preparation,
                this.tester.dict(),
                new HashMap<>(),
                this.tester.proj());

        assertEquals(0, preparation.getDiffContent().size());
    }

    @Test
    public void testProcessDiffLargeIndex() {

        this.tester.setupDatabase("diff8");
        PilotedCommitPreparation<PreparedIndexEntry> preparation = new PilotedCommitPreparation<>(CommitState.LOCAL);
        this.service.completeLocalDiff(
                preparation,
                this.tester.dict(),
                new HashMap<>(),
                this.tester.proj());
        assertEquals(80 + 100 + 85, preparation.getDiffContent().size());
        List<PreparedIndexEntry> adds = preparation.getDiffContent().stream()
                .filter(i -> i.getAction() == IndexAction.ADD)
                .collect(Collectors.toList());
        List<PreparedIndexEntry> removes = preparation.getDiffContent().stream()
                .filter(i -> i.getAction() == IndexAction.REMOVE)
                .collect(Collectors.toList());
        List<PreparedIndexEntry> updates = preparation.getDiffContent().stream()
                .filter(i -> i.getAction() == IndexAction.UPDATE)
                .collect(Collectors.toList());
        assertEquals(85, adds.size());
        assertEquals(100, removes.size());
        assertEquals(80, updates.size());
    }
}
