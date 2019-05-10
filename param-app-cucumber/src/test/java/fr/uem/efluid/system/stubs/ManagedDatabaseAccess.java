package fr.uem.efluid.system.stubs;

import cucumber.api.DataTable;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.system.stubs.entities.SimulatedTableFour;
import fr.uem.efluid.system.stubs.entities.SimulatedTableOne;
import fr.uem.efluid.system.stubs.entities.SimulatedTableThree;
import fr.uem.efluid.system.stubs.entities.SimulatedTableTwo;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableFourRepository;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableOneRepository;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableThreeRepository;
import fr.uem.efluid.system.stubs.repositories.SimulatedTableTwoRepository;
import fr.uem.efluid.utils.FormatUtils;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;

/**
 * <p>
 * Component for init and use of the <tt>managed</tt> database (the configured source
 * database, simulated with stubs entity model)
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
@Component
public class ManagedDatabaseAccess {

    public static final String TABLE_ONE = "TTAB_ONE";
    public static final String TABLE_TWO = "TTAB_TWO";
    public static final String TABLE_THREE = "TTAB_THREE";
    public static final String TABLE_FOUR = "TTAB_FOUR";

    @Autowired
    private SimulatedTableOneRepository tabOne;

    @Autowired
    private SimulatedTableTwoRepository tabTwo;

    @Autowired
    private SimulatedTableThreeRepository tabThree;

    @Autowired
    private SimulatedTableFourRepository tabFour;

    /**
     * Init from cucumber Datatable
     *
     * @param name
     * @param data
     */
    public void initTab(String name, DataTable data) {

        List<Map<String, String>> values = data.asMaps(String.class, String.class);

        switch (name) {
            case TABLE_ONE:
                values.forEach(m ->
                        this.tabOne.save(one(Integer.parseInt(m.get("key")), m.get("preset"), m.get("something"), m.get("value")))
                );
                break;
            case TABLE_TWO:
                values.forEach(m ->
                        this.tabTwo.save(two(m.get("key"), m.get("value"), m.get("other")))
                );
                break;
            case TABLE_THREE:

                values.forEach(m ->
                        this.tabThree.save(three(m.get("key"), m.get("value"), m.get("other")))
                );
                break;
            case TABLE_FOUR:

                values.forEach(m ->
                        this.tabFour.save(four(m.get("key"), Long.valueOf(m.get("otherTable")), FormatUtils.parse(m.get("contentTime")), Integer.valueOf(m.get("contentInt"))))
                );
                break;
            default:
                throw new AssertionError("Unknown table name " + name);
        }

    }

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
        data.setKey((long) key);
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

    private static SimulatedTableFour four(String key, Long otherId, LocalDateTime time, int content) {

        SimulatedTableFour data = new SimulatedTableFour();
        data.setKey(key);
        data.setOtherTable(new SimulatedTableOne(otherId));
        data.setContentTime(time);
        data.setContentInt(content);

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
    public List<String> getColumnNamesForTable(String tableName) {

        if (tableName.equals(TABLE_ONE))
            return Arrays.asList("KEY", "VALUE", "PRESET", "SOMETHING");

        if (tableName.equals(TABLE_TWO))
            return Arrays.asList("KEY", "VALUE", "OTHER");

        if (tableName.equals(TABLE_FOUR))
            return Arrays.asList("KEY", "SIMULATEDTABLEONE_KEY", "CONTENT_TIME", "CONTENT_INT");

        return Arrays.asList("KEY", "VALUE", "PRESET", "SOMETHING");
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAllEntitiesForTable(String tableName) {

        if (tableName.equals(TABLE_ONE))
            return (List<T>) this.tabOne.findAll();

        if (tableName.equals(TABLE_TWO))
            return (List<T>) this.tabTwo.findAll();

        if (tableName.equals(TABLE_FOUR))
            return (List<T>) this.tabFour.findAll();

        return (List<T>) this.tabThree.findAll();
    }

    /**
     * @return
     */
    public long countTable(String tableName) {

        if (tableName.equals(TABLE_ONE))
            return this.tabOne.count();

        if (tableName.equals(TABLE_TWO))
            return this.tabTwo.count();

        if (tableName.equals(TABLE_FOUR))
            return this.tabFour.count();

        return this.tabThree.count();
    }

    /**
     * <p>
     * Get easy to use content for Tab 1
     * </p>
     *
     * @return
     */
    public List<Map<String, String>> getAllContentForTable(String tableName) {

        List<String> cols = getColumnNamesForTable(tableName);

        return getAllEntitiesForTable(tableName).stream().map(i -> {
            Map<String, String> vals = new HashMap<>();
            cols.forEach(c -> vals.put(c, getPropertyValueByColumnName(i, c)));
            return vals;
        })
                .sorted(Comparator.comparing(m -> m.get("KEY")))
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * Matcher spec for the columns for TABLE_ONE. Allows to check Column / ColumnEditData
     * content in an assertion
     * </p>
     *
     * @return
     */
    public List<Matcher<DictionaryEntryEditData.ColumnEditData>> getColumnMatchersForTableOne() {
        return Arrays.asList(
                columnMatcher("KEY", ColumnType.PK_ATOMIC),
                columnMatcher("VALUE", ColumnType.STRING),
                columnMatcher("PRESET", ColumnType.STRING),
                columnMatcher("SOMETHING", ColumnType.STRING));
    }

    /**
     * <p>
     * Matcher spec for the columns for TABLE_TWO. Allows to check Column / ColumnEditData
     * content in an assertion
     * </p>
     *
     * @return
     */
    public List<Matcher<DictionaryEntryEditData.ColumnEditData>> getColumnMatchersForTableTwo() {
        return Arrays.asList(
                columnMatcher("KEY", ColumnType.PK_STRING),
                columnMatcher("VALUE", ColumnType.STRING),
                columnMatcher("OTHER", ColumnType.STRING));
    }

    /**
     * <p>
     * Matcher spec for the columns for TABLE_THREE. Allows to check Column /
     * ColumnEditData content in an assertion
     * </p>
     *
     * @return
     */
    public List<Matcher<DictionaryEntryEditData.ColumnEditData>> getColumnMatchersForTableThree() {
        return Arrays.asList(
                columnMatcher("KEY", ColumnType.PK_STRING),
                columnMatcher("VALUE", ColumnType.STRING),
                columnMatcher("OTHER", ColumnType.STRING));
    }

    /**
     * @param rawObject
     * @param propertyAsColumnName
     * @return
     */
    private static String getPropertyValueByColumnName(Object rawObject, String propertyAsColumnName) {
        try {
            Method getter = rawObject.getClass().getMethod(
                    "get" + propertyAsColumnName.substring(0, 1).toUpperCase() + propertyAsColumnName.substring(1).toLowerCase());

            Object res = getter.invoke(rawObject);

            if (res instanceof LocalDateTime) {
                return FormatUtils.format((LocalDateTime) res);
            }

            return String.valueOf(res);
        } catch (NoSuchMethodException | SecurityException | IllegalArgumentException | IllegalAccessException
                | InvocationTargetException e) {
            throw new AssertionError("Cannot get getter for " + propertyAsColumnName + " into object of type " + rawObject);
        }
    }

    /**
     * @param name
     * @param type
     * @return
     */
    private static Matcher<DictionaryEntryEditData.ColumnEditData> columnMatcher(String name, ColumnType type) {
        return allOf(hasProperty("name", equalTo(name)), hasProperty("type", equalTo(type)));
    }
}
