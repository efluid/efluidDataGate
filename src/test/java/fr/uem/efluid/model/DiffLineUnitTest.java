package fr.uem.efluid.model;

import static fr.uem.efluid.model.entities.IndexAction.ADD;
import static fr.uem.efluid.model.entities.IndexAction.REMOVE;
import static fr.uem.efluid.model.entities.IndexAction.UPDATE;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DiffLineUnitTest {

	@Test
	public void testCombinedDiffLineAllEliminated() {

		UUID dict = UUID.randomUUID();
		String key = "NAME1";

		DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
				DiffLine.combined(dict, key, "ADD1", ADD),
				DiffLine.combined(dict, key, null, REMOVE),
				DiffLine.combined(dict, key, "ADD2", ADD),
				DiffLine.combined(dict, key, null, REMOVE)));

		Assert.assertNull(combined);
	}

	@Test
	public void testCombinedDiffLineStayAdd() {

		UUID dict = UUID.randomUUID();
		String key = "NAME1";

		DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
				DiffLine.combined(dict, key, "ADD1", ADD),
				DiffLine.combined(dict, key, null, REMOVE),
				DiffLine.combined(dict, key, "ADD2", ADD)));

		Assert.assertEquals(ADD, combined.getAction());
		Assert.assertEquals("ADD2", combined.getPayload());
	}

	@Test
	public void testCombinedDiffLineStayAddFromModified() {

		UUID dict = UUID.randomUUID();
		String key = "NAME1";

		DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
				DiffLine.combined(dict, key, "ADD1", ADD),
				DiffLine.combined(dict, key, null, REMOVE),
				DiffLine.combined(dict, key, "ADD2", ADD),
				DiffLine.combined(dict, key, "MODIF3", UPDATE)));

		Assert.assertEquals(ADD, combined.getAction());
		Assert.assertEquals("MODIF3", combined.getPayload());
	}

	@Test
	public void testCombinedDiffLineComplex() {

		UUID dict = UUID.randomUUID();
		String key = "NAME1";

		DiffLine combined = DiffLine.combinedOnSameTableAndKey(Arrays.asList(
				DiffLine.combined(dict, key, "ADD1", ADD),
				DiffLine.combined(dict, key, "MODIF1", UPDATE),
				DiffLine.combined(dict, key, null, REMOVE),
				DiffLine.combined(dict, key, "ADD2", ADD),
				DiffLine.combined(dict, key, "MODIF2", UPDATE),
				DiffLine.combined(dict, key, "MODIF3", UPDATE)));

		Assert.assertEquals(ADD, combined.getAction());
		Assert.assertEquals("MODIF3", combined.getPayload());
	}
}
