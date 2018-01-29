package fr.uem.efluid.utils;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.uem.efluid.TestUtils;
import fr.uem.efluid.model.Value;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ManagedDiffUtilsTest {

	@Test
	public void testAppendExtractedValueWithDataSet() {
		Map<String, String> dataset = TestUtils.readDataset("diff1/actual.csv");

		Assert.assertEquals(dataset.size(), 11);
		Assert.assertEquals("Value=S/U29tZXRoaW5n,Preset=S/T3RoZXI=,Something=S/MTIzNDAw", dataset.get("1"));
	}

	@Test
	public void testExplodeInternalValueWithDataSet() {
		Map<String, String> dataset = TestUtils.readDataset("diff1/actual.csv");
		Map<String, Value> values = Value.mapped(ManagedDiffUtils.expandInternalValue(dataset.get("1")));

		Assert.assertEquals(dataset.size(), 11);
		Assert.assertEquals("Something", values.get("Value").getValueAsString());
		Assert.assertEquals("Other", values.get("Preset").getValueAsString());
		Assert.assertEquals("123400", values.get("Something").getValueAsString());
	}
}
