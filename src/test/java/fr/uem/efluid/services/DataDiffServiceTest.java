package fr.uem.efluid.services;

import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.uem.efluid.TestUtils;
import fr.uem.efluid.model.entities.IndexEntry;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DataDiffServiceTest {

	@Test
	public void testGenerateDiffIndexDiff1() {
		Map<String, String> diff1Actual = TestUtils.readDataset("diff1/actual.csv");
		Map<String, String> diff1Knew = TestUtils.readDataset("diff1/knew.csv");

		// Static internal
		Collection<IndexEntry> index = DataDiffService.generateDiffIndex(diff1Knew, diff1Actual);
		Assert.assertEquals(0, index.size());
	}
}
