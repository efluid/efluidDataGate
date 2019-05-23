package fr.uem.efluid.system.stubs;

import cucumber.api.DataTable;
import fr.uem.efluid.ColumnType;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.system.stubs.entities.*;
import fr.uem.efluid.system.stubs.repositories.*;
import fr.uem.efluid.utils.FormatUtils;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
@Transactional
public class ManagedDatabaseAccess {

    public static final String TABLE_ONE = "TTAB_ONE";
    public static final String TABLE_TWO = "TTAB_TWO";
    public static final String TABLE_THREE = "TTAB_THREE";
    public static final String TABLE_FOUR = "TTAB_FOUR";
    public static final String TABLE_FIVE = "TTAB_FIVE";
    public static final String TABLE_SIX = "TTAB_SIX";

    @Autowired
    private SimulatedTableOneRepository tabOne;

    @Autowired
    private SimulatedTableTwoRepository tabTwo;

    @Autowired
    private SimulatedTableThreeRepository tabThree;

    @Autowired
    private SimulatedTableFourRepository tabFour;

    @Autowired
    private SimulatedTableFiveRepository tabFive;

    @Autowired
    private SimulatedTableSixRepository tabSix;

    @Autowired
    private EntityManager em;

    public void dropManaged() {
        this.tabSix.deleteAll();
        this.tabFive.deleteAll();
        this.tabFour.deleteAll();
        this.tabThree.deleteAll();
        this.tabTwo.deleteAll();
        this.tabOne.deleteAll();
        this.em.flush();
    }

    /**
     * Init from cucumber Datatable
     *
     * @param name
     * @param data
     */
    public void updateTab(String name, DataTable data) {

        this.tabOne.findAll();

        List<Map<String, String>> values = data.asMaps(String.class, String.class);

        switch (name) {
            case TABLE_ONE:
                values.forEach(m -> updateOne(m.get("change"), m));
                break;
            case TABLE_TWO:
                values.forEach(m -> updateTwo(m.get("change"), m));
                break;
            case TABLE_THREE:
                values.forEach(m -> updateThree(m.get("change"), m));
                break;
            case TABLE_FOUR:
                values.forEach(m -> updateFour(m.get("change"), m));
                break;
            case TABLE_FIVE:
                values.forEach(m -> updateFive(m.get("change"), m));
                break;
            case TABLE_SIX:
                values.forEach(m -> updateSix(m.get("change"), m));
                break;
            default:
                throw new AssertionError("Unknown table name " + name);
        }
        this.em.flush();
    }

