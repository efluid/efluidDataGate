package fr.uem.efluid;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.SecurityService;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.services.types.DictionaryEntrySummary;
import fr.uem.efluid.tests.deleteAfterUpload.EfluidFunction;
import fr.uem.efluid.tests.deleteAfterUpload.EfluidWorkflowDomain;
import fr.uem.efluid.tests.deleteAfterUpload.EfluidWorkflowStepRoot;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static fr.uem.efluid.GeneratorTester.onPackage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A test set which also start a FULL Datagate instance to test dictionary push results after a generation
 * <p>
 * Initialized test data are specified in data.sql file
 */
@Transactional
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {IntegrationTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DictionaryPushAfterGenerationTest {

    // From data.sql
    private static final String TOKEN = "123456789";

    private static final String VERSION = "TEST_SIMPLE";

    // TODO : Add here all test cases on dictionary push

    @LocalServerPort
    private int serverPort;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserHolder userHolder;

    @Autowired
    private DictionaryManagementService dictionaryManagementService;

    @Autowired
    private ProjectManagementService projectManagementService;


    @Test
    public void testVersionIsUpdatedOnUploadWithModelId() {

        var tester = onPackage("fr.uem.efluid.tests.autoUpload")
                .withSpecifiedVersion(VERSION)
                .generate();

        tester.assertThatContentWereIdentified();

        tester.exportWithUpload(this.serverPort, TOKEN);
        tester.assertNoExportErrorWasMet();

        // Need to say that we are on same project
        switchUserToUploadedProject(tester);

        Version version = this.dictionaryManagementService.getLastUpdatedVersion();

        // Check it is the version from model identifier
        assertThat(version.getName()).isEqualTo(VERSION);
        assertThat(version.getModelIdentity()).isEqualTo(FixedModelIdentifier.VERSION);

        // Check content is here
        assertThat(version.getDictionaryContent()).isNotEmpty();
    }

    @Test
    public void testSimpleUploadValidateCreatedDictionary() {

        var tester = onPackage("fr.uem.efluid.tests.autoUpload")
                .withSpecifiedVersion(VERSION)
                .generate();

        // Check content was found
        tester.assertThatContentWereIdentified();
        tester.assertThatTable("T_TABLE").exists();

        // When pushed to server
        tester.exportWithUpload(this.serverPort, TOKEN);
        tester.assertNoExportErrorWasMet();

        // Need to say that we are on same project
        switchUserToUploadedProject(tester);

        // Check existing content in datagate instance
        var entrySummaryList = this.dictionaryManagementService.getDictionnaryEntrySummaries();

        assertThat(entrySummaryList).hasSize(1);
        assertThat(entrySummaryList.get(0).getTableName()).isEqualTo("T_TABLE");
        assertThat(entrySummaryList.get(0).getUuid()).isEqualTo(tester.getUuidForTable("T_TABLE"));

        var domains = this.dictionaryManagementService.getAvailableFunctionalDomains();

        assertThat(domains).hasSize(1);
        assertThat(domains.get(0).getName()).isEqualTo("My Domain");
        assertThat(domains.get(0).getUuid()).isEqualTo(tester.getUuidForDomain("My Domain"));

    }

    @Test
    public void testEfluidModelGeneration() {

        var tester = onPackage(fr.uem.efluid.tests.deleteAfterUpload.EfluidFunction.class.getPackageName())
                .withSpecifiedVersion(VERSION)
                .generate();

        tester.assertThatContentWereIdentified();

        tester.assertThatTable("TETAPEWORKFLOW")
                .wasFoundOn(fr.uem.efluid.tests.deleteAfterUpload.EfluidWorkflowStepRoot.class)
                .wasFoundOn(fr.uem.efluid.tests.deleteAfterUpload.EfluidWorkflowStepChild.class)
                .isInDomain(EfluidWorkflowDomain.NAME)
                .isInProject(EfluidWorkflowDomain.PROJECT)
                .hasKey("KEYONE", ColumnType.STRING)
                .hasKey("KEYTWO", ColumnType.ATOMIC)
                .hasColumns("LABEL", "COMMENT", "LABELSTATUS", "STEPNUMBER", "DELAY", "REFERENCE", "SUPPROBJETTRAITEAUTORISEE")
                .doesntHaveColumns("STEPROLES");

        tester.assertThatTable("TETAPEWKFOBJGEN")
                .wasFoundOn(fr.uem.efluid.tests.deleteAfterUpload.EfluidWorkflowStepChild.class)
                .isInDomain(EfluidWorkflowDomain.NAME)
                .isInProject(EfluidWorkflowDomain.PROJECT)
                .hasKey("KEYONE", ColumnType.STRING)
                .hasKey("KEYTWO", ColumnType.ATOMIC)
                .hasColumns("LABEL", "COMMENT", "LABELSTATUS", "STEPNUMBER", "DELAY", "REFERENCE", "SUPPROBJETTRAITEAUTORISEE", "FUNCTION_ID")
                .hasLinkForColumn("FUNCTION_ID");

        tester.assertThatTable("TMODELEFONCTION")
                .wasFoundOn(fr.uem.efluid.tests.deleteAfterUpload.EfluidFunction.class)
                .isInDomain(EfluidWorkflowDomain.NAME)
                .isInProject(EfluidWorkflowDomain.PROJECT)
                .hasKey("ID", ColumnType.ATOMIC)
                .hasColumns("VALUE", "CREATEDAT");
    }

    @Test
    public void testEfluidModelUpload() {

        var tester = onPackage(fr.uem.efluid.tests.deleteAfterUpload.EfluidFunction.class.getPackageName())
                .withSpecifiedVersion(VERSION)
                .generate();

        tester.assertThatContentWereIdentified();
        tester.exportWithUpload(this.serverPort, TOKEN);
        tester.assertNoExportErrorWasMet();

        // Need to say that we are on same project
        switchUserToUploadedProject(tester);

        Version version = this.dictionaryManagementService.getLastUpdatedVersion();

        // Check it is the version from model identifier
        assertThat(version.getName()).isEqualTo(VERSION);
        assertThat(version.getModelIdentity()).isEqualTo(FixedModelIdentifier.VERSION);

        // Check content is here
        assertThat(version.getDictionaryContent()).isNotEmpty();

        // Check available table entries
        var tabs = this.dictionaryManagementService.getDictionnaryEntrySummaries();

        assertThat(tabs).hasSize(3);

        // Check all table entries on pushed content
        var fetapwfl = tabs.stream().filter(e -> e.getTableName().equals("TETAPEWORKFLOW")).findFirst();
        var fobjgen = tabs.stream().filter(e -> e.getTableName().equals("TETAPEWKFOBJGEN")).findFirst();
        var fmodfunc = tabs.stream().filter(e -> e.getTableName().equals("TMODELEFONCTION")).findFirst();

        assertThat(fetapwfl).isPresent();
        assertThat(fobjgen).isPresent();
        assertThat(fmodfunc).isPresent();

        assertThat(fetapwfl.get().getDomainName()).isEqualTo(EfluidWorkflowDomain.NAME);
        assertThat(fetapwfl.get().getName()).isEqualTo("WorkflowStep");

        assertDictionnaryColumnsAre(fetapwfl.get(),
                key("KEYONE", ColumnType.STRING),
                key("KEYTWO", ColumnType.ATOMIC),
                col("LABEL"),
                col("COMMENT"),
                col("LABELSTATUS"),
                col("STEPNUMBER"),
                col("DELAY"),
                col("REFERENCE"),
                col("SUPPROBJETTRAITEAUTORISEE")
        );

        assertThat(fobjgen.get().getDomainName()).isEqualTo(EfluidWorkflowDomain.NAME);
        assertThat(fobjgen.get().getName()).isEqualTo("WorkflowStepGenericObject");

        assertDictionnaryColumnsAre(fobjgen.get(),
                key("KEYONE", ColumnType.STRING),
                key("KEYTWO", ColumnType.ATOMIC),
                col("LABEL"),
                col("COMMENT"),
                col("LABELSTATUS"),
                col("STEPNUMBER"),
                col("DELAY"),
                col("REFERENCE"),
                col("SUPPROBJETTRAITEAUTORISEE"),
                ln("FUNCTION_ID", "TMODELEFONCTION", "ID")
        );

        assertThat(fmodfunc.get().getDomainName()).isEqualTo(EfluidWorkflowDomain.NAME);
        assertThat(fmodfunc.get().getName()).isEqualTo("Function");

        assertDictionnaryColumnsAre(fmodfunc.get(),
                key("ID", ColumnType.ATOMIC),
                col("VALUE"),
                col("CREATEDAT")
        );
    }

    @Test
    public void testEfluidModelDelete() {

        var tester = onPackage(fr.uem.efluid.tests.deleteAfterUpload.EfluidFunction.class.getPackageName())
                .withSpecifiedVersion(VERSION)
                .generate();

        tester.assertThatContentWereIdentified();
        tester.exportWithUpload(this.serverPort, TOKEN);
        tester.assertNoExportErrorWasMet();

        // Need to say that we are on same project
        switchUserToUploadedProject(tester);

        Version version = this.dictionaryManagementService.getLastUpdatedVersion();

        // Check it is the version from model identifier
        assertThat(version.getName()).isEqualTo(VERSION);
        assertThat(version.getModelIdentity()).isEqualTo(FixedModelIdentifier.VERSION);

        // Check content is here
        assertThat(version.getDictionaryContent()).isNotEmpty();

        // Check available table entries
        var tabs = this.dictionaryManagementService.getDictionnaryEntrySummaries();

        assertThat(tabs).hasSize(3);

        // Check all table entries on pushed content
        var fetapwfl = tabs.stream().filter(e -> e.getTableName().equals("TETAPEWORKFLOW")).findFirst();
        var fobjgen = tabs.stream().filter(e -> e.getTableName().equals("TETAPEWKFOBJGEN")).findFirst();
        var fmodfunc = tabs.stream().filter(e -> e.getTableName().equals("TMODELEFONCTION")).findFirst();

        assertThat(fetapwfl).isPresent();
        assertThat(fobjgen).isPresent();
        assertThat(fmodfunc).isPresent();

        UUID etapwflId = fetapwfl.get().getUuid();
        UUID objgenId = fobjgen.get().getUuid();
        UUID modfuncId = fmodfunc.get().getUuid();

        this.dictionaryManagementService.deleteDictionaryEntry(etapwflId);

        // Check updated
        tabs = this.dictionaryManagementService.getDictionnaryEntrySummaries();
        assertThat(tabs).hasSize(2);
    }

    private void switchUserToUploadedProject(GeneratorTester tester) {
        this.userHolder.setCurrentUser(new User("login"));
        this.projectManagementService.selectProject(tester.getDefaultProjectUuid());
    }

    private void assertDictionnaryColumnsAre(DictionaryEntrySummary dict, DictionaryEntryEditData.ColumnEditData... columns) {
        DictionaryEntryEditData edit = this.dictionaryManagementService.editEditableDictionaryEntry(dict.getUuid());

        assertThat(edit.getColumns()).hasSize(columns.length);

        for (DictionaryEntryEditData.ColumnEditData col : columns) {

            var found = edit.getColumns().stream().filter(c -> c.getName().equals(col.getName())).findFirst();
            assertThat(found).as("Editable column with name %s for table %s is not found", col.getName(), dict.getName()).isPresent();
            assertThat(found.get().getType()).as("Type of column with name %s for table %s", col.getName(), dict.getName()).isEqualTo(col.getType());
            assertThat(found.get().isKey()).as("Key check of column with name %s for table %s", col.getName(), dict.getName()).isEqualTo(col.isKey());
            assertThat(found.get().isSelected()).as("Select check of column with name %s for table %s", col.getName(), dict.getName()).isEqualTo(col.isSelected());
        }
    }

    private static DictionaryEntryEditData.ColumnEditData key(String name, ColumnType type) {
        DictionaryEntryEditData.ColumnEditData key = new DictionaryEntryEditData.ColumnEditData();
        key.setKey(true);
        key.setType(type);
        key.setName(name);
        key.setSelected(true);
        return key;
    }

    private static DictionaryEntryEditData.ColumnEditData col(String name) {
        DictionaryEntryEditData.ColumnEditData key = new DictionaryEntryEditData.ColumnEditData();
        key.setKey(false);
        key.setType(ColumnType.UNKNOWN);
        key.setName(name);
        key.setSelected(true);
        return key;
    }

    private static DictionaryEntryEditData.ColumnEditData ln(String name, String toTable, String toCol) {
        DictionaryEntryEditData.ColumnEditData key = new DictionaryEntryEditData.ColumnEditData();
        key.setKey(false);
        key.setType(ColumnType.UNKNOWN);
        key.setName(name);
        key.setForeignKeyTable(toTable);
        key.setForeignKeyColumn(toCol);
        key.setSelected(true);
        return key;
    }
}
