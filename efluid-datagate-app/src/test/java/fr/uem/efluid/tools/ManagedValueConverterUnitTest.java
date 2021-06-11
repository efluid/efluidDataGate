package fr.uem.efluid.tools;

import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.stubs.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

		assertEquals(dataset.size(), 11);
		assertEquals("VALUE=S/U29tZXRoaW5n,PRESET=S/T3RoZXI=,SOMETHING=S/MTIzNDAw", dataset.get("1"));
	}

	@Test
	public void testExplodeInternalValueWithDataSet() {
		Map<String, String> dataset = TestUtils.readDataset("diff1/actual.csv", this.converter);
		Map<String, Value> values = Value.mapped(this.converter.expandInternalValue(dataset.get("1")));

		assertEquals(dataset.size(), 11);
		assertEquals("Something", values.get("VALUE").getValueAsString());
		assertEquals("Other", values.get("PRESET").getValueAsString());
		assertEquals("123400", values.get("SOMETHING").getValueAsString());
	}

	//The payload should be human readable format.
	@Test
	public void testConvertToHrPayloadCreateWithLob(){

		String activePayload = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
		String existingPayload = "";

		String hrPayload = this.converter.convertToHrPayload(activePayload, existingPayload);

		assertEquals("ACTIF:1, BINARY_VALUE:<a href=\"/ui/lob/VkZoSHdtWENPSzgrMk4yRzM1c1lwVHNjNmx0SCsrSUZ1OU9qQUxybn" +"RpQT0=\" download=\"download\">LOB</a>, CREATE_DATE:2018-01-02 23:42:34, DESCRIPTION:'Un modèle" +" de test - 3', FABRICANT:'DUPON', TYPEID:3", hrPayload);

	}

	// If activePayload is empty and existing is empty should return null
	@Test
	public void testActiveAndExistingEmpty () {
		String active = "";
		String existing = "";
		String hrPayload = this.converter.convertToHrPayload(active, existing);

		assertEquals(null, hrPayload);
	}

	//If active is empty should return existing data
	@Test
	public void testActiveIsEmpty () {
		String active = "";
		String expect = "ACTIF:1, BINARY_VALUE:<a href=" + "\"/ui/lob/VkZoSHdtWENPSzgrMk4yRzM1c1lwVHNjNmx0SCsrSUZ1OU9qQUxybnRpQT0=\"" + " download=" + "\"download\">LOB</a>, CREATE_DATE:2018-01-02 23:42:34, DESCRIPTION:'Un modèle de test - 3', FABRICANT:'DUPON', TYPEID:3";
		String existing = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
		String hrPayload = this.converter.convertToHrPayload(active, existing);

		assertEquals(expect, hrPayload);
	}

	//If existing is empty should return active data
	@Test
	public void testExistingIsEmpty () {
		String expect = "ACTIF:1, BINARY_VALUE:<a href=" + "\"/ui/lob/VkZoSHdtWENPSzgrMk4yRzM1c1lwVHNjNmx0SCsrSUZ1OU9qQUxybnRpQT0=\"" + " download=" + "\"download\">LOB</a>, CREATE_DATE:2018-01-02 23:42:34, DESCRIPTION:'Un modèle de test - 3', FABRICANT:'DUPON', TYPEID:3";
		String active = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
		String existing = "";
		String hrPayload = this.converter.convertToHrPayload(active, existing);

		assertEquals(expect, hrPayload);
	}

}
