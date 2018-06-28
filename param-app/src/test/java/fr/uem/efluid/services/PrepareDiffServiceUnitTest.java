package fr.uem.efluid.services;

import java.util.Collection;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.model.entities.IndexAction;
import fr.uem.efluid.model.entities.Project;
import fr.uem.efluid.services.types.PreparedIndexEntry;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.tools.ManagedValueConverter;
import fr.uem.efluid.utils.DataGenerationUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class PrepareDiffServiceUnitTest {

	// No spring here neither
	final ManagedValueConverter converter = new ManagedValueConverter();

	// No spring load here, for simple diff needs
	private PrepareIndexService service = new PrepareIndexService() {

		/**
		 * @return
		 * @see fr.uem.efluid.services.PrepareIndexService#getConverter()
		 */
		@Override
		protected ManagedValueConverter getConverter() {
			return PrepareDiffServiceUnitTest.this.converter;
		}

	};

	@Test
	public void testGenerateDiffIndexDiffNotChanged() {
		Collection<PreparedIndexEntry> index = getDiff("diff1");
		Assert.assertEquals(0, index.size());
	}

	@Test
	public void testGenerateDiffIndexAddedTwo() {
		Collection<PreparedIndexEntry> index = getDiff("diff2");
		Assert.assertEquals(2, index.size());
		Assert.assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.ADD));
		Assert.assertTrue(index.stream()
				.allMatch(i -> Value.mapped(this.converter.expandInternalValue(i.getPayload())).get("VALUE").getValueAsString()
						.equals("Different")));
	}

	@Test
	public void testGenerateDiffIndexRemovedTwo() {
		Collection<PreparedIndexEntry> index = getDiff("diff3");
		Assert.assertEquals(2, index.size());
		Assert.assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.REMOVE));
		Assert.assertTrue(index.stream().allMatch(i -> i.getPayload() == null));
	}

	@Test
	public void testGenerateDiffIndexModifiedTwo() {
		Collection<PreparedIndexEntry> index = getDiff("diff4");
		Assert.assertEquals(2, index.size());
		Assert.assertTrue(index.stream().allMatch(i -> i.getAction() == IndexAction.UPDATE));
		Assert.assertTrue(index.stream().anyMatch(i -> i.getKeyValue().equals("5")));
		Assert.assertTrue(index.stream().anyMatch(i -> i.getKeyValue().equals("8")));
		Assert.assertTrue(index.stream()
				.allMatch(i -> Value.mapped(this.converter.expandInternalValue(i.getPayload())).get("VALUE").getValueAsString()
						.equals("Modified")));
	}

	@Test
	public void testGenerateDiffIndexAddedTwoRemovedTwoModifiedTwo() {
		Collection<PreparedIndexEntry> index = getDiff("diff5");
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
		Collection<PreparedIndexEntry> index = getDiff("diff6");
		Assert.assertEquals(11, index.size());
		Assert.assertEquals(4, index.stream().filter(i -> i.getAction() == IndexAction.UPDATE).count());
		Assert.assertEquals(3, index.stream().filter(i -> i.getAction() == IndexAction.REMOVE).count());
		Assert.assertEquals(4, index.stream().filter(i -> i.getAction() == IndexAction.ADD).count());
	}

	/**
	 * @param diffName
	 * @return
	 */
	private Collection<PreparedIndexEntry> getDiff(String diffName) {
		Map<String, String> diff1Actual = TestUtils.readDataset(diffName + "/actual.csv", this.converter);
		Map<String, String> diff1Knew = TestUtils.readDataset(diffName + "/knew.csv", this.converter);

		Project proj = DataGenerationUtils.project("mock");
		// Static internal
		return this.service.generateDiffIndexFromContent(PreparedIndexEntry::new, diff1Knew, diff1Actual,
				DataGenerationUtils.entry("mock", DataGenerationUtils.domain("mock", proj), "s*", "table", "1=1", "key", ColumnType.STRING));

	}
}
