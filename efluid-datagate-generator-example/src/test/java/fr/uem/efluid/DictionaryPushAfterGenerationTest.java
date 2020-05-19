package fr.uem.efluid;

import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.security.UserHolder;
import fr.uem.efluid.services.DictionaryManagementService;
import fr.uem.efluid.services.ProjectManagementService;
import fr.uem.efluid.services.SecurityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

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

    private void switchUserToUploadedProject(GeneratorTester tester) {
        this.userHolder.setCurrentUser(new User("login"));
        this.projectManagementService.selectProject(tester.getDefaultProjectUuid());
    }
}
