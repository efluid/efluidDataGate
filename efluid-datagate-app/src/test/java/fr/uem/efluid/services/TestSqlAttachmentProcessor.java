package fr.uem.efluid.services;

import static fr.uem.efluid.utils.DataGenerationUtils.user;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.entities.AttachmentType;
import fr.uem.efluid.model.entities.User;
import fr.uem.efluid.model.repositories.ApplyHistoryEntryRepository;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.tools.AttachmentProcessor;
import fr.uem.efluid.tools.SqlAttachmentProcessor;
import fr.uem.efluid.utils.FormatUtils;

/**
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RunWith(SpringRunner.class)
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class TestSqlAttachmentProcessor {

	@Autowired
	private JdbcTemplate managedSource;

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

		Assert.assertEquals(FORMATED, display);
	}

	@Test
	public void testExecute() {

		User user = user("testeur");

		SqlAttachmentProcessor proc = new SqlAttachmentProcessor(this.managedSource, this.history);
		AttachmentProcessor.Compliant comp = initComp();

		Assert.assertEquals(0, this.history.findAll().size());

		proc.execute(user, comp);

		Assert.assertEquals(1, this.history.findAll().size());
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
