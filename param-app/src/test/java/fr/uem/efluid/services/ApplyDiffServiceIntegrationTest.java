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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import fr.uem.efluid.IntegrationTestConfig;
import fr.uem.efluid.model.DiffLine;
import fr.uem.efluid.stubs.TestDataLoader;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.utils.ApplicationException;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Ignore
@RunWith(SpringRunner.class) 
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
		this.loader.setupDatabaseForUpdate(update);
	}

	@Test
	public void testApplyDiffSimpleAddSuccess() {

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

		this.service.applyDiff(diff, new HashMap<>());

		// Added items
		this.loader.assertSourceSize(8);
		this.loader.assertSourceChildSize(6);
	}

	@Test
	public void testApplyDiffSimpleAddFailOnConstraintAndRollback() {

		setupDatabase("update1");

		// 4 items each
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(4);

		List<DiffLine> diff = Arrays.asList(
				/* WRONG PARENT */ this.loader.initIndexEntry(C, "5", ADD, "VALUE=\"child-test\",PARENT=\"700\""),
				this.loader.initIndexEntry(P, "5", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"555\""),
				this.loader.initIndexEntry(P, "6", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"666\""),
				/* WRONG PARENT */this.loader.initIndexEntry(C, "6", ADD, "VALUE=\"child-test\",PARENT=\"99\""),
				this.loader.initIndexEntry(P, "7", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"777\""),
				this.loader.initIndexEntry(P, "8", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""));

		try {
			this.service.applyDiff(diff, new HashMap<>());
			Assert.fail();
		}

		// Required
		catch (ApplicationException e) {
			// DB unchanged
			this.loader.assertSourceSize(4);
			this.loader.assertSourceChildSize(4);
		}
	}

	@Test
	public void testApplyDiffSimpleRemoveSuccess() {

		setupDatabase("update2");

		// Combined items
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(8);

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", REMOVE, null),
				this.loader.initIndexEntry(P, "2", REMOVE, null),
				this.loader.initIndexEntry(C, "6", REMOVE, null),
				this.loader.initIndexEntry(C, "7", REMOVE, null),
				this.loader.initIndexEntry(C, "8", REMOVE, null));

		this.service.applyDiff(diff, new HashMap<>());

		// Removed items
		this.loader.assertSourceSize(3);
		this.loader.assertSourceChildSize(4);
	}

	@Test
	public void testApplyDiffSimpleRemoveFailOnConstraintAndRollback() {

		setupDatabase("update2");

		// Combined items
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(8);

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", REMOVE, null),
				/* STILL HAS CHILD */this.loader.initIndexEntry(P, "2", REMOVE, null),
				/* STILL HAS CHILD */this.loader.initIndexEntry(P, "1", REMOVE, null),
				this.loader.initIndexEntry(C, "6", REMOVE, null),
				this.loader.initIndexEntry(C, "8", REMOVE, null));

		try {
			this.service.applyDiff(diff, new HashMap<>());
			Assert.fail();
		}

		// Required
		catch (ApplicationException e) {
			// DB unchanged
			this.loader.assertSourceSize(4);
			this.loader.assertSourceChildSize(8);
		}
	}

	@Test
	public void testApplyDiffSimpleRemoveFailOnUnknownRefAndRollback() {

		setupDatabase("update2");

		// Combined items
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(8);

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", REMOVE, null),
				this.loader.initIndexEntry(P, "2", REMOVE, null),
				/* ID NOT EXIST */ this.loader.initIndexEntry(C, "16", REMOVE, null),
				this.loader.initIndexEntry(C, "7", REMOVE, null),
				this.loader.initIndexEntry(C, "8", REMOVE, null));

		try {
			this.service.applyDiff(diff, new HashMap<>());
			Assert.fail();
		}

		// Required
		catch (ApplicationException e) {
			// DB unchanged
			this.loader.assertSourceSize(4);
			this.loader.assertSourceChildSize(8);
		}
	}

	@Test
	public void testApplyDiffSimpleUpdateSuccess() {

		setupDatabase("update2");

		// Combined items
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(8);
		this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 2);
		this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 3);
		this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("1234"));
		this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("1234"));

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""),
				this.loader.initIndexEntry(P, "1", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"111changed\""),
				this.loader.initIndexEntry(P, "4", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"444changed\""),
				this.loader.initIndexEntry(C, "4", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""));

		this.service.applyDiff(diff, new HashMap<>());

		// Modified items
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(8);
		this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 1);
		this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 1);
		this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("111changed"));
		this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("444changed"));
	}

	@Test
	public void testApplyDiffSimpleUpdateFailOnUnknownRefAndRollback() {

		setupDatabase("update2");

		// Combined items
		this.loader.assertSourceSize(4);
		this.loader.assertSourceChildSize(8);
		this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 2);
		this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 3);
		this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("1234"));
		this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("1234"));

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""),
				this.loader.initIndexEntry(P, "1", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"111changed\""),
				this.loader.initIndexEntry(P, "4", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"444changed\""),
				/* ID NOT EXIST */this.loader.initIndexEntry(C, "12", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""));

		try {
			this.service.applyDiff(diff, new HashMap<>());
			Assert.fail();
		}

		// Required
		catch (ApplicationException e) {
			// DB unchanged
			this.loader.assertSourceSize(4);
			this.loader.assertSourceChildSize(8);
			this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 2);
			this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 3);
			this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("1234"));
			this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("1234"));
		}
	}

	@Test
	public void testApplyDiffCombinedSuccess() {

		setupDatabase("update3");

		// Combined items
		this.loader.assertSourceSize(7);
		this.loader.assertSourceChildSize(12);
		this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 2);
		this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 3);
		this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("1234"));
		this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("1234"));

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""),
				this.loader.initIndexEntry(P, "1", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"111changed\""),
				this.loader.initIndexEntry(P, "4", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"444changed\""),
				this.loader.initIndexEntry(C, "4", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""),
				this.loader.initIndexEntry(C, "12", REMOVE, null),
				this.loader.initIndexEntry(P, "6", REMOVE, null),
				this.loader.initIndexEntry(C, "3", REMOVE, null),
				this.loader.initIndexEntry(C, "7", REMOVE, null),
				this.loader.initIndexEntry(C, "13", ADD, "VALUE=\"child-test\",PARENT=\"8\""),
				this.loader.initIndexEntry(P, "8", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""),
				this.loader.initIndexEntry(C, "14", ADD, "VALUE=\"child-test\",PARENT=\"8\""),
				this.loader.initIndexEntry(P, "9", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"999\""));

		this.service.applyDiff(diff, new HashMap<>());

		// Modified items
		this.loader.assertSourceSize(7 - 1 + 2);
		this.loader.assertSourceChildSize(12 - 3 + 2);
		this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 1);
		this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 1);
		this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("111changed"));
		this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("444changed"));
	}

	@Test
	public void testApplyDiffCombinedFailOnUnknownRefAndRollback() {

		setupDatabase("update3");

		// Combined items
		this.loader.assertSourceSize(7);
		this.loader.assertSourceChildSize(12);
		this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 2);
		this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 3);
		this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("1234"));
		this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("1234"));

		List<DiffLine> diff = Arrays.asList(
				this.loader.initIndexEntry(C, "5", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""),
				this.loader.initIndexEntry(P, "1", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"111changed\""),
				this.loader.initIndexEntry(P, "4", UPDATE, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"444changed\""),
				this.loader.initIndexEntry(C, "4", UPDATE, "VALUE=\"child-test\",PARENT=\"1\""),
				this.loader.initIndexEntry(C, "12", REMOVE, null),
				/* ID NOT EXIST */this.loader.initIndexEntry(P, "45", REMOVE, null),
				this.loader.initIndexEntry(C, "3", REMOVE, null),
				this.loader.initIndexEntry(C, "7", REMOVE, null),
				this.loader.initIndexEntry(C, "13", ADD, "VALUE=\"child-test\",PARENT=\"8\""),
				this.loader.initIndexEntry(P, "8", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"888\""),
				this.loader.initIndexEntry(C, "14", ADD, "VALUE=\"child-test\",PARENT=\"8\""),
				this.loader.initIndexEntry(P, "9", ADD, "VALUE=\"test\",PRESET=\"loaded\",SOMETHING=\"999\""));

		try {
			this.service.applyDiff(diff, new HashMap<>());
			Assert.fail();
		}

		// Required
		catch (ApplicationException e) {
			// DB unchanged
			this.loader.assertSourceSize(7);
			this.loader.assertSourceChildSize(12);
			this.loader.assertSourceChildContentValidate(5, c -> c.getParent().getKey().longValue() == 2);
			this.loader.assertSourceChildContentValidate(4, c -> c.getParent().getKey().longValue() == 3);
			this.loader.assertSourceContentValidate(1, c -> c.getSomething().equals("1234"));
			this.loader.assertSourceContentValidate(4, c -> c.getSomething().equals("1234"));
		}
	}
}
