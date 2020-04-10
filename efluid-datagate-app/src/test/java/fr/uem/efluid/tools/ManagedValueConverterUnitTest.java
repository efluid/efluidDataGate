package fr.uem.efluid.tools;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	//The payload should be human readable format.
	@Test
	public void testConvertToHrPayloadCreateWithLob(){

		String activePayload = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
		String existingPayload = "";

		String hrPayload = this.converter.convertToHrPayload(activePayload, existingPayload);

		Assert.assertEquals("ACTIF:1, BINARY_VALUE:<a href=\"/ui/lob/VkZoSHdtWENPSzgrMk4yRzM1c1lwVHNjNmx0SCsrSUZ1OU9qQUxybn" +"RpQT0=\" download=\"download\">LOB</a>, CREATE_DATE:2018-01-02 23:42:34, DESCRIPTION:'Un modèle" +" de test - 3', FABRICANT:'DUPON', TYPEID:3", hrPayload);

	}

	// If activePayload is empty and existing is empty should return null
	@Test
	public void testActiveAndExistingEmpty () {
		String active = "";
		String existing = "";
		String hrPayload = this.converter.convertToHrPayload(active, existing);

		Assert.assertEquals(null, hrPayload);
	}

	//If active is empty should return existing data
	@Test
	public void testActiveIsEmpty () {
		String active = "";
		String expect = "ACTIF:1, BINARY_VALUE:<a href=" + "\"/ui/lob/VkZoSHdtWENPSzgrMk4yRzM1c1lwVHNjNmx0SCsrSUZ1OU9qQUxybnRpQT0=\"" + " download=" + "\"download\">LOB</a>, CREATE_DATE:2018-01-02 23:42:34, DESCRIPTION:'Un modèle de test - 3', FABRICANT:'DUPON', TYPEID:3";
		String existing = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
		String hrPayload = this.converter.convertToHrPayload(active, existing);

		Assert.assertEquals(expect, hrPayload);
	}

	//If existing is empty should return active data
	@Test
	public void testExistingIsEmpty () {
		String expect = "ACTIF:1, BINARY_VALUE:<a href=" + "\"/ui/lob/VkZoSHdtWENPSzgrMk4yRzM1c1lwVHNjNmx0SCsrSUZ1OU9qQUxybnRpQT0=\"" + " download=" + "\"download\">LOB</a>, CREATE_DATE:2018-01-02 23:42:34, DESCRIPTION:'Un modèle de test - 3', FABRICANT:'DUPON', TYPEID:3";
		String active = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
		String existing = "";
		String hrPayload = this.converter.convertToHrPayload(active, existing);

		Assert.assertEquals(expect, hrPayload);
	}
	 /*
	    Test if lob contains illegal characters, lob cannot be converted
		List legal character for base64
		base={  0:'A',1:'B',2:'C',3:'D',4:'E',5:'F',6:'G',
			  7:'H',8:'I',9:'J',10:'K',11:'L',12:'M',
			  13:'N',14:'O',15:'P',16:'Q',17:'R',18:'S',
			  19:'T',20:'U',21:'V',22:'W',23:'X',24:'Y',
			  25:'Z',26:'a',27:'b',28:'c',29:'d',30:'e',
			  31:'f',32:'g',33:'h',34:'i',35:'j',36:'k',
			  37:'l',38:'m',39:'n',40:'o',41:'p',42:'q',
			  43:'r',44:'s',45:'t',46:'u',47:'v',48:'w',
			  49:'x',50:'y',51:'z',52:'0',53:'1',54:'2',
			  55:'3',56:'4',57:'5',58:'6',59:'7',60:'8',
			  61:'9',62:'+',63:'/'
			}
	*/

	 @Test
	 public void tesIllegalCharacters () {
	 	String payload = "ACTIF=O/MQ==,BINARY_VALUE=B/VFhHwmXCOK8+2N2G35sYpTsc6ltH++IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA==,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";
	 	String illegal = "ACTIF=O/MQ==,BINARY_VALUE=B§§VFhHwmXCOK8+2N2G3`````````````5sYpTsc6ltH%%IFu9OjALrntiA=,CREATE_DATE=T/MjAxOC0wMS0wMiAyMzo0MjozNA**,DESCRIPTION=S/VW4gbW9kw6hsZSBkZSB0ZXN0IC0gMw==,FABRICANT=S/RFVQT04=,TYPEID=O/Mw==";

	 	Boolean payloadHasLegalCharacter = this.converter.checkPayloadBeforeConverting(payload);
	 	Boolean payloadHasIllegalCharacter =  this.converter.checkPayloadBeforeConverting(illegal);

	 	String regex = "[a-zA-Z0-9/+=]+$";
		Pattern pattern = Pattern.compile(regex);
		Matcher m = pattern.matcher(payload);

		System.out.println(m.find());
		//Assert.assertEquals(false, payloadHasLegalCharacter);
	 	//Assert.assertEquals(true, payloadHasIllegalCharacter);
	 }

}
