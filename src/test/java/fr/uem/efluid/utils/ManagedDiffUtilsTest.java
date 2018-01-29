package fr.uem.efluid.utils;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.uem.efluid.TestUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class ManagedDiffUtilsTest {

	@Test
	public void testAppendExtractedValueWithDataSet(){
		Map<String,String> dataset = TestUtils.readDataset("diff1/actual.csv");
		
		Assert.assertEquals(dataset.size(),11 );
		Assert.assertEquals("Value=S/[B@65e579dc,Preset=S/[B@61baa894,Something=S/[B@b065c63", dataset.get("1"));
	}
	

	@Test
	public void testExplodeInternalValueWithDataSet(){
		Map<String,String> dataset = TestUtils.readDataset("diff1/actual.csv");
		
		Assert.assertEquals(dataset.size(),11 );
		Assert.assertEquals("Something", ManagedDiffUtils.explodeInternalValue(dataset.get("1")).get("Value"));
		Assert.assertEquals("Other", ManagedDiffUtils.explodeInternalValue(dataset.get("1")).get("Preset"));
		Assert.assertEquals("123400", ManagedDiffUtils.explodeInternalValue(dataset.get("1")).get("Something"));
	}
}
