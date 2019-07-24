package fr.uem.efluid.tools;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.stubs.TestUtils;
import fr.uem.efluid.tools.ManagedValueConverter;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ManagedValueConverterUnitTest {

	private ManagedValueConverter converter = new ManagedValueConverter();

	@Test
	public void testAppendExtractedValueWithDataSet() {
		Map<String, String> dataset = TestUtils.readDataset("diff1/actual.csv", this.converter);

		Assert.assertEquals(dataset.size(), 11);
		Assert.assertEquals("VALUE=S/U29tZXRoaW5n,PRESET=S/T3RoZXI=,SOMETHING=S/MTIzNDAw", dataset.get("1"));
	}

	@Test
	public void testExplodeInternalValueWithDataSet() {
		Map<String, String> dataset = TestUtils.readDataset("diff1/actual.csv", this.converter);
		Map<String, Value> values = Value.mapped(this.converter.expandInternalValue(dataset.get("1")));

		Assert.assertEquals(dataset.size(), 11);
		Assert.assertEquals("Something", values.get("VALUE").getValueAsString());
		Assert.assertEquals("Other", values.get("PRESET").getValueAsString());
		Assert.assertEquals("123400", values.get("SOMETHING").getValueAsString());
	}
}
