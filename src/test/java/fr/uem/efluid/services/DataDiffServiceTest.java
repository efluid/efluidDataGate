package fr.uem.efluid.services;

import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.uem.efluid.TestUtils;
import fr.uem.efluid.model.Value;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.IndexEntry;
import fr.uem.efluid.utils.ManagedDiffUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class DataDiffServiceTest {

	// No spring load here, for simple diff needs
	private DataDiffService service = new DataDiffService();

	@Test
	public void testGenerateDiffIndexDiffNotChanged() {
		Collection<IndexEntry> index = getDiff("diff1");
		Assert.assertEquals(0, index.size());
	}

	@Test
	public void testGenerateDiffIndexAddedTwo() {
		Collection<IndexEntry> index = getDiff("diff2");
		Assert.assertEquals(2, index.size());
		Assert.assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.ADD));
		Assert.assertTrue(index.stream()
				.allMatch(i -> Value.mapped(ManagedDiffUtils.expandInternalValue(i.getPayload())).get("Value").getValueAsString()
						.equals("Different")));
	}

	@Test
	public void testGenerateDiffIndexRemovedTwo() {
		Collection<IndexEntry> index = getDiff("diff3");
		Assert.assertEquals(2, index.size());
		Assert.assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.REMOVE));
		Assert.assertTrue(index.stream().allMatch(i -> i.getPayload() == null));
	}

	@Test
	public void testGenerateDiffIndexModifiedTwo() {
		Collection<IndexEntry> index = getDiff("diff4");
		Assert.assertEquals(2, index.size());
		Assert.assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.UPDATE));
		Assert.assertTrue(index.stream().anyMatch(i -> i.getKeyValue().equals("5")));
		Assert.assertTrue(index.stream().anyMatch(i -> i.getKeyValue().equals("8")));
		Assert.assertTrue(index.stream()
				.allMatch(i -> Value.mapped(ManagedDiffUtils.expandInternalValue(i.getPayload())).get("Value").getValueAsString()
						.equals("Modified")));
	}

	@Test
	public void testGenerateDiffIndexAddedTwoRemovedTwoModifiedTwo() {
		Collection<IndexEntry> index = getDiff("diff5");
		Assert.assertEquals(6, index.size());
		Assert.assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).anyMatch(i -> i.getKeyValue().equals("5")));
		Assert.assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).anyMatch(i -> i.getKeyValue().equals("8")));
		Assert.assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).anyMatch(i -> i.getKeyValue().equals("2")));
		Assert.assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).anyMatch(i -> i.getKeyValue().equals("4")));
		Assert.assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.ADD).anyMatch(i -> i.getKeyValue().equals("12")));
		Assert.assertTrue(index.stream().filter(i -> i.getAction() == IndexAction.ADD).anyMatch(i -> i.getKeyValue().equals("13")));
	}

	@Test
	public void testGenerateDiffIndexAddedFourRemovedThreeModifiedFour() {
		Collection<IndexEntry> index = getDiff("diff6");
		Assert.assertEquals(11, index.size());
		Assert.assertEquals(4, index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).count());
		Assert.assertEquals(3, index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).count());
		Assert.assertEquals(4, index.stream().filter(i -> i.getAction() == IndexAction.ADD).count());
	}

	@Test
	public void testGenerateDiffBench() {

		final int play = 25;

		int totalLargeKnew = 0;
		int totalLargeActual = 0;
		int totalIndex = 0;
		long totalDurationParallel = 0;
		long totalDurationNotParallel = 0;
		long minDurationParallel = 0;
		long maxDurationParallel = 0;
		long minDurationNotParallel = 0;
		long maxDurationNotParallel = 0;

		// Repeated run
		for (int i = 0; i < play; i++) {
			// Prepare large sources (~100k lines each, 10% differences)
			Map<String, String> largeActual = TestUtils.multiplyDataset(TestUtils.readDataset("diff6/actual.csv"), 1000);

			// Knews are modified during diff : use duplicates for double test
			Map<String, String> largeKnew1 = TestUtils.multiplyDataset(TestUtils.readDataset("diff6/knew.csv"), 1000);
			Map<String, String> largeKnew2 = TestUtils.multiplyDataset(TestUtils.readDataset("diff6/knew.csv"), 1000);

			totalLargeActual += largeActual.size();
			totalLargeKnew += largeKnew1.size();

			// Not Parallel
			this.service.applyParallelMode(false);
			long start = System.currentTimeMillis();
			totalIndex += this.service.generateDiffIndex(largeKnew1, largeActual).size();
			long delay = System.currentTimeMillis() - start;
			totalDurationNotParallel += delay;
			if (minDurationNotParallel == 0 || minDurationNotParallel > delay) {
				minDurationNotParallel = delay;
			}
			if (maxDurationNotParallel < delay) {
				maxDurationNotParallel = delay;
			}

			// parallel
			this.service.applyParallelMode(false);
			start = System.currentTimeMillis();
			totalIndex += this.service.generateDiffIndex(largeKnew2, largeActual).size();
			delay = System.currentTimeMillis() - start;
			totalDurationParallel += delay;
			if (minDurationParallel == 0 || minDurationParallel > delay) {
				minDurationParallel = delay;
			}
			if (maxDurationParallel < delay) {
				maxDurationParallel = delay;
			}
		}

		System.out.println("Processed large bench. For AVG " + totalLargeActual / play + " actual, over " + totalLargeKnew / play
				+ " knew lines, found "
				+ totalIndex / (2 * play) + " index entries. AVG Duration in parallel mode = "
				+ Double.valueOf(totalDurationParallel / play).intValue() + "ms (min = " + minDurationParallel + "/ max = "
				+ maxDurationParallel + "), in not parallel mode = "
				+ Double.valueOf(totalDurationNotParallel / play).intValue() + "ms (min = " + minDurationNotParallel + "/ max = "
				+ maxDurationNotParallel + ")");
	}

	/**
	 * @param diffName
	 * @return
	 */
	private Collection<IndexEntry> getDiff(String diffName) {
		Map<String, String> diff1Actual = TestUtils.readDataset(diffName + "/actual.csv");
		Map<String, String> diff1Knew = TestUtils.readDataset(diffName + "/knew.csv");

		// Static internal
		return this.service.generateDiffIndex(diff1Knew, diff1Actual);

	}
}
