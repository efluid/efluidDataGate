package fr.uem.efluid.system.stubs;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.system.stubs.entities.SimulatedTableOne;
import fr.uem.efluid.system.stubs.entities.SimulatedTableThree;
import fr.uem.efluid.system.stubs.entities.SimulatedTableTwo;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableOneRepository;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableThreeRepository;
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

	@Autowired
	private SimulatedTableThreeRepository tabThree;

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

	/**
	 * @param nbr
	 * @param keyPattern
	 * @param valuePattern
	 * @param otherPattern
	 */
	public void initTabThreeData(int nbr, String keyPattern, String valuePattern, String otherPattern) {

		for (int i = 0; i < nbr; i++) {
			this.tabThree.save(three(keyPattern + i, valuePattern + i, otherPattern + i));
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

	private static SimulatedTableThree three(String key, String value, String other) {

		SimulatedTableThree data = new SimulatedTableThree();
		data.setKey(key);
		data.setValue(value);
		data.setOther(other);

		return data;
	}

	/**
	 * <p>
	 * Matcher spec for the columns for TABLE_ONE. Allows to check Column / ColumnEditData
	 * content in an assertion
	 * </p>
	 * 
	 * @return
	 */
	public List<Matcher<?>> getColumnMatchersForTableOne() {
		return Arrays.asList(
				columnMatcher("key", ColumnType.PK_ATOMIC),
				columnMatcher("value", ColumnType.STRING),
				columnMatcher("preset", ColumnType.STRING),
				columnMatcher("something", ColumnType.STRING));
	}

	/**
	 * <p>
	 * Matcher spec for the columns for TABLE_TWO. Allows to check Column / ColumnEditData
	 * content in an assertion
	 * </p>
	 * 
	 * @return
	 */
	public List<Matcher<?>> getColumnMatchersForTableTwo() {
		return Arrays.asList(
				columnMatcher("key", ColumnType.PK_STRING),
				columnMatcher("value", ColumnType.STRING),
				columnMatcher("other", ColumnType.STRING));
	}

	/**
	 * @param name
	 * @param type
	 * @return
	 */
	private static Matcher<?> columnMatcher(String name, ColumnType type) {
		return allOf(hasProperty("name", equalTo(name)), hasProperty("type", equalTo(type)));
	}
}
