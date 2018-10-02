package fr.uem.efluid.system.stubs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.system.stubs.entities.SimulatedTableOne;
import fr.uem.efluid.system.stubs.entities.SimulatedTableTwo;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableOneRepository;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableTwoRepository;

/**
 * <p>
 * Component for init and use of the <tt>managed</tt> database (the configured source
 * database, simulated with stubs entity model)
 * </p>
 * 
 * @author elecomte
 * @since v0.0.8
 * @version 1
 */
@Component
public class ManagedDatabaseAccess {

	public static final String TABLE_ONE = "TTAB_ONE";
	public static final String TABLE_TWO = "TTAB_TWO";
	public static final String TABLE_THREE = "TTAB_THREE";

	@Autowired
	private SimulatedTableOneRepository tabOne;

	@Autowired
	private SimulatedTableTwoRepository tabTwo;

	/**
	 * @param nbr
	 * @param keyPattern
	 * @param valuePattern
	 * @param otherPattern
	 */
	public void initTabTwoData(int nbr, String keyPattern, String valuePattern, String otherPattern) {

		for (int i = 0; i < nbr; i++) {
			this.tabTwo.save(two(keyPattern + i, valuePattern + i, otherPattern + i));
		}
	}

	/**
	 * @param nbr
	 * @param presetPattern
	 * @param somethingPattern
	 * @param valuePattern
	 */
	public void initTabOneData(int nbr, String presetPattern, String somethingPattern, String valuePattern) {

		for (int i = 0; i < nbr; i++) {
			this.tabOne.save(one(i, presetPattern + i, somethingPattern + i, valuePattern + i));
		}
	}

	private static SimulatedTableOne one(int key, String preset, String something, String value) {

		SimulatedTableOne data = new SimulatedTableOne();
		data.setKey(Long.valueOf(key));
		data.setPreset(preset);
		data.setSomething(something);
		data.setValue(value);

		return data;
	}

	private static SimulatedTableTwo two(String key, String value, String other) {

		SimulatedTableTwo data = new SimulatedTableTwo();
		data.setKey(key);
		data.setValue(value);
		data.setOther(other);

		return data;
	}
}
