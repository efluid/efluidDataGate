package fr.uem.efluid.services;

import static fr.uem.efluid.model.entities.IndexAction.ADD;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@SpringBootTest(classes = { IntegrationTestConfig.class })
public class ApplyDiffServiceIntegrationTest {

	private static final String P = TestUtils.SOURCE_TABLE_NAME;
	private static final String C = TestUtils.SOURCE_CHILD_TABLE_NAME;

	@Autowired
	private ApplyDiffService service;

	@Autowired
	private TestDataLoader loader;

	@Transactional
	public void setupDatabase(String update) {
		this.loader.setupSourceDatabaseForUpdate(update);
		this.loader.setupDictionnaryForUpdate();
	}

	@Test
	public void testApplyDiffSimpleAdd() {

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
				this.loader.initIndexEntry(P, "8", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""));

		this.service.applyDiff(diff);

		// Added items
		this.loader.assertSourceSize(8);
		this.loader.assertSourceChildSize(6);

	}

}