    /**
     * Init from cucumber Datatable
     *
     * @param name
     * @param data
     */
    public void assertCurrentTabComplies(String name, DataTable data) {

        List<Map<String, String>> values = data.asMaps(String.class, String.class);

        switch (name) {
            case TABLE_ONE:
                assertOneComplies(values);
                break;
            case TABLE_TWO:
                assertTwoComplies(values);
                break;
            case TABLE_THREE:
                assertThreeComplies(values);
                break;
            case TABLE_FOUR:
                assertFourComplies(values);
                break;
            case TABLE_FIVE:
                assertFiveComplies(values);
                break;
            case TABLE_SIX:
                assertSixComplies(values);
                break;
            default:
                throw new AssertionError("Unknown table name " + name);
        }
    }

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
                values.forEach(m -> updateOne("add", m));
                break;
            case TABLE_TWO:
                values.forEach(m -> updateTwo("add", m));
                break;
            case TABLE_THREE:
                values.forEach(m -> updateThree("add", m));
                break;
            case TABLE_FOUR:
                values.forEach(m -> updateFour("add", m));
                break;
            case TABLE_FIVE:
                values.forEach(m -> updateFive("add", m));
                break;
            case TABLE_SIX:
                values.forEach(m -> updateSix("add", m));
                break;
            default:
                throw new AssertionError("Unknown table name " + name);
        }
        this.em.flush();
    }

    private void updateOne(String action, Map<String, String> m) {

        switch (action) {
            case "add":
                this.tabOne.save(one(Integer.parseInt(m.get("key")), m.get("preset"), m.get("something"), m.get("value")));
                break;
            case "update":
                SimulatedTableOne one = this.tabOne.getOne(Long.parseLong(m.get("key")));
                one.setPreset(m.get("preset"));
                one.setSomething(m.get("something"));
                one.setValue(m.get("value"));
                this.tabOne.save(one);
                break;
            case "delete":
            default:
                this.tabOne.delete(this.tabOne.getOne(Long.parseLong(m.get("key"))));
                break;
        }
    }

    private void assertOneComplies(List<Map<String, String>> values) {
        assertThat(this.tabOne.count()).isEqualTo(values.size());

        values.forEach(m -> {
            SimulatedTableOne expected = one(Integer.parseInt(m.get("key")), m.get("preset"), m.get("something"), m.get("value"));
            SimulatedTableOne found = this.tabOne.getOne(expected.getKey());
            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(expected);
        });
    }


    private void updateTwo(String action, Map<String, String> m) {

        switch (action) {
            case "add":
                this.tabTwo.save(two(m.get("key"), m.get("value"), m.get("other")));
                break;
            case "update":
                SimulatedTableTwo two = this.tabTwo.getOne(m.get("key"));
                two.setOther(m.get("other"));
                two.setValue(m.get("value"));
                this.tabTwo.save(two);
                break;
            case "delete":
            default:
                this.tabTwo.delete(this.tabTwo.getOne(m.get("key")));
                break;
        }
    }

    private void assertTwoComplies(List<Map<String, String>> values) {
        assertThat(this.tabTwo.count()).isEqualTo(values.size());

        values.forEach(m -> {
            SimulatedTableTwo expected = two(m.get("key"), m.get("value"), m.get("other"));
            SimulatedTableTwo found = this.tabTwo.getOne(expected.getKey());
            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(expected);
        });
    }

    private void updateThree(String action, Map<String, String> m) {

        switch (action) {
            case "add":
                this.tabThree.save(three(m.get("key"), m.get("value"), m.get("other")));
                break;
            case "update":
                SimulatedTableThree three = this.tabThree.getOne(m.get("key"));
                three.setOther(m.get("other"));
                three.setValue(m.get("value"));
                this.tabThree.save(three);
                break;
            case "delete":
            default:
                this.tabThree.delete(this.tabThree.getOne(m.get("key")));
                break;
        }
    }

    private void assertThreeComplies(List<Map<String, String>> values) {
        assertThat(this.tabThree.count()).isEqualTo(values.size());

        values.forEach(m -> {
            SimulatedTableThree expected = three(m.get("key"), m.get("value"), m.get("other"));
            SimulatedTableThree found = this.tabThree.getOne(expected.getKey());
            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(expected);
        });
    }

    private void updateFour(String action, Map<String, String> m) {

        switch (action) {
            case "add":
                this.tabFour.save(four(m.get("key"), Long.valueOf(m.get("otherTable")), FormatUtils.parse(m.get("contentTime")), Integer.valueOf(m.get("contentInt"))));
                break;
            case "update":
                SimulatedTableFour four = this.tabFour.getOne(m.get("key"));
                four.setOtherTable(new SimulatedTableOne(Long.valueOf(m.get("otherTable"))));
                four.setContentInt(Integer.valueOf(m.get("contentInt")));
                four.setContentTime(FormatUtils.parse(m.get("contentTime")));
                this.tabFour.save(four);
                break;
            case "delete":
            default:
                this.tabFour.delete(this.tabFour.getOne(m.get("key")));
                break;
        }
    }

    private void assertFourComplies(List<Map<String, String>> values) {
        assertThat(this.tabFour.count()).isEqualTo(values.size());

        values.forEach(m -> {
            SimulatedTableFour expected = four(m.get("key"), Long.valueOf(m.get("otherTable")), FormatUtils.parse(m.get("contentTime")), Integer.valueOf(m.get("contentInt")));
            SimulatedTableFour found = this.tabFour.getOne(expected.getKey());
            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(expected);
        });
    }

    private void updateFive(String action, Map<String, String> m) {

        switch (action) {
            case "add":
                this.tabFive.save(five(m.get("key"), FormatUtils.toBytes(m.get("data")), new BigDecimal(m.get("simple"))));
                break;
            case "update":
                SimulatedTableFive five = this.tabFive.getOne(m.get("key"));
                five.setData(FormatUtils.toBytes(m.get("data")));
                five.setSimple(new BigDecimal(m.get("simple")));
                this.tabFive.save(five);
                break;
            case "delete":
            default:
                this.tabFive.delete(this.tabFive.getOne(m.get("key")));
                break;
        }
    }

    private void assertFiveComplies(List<Map<String, String>> values) {
        assertThat(this.tabFive.count()).isEqualTo(values.size());

        values.forEach(m -> {
            SimulatedTableFive expected = five(m.get("key"), FormatUtils.toBytes(m.get("data")), new BigDecimal(m.get("simple")));
            SimulatedTableFive found = this.tabFive.getOne(expected.getKey());
            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(expected);
        });
    }

    private void updateSix(String action, Map<String, String> m) {

        switch (action) {
            case "add":
                this.tabSix.save(six(Long.valueOf(m.get("identifier")), m.get("text"), FormatUtils.parseLd(m.get("date"))));
                break;
            case "update":
                SimulatedTableSix six = this.tabSix.getOne(Long.valueOf(m.get("identifier")));
                six.setText(m.get("text"));
                six.setDate(FormatUtils.parseLd(m.get("date")));
                this.tabSix.save(six);
                break;
            case "delete":
            default:
                this.tabSix.delete(this.tabSix.getOne(Long.valueOf(m.get("identifier"))));
                break;
        }
    }

    private void assertSixComplies(List<Map<String, String>> values) {
        assertThat(this.tabSix.count()).isEqualTo(values.size());

        values.forEach(m -> {
            SimulatedTableSix expected = six(Long.valueOf(m.get("identifier")), m.get("text"), FormatUtils.parseLd(m.get("date")));
            SimulatedTableSix found = this.tabSix.getOne(Long.valueOf(m.get("identifier")));
            assertThat(found).isNotNull();
            if (!found.equals(expected)) {
                throw new AssertionError("Differences found for table " + TABLE_SIX + " on id " + found.getIdentifier() +
                        ". Found values : text=\"" + found.getText() + "\" (expected=\"" + expected.getText() + "\"), " +
                        "date=" + found.getDate() + " (expected=" + expected.getDate() + ")");
            }
        });
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

    private static SimulatedTableFive five(String key, byte[] clob, BigDecimal content) {

        SimulatedTableFive data = new SimulatedTableFive();
        data.setKey(key);
        data.setData(clob);
        data.setSimple(content);

        return data;
    }

    private static SimulatedTableSix six(Long id, String text, LocalDate date) {

        SimulatedTableSix data = new SimulatedTableSix();
        data.setIdentifier(id);
        data.setText(text);
        data.setDate(date);

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

        if (tableName.equals(TABLE_FIVE))
            return Arrays.asList("KEY", "DATA", "SIMPLE");

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

        if (tableName.equals(TABLE_FIVE))
            return (List<T>) this.tabFive.findAll();

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

        if (tableName.equals(TABLE_FIVE))
            return this.tabFive.count();

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
