package fr.uem.efluid.services;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.*;
import fr.uem.efluid.model.repositories.*;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.tools.AttachmentProcessor;
import fr.uem.efluid.tools.SqlAttachmentProcessor;
import fr.uem.efluid.utils.DataGenerationUtils;
import fr.uem.efluid.utils.FormatUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = {IntegrationTestConfig.class})
public class TestSqlAttachmentProcessor {

    @Autowired
    private JdbcTemplate managedSource;

    @Autowired
    private CommitRepository commits;

    @Autowired
    private ProjectRepository projects;

    @Autowired
    private VersionRepository versions;

    @Autowired
    private UserRepository users;

    @Autowired
    private ApplyHistoryEntryRepository history;

    private static final String SCRIPT = "-- Une requête quelconque\r\n" +
            "SELECT * FROM " + TestUtils.SOURCE_TABLE_NAME + ";\r\n";

    private static final String FORMATED = "<span class=\"sql-comment\">-- Une requête quelconque</span>\r<br/>" +
            "SELECT * FROM " + TestUtils.SOURCE_TABLE_NAME + ";\r<br/>";

    @Test
    public void testDisplay() {

        SqlAttachmentProcessor proc = new SqlAttachmentProcessor(this.managedSource, this.history);
        AttachmentProcessor.Compliant comp = initComp();

        String display = FormatUtils.toString(proc.display(comp));

        assertEquals(FORMATED, display);
    }

    @Test
    public void testExecute() {

        User user = this.users.save(DataGenerationUtils.user("test"));
        Project pro = this.projects.save(DataGenerationUtils.project("demo"));
        Version ver = this.versions.save(DataGenerationUtils.version("V1", pro));

        Commit commit = this.commits.save(DataGenerationUtils.commit("commit1", user, 0, pro, ver));

        SqlAttachmentProcessor proc = new SqlAttachmentProcessor(this.managedSource, this.history);
        AttachmentProcessor.Compliant comp = initComp();

        assertEquals(0, this.history.findAll().size());

        proc.execute(user, comp, commit);

        assertEquals(1, this.history.findAll().size());
    }

    private static AttachmentProcessor.Compliant initComp() {

        return new AttachmentProcessor.Compliant() {

            @Override
            public UUID getUuid() {
                return null;
            }

            @Override
            public String getName() {
                return "script.sql";
            }

            @Override
            public byte[] getData() {
                return FormatUtils.toBytes(SCRIPT);
            }

            @Override
            public AttachmentType getType() {
                return AttachmentType.SQL_FILE;
            }

        };
    }
}
