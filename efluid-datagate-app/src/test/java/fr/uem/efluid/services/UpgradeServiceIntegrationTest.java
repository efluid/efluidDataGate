package fr.uem.efluid.services;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.model.repositories.IndexRepository;
import fr.uem.efluid.model.repositories.UpgradeRepository;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.upgrades.InitPreviousPayloadUpgrade;
import fr.uem.efluid.upgrades.UpgradeProcess;
import fr.uem.efluid.utils.ApplicationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional(propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = {IntegrationTestConfig.class})
public class UpgradeServiceIntegrationTest {

    private static final String P = TestUtils.SOURCE_TABLE_NAME;
    private static final String C = TestUtils.SOURCE_CHILD_TABLE_NAME;

    @Autowired
    private InitPreviousPayloadUpgrade previousPayloadUpgrade;

    @Autowired
    private IndexRepository indexes;

    @Autowired
    private TestDataLoader loader;

    @Autowired
    private UpgradeRepository upgrades;

    @Before
    public void resetUpgrades() {
        this.upgrades.deleteAll();
        this.upgrades.flush();
    }

    public void setupDatabase(String update) {
        this.loader.setupIndexDatabaseForDiff(update);
    }

    @Test
    public void testNoUpgradeIfNonePresent() {

        assertThat(this.upgrades.findAll()).isEmpty();

        new UpgradeService(this.upgrades, new ArrayList<>()).applyUpgrades();
        new UpgradeService(this.upgrades, null).applyUpgrades();

        assertThat(this.upgrades.findAll()).isEmpty();
    }

    @Test
    public void testBasicUpgradeRecord() {

        LocalDateTime begin = LocalDateTime.now();

        assertThat(this.upgrades.findAll()).isEmpty();

        new UpgradeService(this.upgrades, Collections.singletonList(new TestUpgrade())).applyUpgrades();

        assertThat(this.upgrades.findAll()).hasSize(1);

        var record = this.upgrades.findById("test-upd");

        assertThat(record).isPresent();
        assertThat(record.get().getIndex()).isEqualTo(14);
        assertThat(record.get().getRunTime()).isAfterOrEqualTo(begin);
    }

    @Test
    public void testUpgradeMultiples() {

        assertThat(this.upgrades.findAll()).isEmpty();

        new UpgradeService(this.upgrades, Arrays.asList(new TestUpgrade(), this.previousPayloadUpgrade)).applyUpgrades();

        assertThat(this.upgrades.findAll()).hasSize(2);

        var record1 = this.upgrades.findById("test-upd");
        var record2 = this.upgrades.findById("previous-payload-init");

        assertThat(record1).isPresent();
        assertThat(record2).isPresent();
        assertThat(record1.get().getIndex()).isEqualTo(14);
        assertThat(record2.get().getIndex()).isEqualTo(1);
        assertThat(record1.get().getRunTime()).isAfterOrEqualTo(record2.get().getRunTime());
    }

    @Test
    public void testUpgradePreviousOnSimpleDiffApplied() {

        LocalDateTime begin = LocalDateTime.now();

        setupDatabase("diff9");

        assertThat(this.indexes.findAll()).isNotEmpty();
        assertThat(this.upgrades.findAll()).isEmpty();


        new UpgradeService(this.upgrades, Collections.singletonList(this.previousPayloadUpgrade)).applyUpgrades();

        assertThat(this.upgrades.findAll()).hasSize(1);

        var record = this.upgrades.findById("previous-payload-init");

        assertThat(record).isPresent();
        assertThat(record.get().getRunTime()).isAfterOrEqualTo(begin);
    }

    @Test
    public void testUpgradePreviousOnSimpleDiffResult() {


        setupDatabase("diff9");

        List<IndexEntry> all = this.indexes.findAll();

        assertThat(all).hasSize(15);

        // Default "direct init" will not create previous content
        assertThat(all).allMatch(i -> i.getPrevious() == null);

        new UpgradeService(this.upgrades, Collections.singletonList(this.previousPayloadUpgrade)).applyUpgrades();

        assertThat(all).filteredOn(i -> i.getPrevious() != null).hasSize(7);

        // Modified
        var i1 = all.stream().filter(i -> i.getKeyValue().equals("1") && i.getAction() == IndexAction.UPDATE).findFirst();
        assertThat(i1).isPresent();
        assertThat(i1.get().getPayload()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/TW9kaWZpZWQ=,SOMETHING=S/MQ==");
        assertThat(i1.get().getPrevious()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/SW5pdA==,SOMETHING=S/MQ==");

        // Deleted
        var i3 = all.stream().filter(i -> i.getKeyValue().equals("3") && i.getAction() == IndexAction.REMOVE).findFirst();
        assertThat(i3).isPresent();
        assertThat(i3.get().getPayload()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/SW5pdA==,SOMETHING=S/MQ==");
        assertThat(i3.get().getPrevious()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/SW5pdA==,SOMETHING=S/MQ==");

        // Not modified (unchanged)
        var i6 = all.stream().filter(i -> i.getKeyValue().equals("6")).findFirst();
        assertThat(i6).isPresent();
        assertThat(i6.get().getAction()).isEqualTo(IndexAction.ADD);
        assertThat(i6.get().getPayload()).isEqualTo("VALUE=S/U29tZXRoaW5n,PRESET=S/SW5pdA==,SOMETHING=S/MQ==");
        assertThat(i6.get().getPrevious()).isNull();

    }

    private static class TestUpgrade implements UpgradeProcess {

        @Override
        public boolean repeat() {
            return false;
        }

        @Override
        public int index() {
            return 14;
        }

        @Override
        public String name() {
            return "test-upd";
        }

        @Override
        public void runUpgrade() throws ApplicationException {
            // Does nothing
        }
    }

}
