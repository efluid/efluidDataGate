package fr.uem.efluid.services;

import static fr.uem.efluid.model.entities.IndexAction.ADD;
import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static fr.uem.efluid.model.entities.IndexAction.UPDATE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.utils.ApplicationException;
import fr.uem.efluid.utils.DatasourceUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class CommitServiceTest {

    private static final String P = TestUtils.SOURCE_TABLE_NAME;
    private static final String C = TestUtils.SOURCE_CHILD_TABLE_NAME;

    @Autowired
    private ApplyDiffService service;

    @Autowired
    private TestDataLoader loader;

    @Autowired
    @Qualifier(DatasourceUtils.MANAGED_TRANSACTION_MANAGER)
    private PlatformTransactionManager managedDbTransactionManager;

    public void setupDatabase(String update) {
        this.loader.setupDatabaseForUpdate(update);
    }

    @Test
    public void testHistoryIsSplitted() {

        setupDatabase("update1");

        // 4 items each
        this.loader.assertSourceSize(4);
        this.loader.assertSourceChildSize(4);

        List<DiffLine> diff = Arrays.asList(
                this.loader.initIndexEntry(C, "5", ADD, "VALUE=\"child-test\",PARENT=\"7\""),
                this.loader.initIndexEntry(P, "5", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"555\""),
                this.loader.initIndexEntry(P, "6", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"666\""),
                this.loader.initIndexEntry(C, "6", ADD, "VALUE=\"child-test\",PARENT=\"8\""),
                this.loader.initIndexEntry(P, "7", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"777\""),
                this.loader.initIndexEntry(P, "8", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""),
                this.loader.initIndexEntry(P, "9", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""),
                this.loader.initIndexEntry(P, "10", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""),
                this.loader.initIndexEntry(P, "11", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""),
                this.loader.initIndexEntry(P, "12", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""));


        this.service.applyDiff(diff, new HashMap<>());

        Assert.assertEquals(10, this.service.getHistory(1, P).getTotalCount());
    }
}
