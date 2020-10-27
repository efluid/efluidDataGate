package fr.uem.efluid.cucumber.stubs;

import fr.uem.efluid.ColumnType;
import fr.uem.efluid.cucumber.stubs.entities.*;
import fr.uem.efluid.services.types.DictionaryEntryEditData;
import fr.uem.efluid.utils.FormatUtils;
import io.cucumber.datatable.DataTable;
import org.hamcrest.Matcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * <p>
 * Component for init and use of the <tt>managed</tt> database (the configured source
 * database, simulated with stubs entity model)
 * </p>
 *
 * @author elecomte
 * @version 2
 * @since v0.0.8
 */
@Component
@Transactional
public class ManagedDatabaseAccess {

    public static final String TABLE_ONE = "TTAB_ONE"; // Basic table, key is value column
    public static final String TABLE_TWO = "TTAB_TWO"; // Basic table, key is native pk
    public static final String TABLE_THREE = "TTAB_THREE"; // Basic table, simplest version
    public static final String TABLE_FOUR = "TTAB_FOUR"; // Table with native join on tab ONE
    public static final String TABLE_FIVE = "TTAB_FIVE"; // Table with BLOB
    public static final String TABLE_SIX = "TTAB_SIX"; // Table with CLOB
    public static final String TABLE_SEVEN = "TTAB_SEVEN"; // Table with business join on tab THREE
    public static final String TABLE_ONLY_KEYS = "TTAB_ONLY_KEYS"; // Table with only keys

    public static final String TTEST1 = "TTEST1";
    public static final String TTESTMULTIDATATYPE = "TTESTMULTIDATATYPE";
    public static final String EFLUIDTESTPKCOMPOSITE = "EFLUIDTESTPKCOMPOSITE";
    public static final String EFLUIDTESTNUMBER = "EFLUIDTESTNUMBER";
    public static final String EFLUIDTESTAUDIT = "T_EFLUID_TEST_AUDIT";

    public static final String TTESTNULLLINK_SRC = "T_NULL_LINK_DEMO_SRC";
    public static final String TTESTNULLLINK_DEST = "T_NULL_LINK_DEMO_DEST";

    // Table to pair of "order" / type
    private static final Map<String, Pair<Integer, Class<?>>> ENTITY_TYPES = new HashMap<>();

    static {
        ENTITY_TYPES.put(TABLE_ONE, Pair.of(1, SimulatedTableOne.class));
        ENTITY_TYPES.put(TABLE_TWO, Pair.of(2, SimulatedTableTwo.class));
        ENTITY_TYPES.put(TABLE_THREE, Pair.of(3, SimulatedTableThree.class));
        ENTITY_TYPES.put(TABLE_FOUR, Pair.of(4, SimulatedTableFour.class));
        ENTITY_TYPES.put(TABLE_FIVE, Pair.of(5, SimulatedTableFive.class));
        ENTITY_TYPES.put(TABLE_SIX, Pair.of(6, SimulatedTableSix.class));
        ENTITY_TYPES.put(TABLE_SEVEN, Pair.of(7, SimulatedTableSeven.class));
        ENTITY_TYPES.put(TTEST1, Pair.of(8, EfluidTest1.class));
        ENTITY_TYPES.put(EFLUIDTESTNUMBER, Pair.of(9, EfluidTestNumber.class));
        ENTITY_TYPES.put(TTESTMULTIDATATYPE, Pair.of(10, EfluidTestMultiDataType.class));
        ENTITY_TYPES.put(EFLUIDTESTPKCOMPOSITE, Pair.of(11, EfluidTestPkComposite.class));
        ENTITY_TYPES.put(TABLE_ONLY_KEYS, Pair.of(12, SimulatedTableOnlyKeys.class));
        ENTITY_TYPES.put(TTESTNULLLINK_SRC, Pair.of(13, EfluidTestNullableLinkSource.class));
        ENTITY_TYPES.put(TTESTNULLLINK_DEST, Pair.of(14, EfluidTestNullableLinkDestination.class));
        ENTITY_TYPES.put(EFLUIDTESTAUDIT, Pair.of(15, EfluidTestAudit.class));
    }

    @Autowired
    private EntityManager em;

    public void dropManaged() {
        Comparator<Pair<Integer, Class<?>>> comp = reverseOrder(comparing(Pair::getFirst));

        // Delete all by reverse order
        ENTITY_TYPES.values().stream()
                .sorted(comp)
                .forEach(
                        p -> this.deleteAll(p.getSecond())
                );

        this.em.flush();
    }


    /**
     * Update from cucumber Datatable
     *
     * @param name table name
     * @param data source for data from feature code
     */
    public void updateTab(String name, DataTable data) {

        List<Map<String, String>> values = data.asMaps();

        // 1st After "change" (so 2nd col)
        String firstCol = data.cells().get(0).get(1);

        Class<?> type = ENTITY_TYPES.get(name).getSecond();
        values.forEach(m -> update(m.get("change"), firstCol, m, type));

        this.em.flush();
    }

    /**
     * Check from cucumber Datatable that current data are equals
     *
     * @param name table name
     * @param data source for data from feature code
     */
    public void assertCurrentTabComplies(String name, DataTable data) {

        List<Map<String, String>> values = data.asMaps(String.class, String.class);

        // 1st col
        String firstCol = data.cells().get(0).get(0);

        Class<?> type = ENTITY_TYPES.get(name).getSecond();
        assertComplies(firstCol, values, type);
    }


    private <T> void save(T item) {
        this.em.merge(item);
    }

    private <K, T> T getOne(K key, Class<T> type) {
        return this.em.find(type, key);
    }

    private <T> void deleteOne(T item) {
        this.em.remove(item);
    }

    private <T> void deleteAll(Class<T> type) {
        this.em.createQuery("delete from " + type.getName()).executeUpdate();
    }

    private <T> List<T> findAll(Class<T> type) {
        return this.em.createQuery("select t from " + type.getName() + " t", type).getResultList();
    }

    /**
     * Init from cucumber Datatable
     *
     * @param name table name
     * @param data source for data from feature code
     */
    public void initTab(String name, DataTable data) {

        List<Map<String, String>> values = data.asMaps(String.class, String.class);

        // 1st cel
        String firstCol = data.cells().get(0).get(0);

        Class<?> type = ENTITY_TYPES.get(name).getSecond();
        values.forEach(m -> update("add", firstCol, m, type));

        this.em.flush();
    }


    /**
     * Init from cucumber Datatable with a repeated pattern
     *
     * @param count repeated line count
     * @param name  table name
     * @param data  source for data from feature code, for one entry, repeated <tt>count</tt> times
     */
    public void initHeavyTab(int count, String name, DataTable data) {

        List<Map<String, String>> values = data.asMaps(String.class, String.class);
        Class<?> type = ENTITY_TYPES.get(name).getSecond();
        values.forEach(m -> initHeavy(count, m, type));

        this.em.flush();
    }


    private <T> T load(Class<T> type, Map<String, String> m) {

        try {
            // Default constructor call
            T entity = type.getConstructor().newInstance();

            // Search setters
            Map<String, Method> setters = loadSetters(type);

            // apply all setters
            m.forEach((k, v) -> {
                // Ignore change
                if (!k.equals("change")) {
                    Method setter = setters.get(k);
                    if (setter == null) {
                        throw new UnsupportedOperationException("The column " + k
                                + " has no corresponding setter in type " + type
                                + ": add it as exclude type in fr.uem.efluid.cucumber.stubs." +
                                "ManagedDatabaseAccess.load or check datatable");
                    }

                    Object val = getValForSetter(setter, v, k);
                    try {
                        setter.invoke(entity, val);
                    } catch (Exception e) {
                        throw new UnsupportedOperationException("Cannot call setter "
                                + setter.getName() + " on entity of type " + type, e);
                    }
                }
            });

            return entity;

        } catch (Throwable t) {
            throw new AssertionError("Cannot populate test entity of type " + type, t);
        }
    }


    private <T> T loadPattern(Class<T> type, Map<String, String> m, String replace) {

        try {
            // Default constructor call
            T entity = type.getConstructor().newInstance();

            // Search setters
            Map<String, Method> setters = loadSetters(type);

            // apply all setters
            m.forEach((k, v) -> {
                // Ignore change
                if (!k.equals("change")) {
                    Method setter = setters.get(k);
                    if (setter == null) {
                        throw new UnsupportedOperationException("The column " + k
                                + " has no corresponding setter in type " + type
                                + ": add it as exclude type in fr.uem.efluid.cucumber.stubs." +
                                "ManagedDatabaseAccess.load or check datatable");
                    }

                    Object val = getValForSetter(setter, v.replaceAll("%%", replace), k);
                    try {
                        setter.invoke(entity, val);
                    } catch (Exception e) {
                        throw new UnsupportedOperationException("Cannot call setter "
                                + setter.getName() + " on entity of type " + type, e);
                    }
                }
            });

            return entity;

        } catch (Throwable t) {
            throw new AssertionError("Cannot populate test entity of type " + type, t);
        }
    }

    private Object getValForSetter(Method setter, String rawVal, String name) {
        Class<?> paramType = setter.getParameterTypes()[0];
        return getMappedValue(paramType, rawVal, name);
    }

    private static Map<String, Method> loadSetters(Class<?> type) {
        return Stream.of(type.getMethods())
                .filter(met -> met.getName().startsWith("set"))
                .collect(Collectors.toMap(met -> {
                    String name = met.getName();
                    return name.substring(3, 4).toLowerCase() + name.substring(4);
                }, met -> met));
    }

    /**
     * For a value from a datatable, get the corresponding object of specified type. Can be a basic field type, or another stub type
     */
    private Object getMappedValue(Class<?> paramType, String v, String name) {

        // Forced null
        if ("-null-".equals(v)) {
            return null;
        }

        // Keep string
        if (paramType == String.class) {
            return v;
        }

        // Remove empty
        if (!StringUtils.hasText(v)) {
            return null;
        }

        // Process by type
        if (paramType == LocalDate.class) {
            return FormatUtils.parseLd(v);
        } else if (paramType == LocalDateTime.class) {
            return FormatUtils.parse(v);
        } else if (paramType == BigDecimal.class) {
            return new BigDecimal(v);
        } else if (paramType == Integer.class) {
            return Integer.valueOf(v);
        } else if (paramType == byte[].class) {
            return FormatUtils.toBytes(v);
        } else if (paramType == Long.class) {
            return Long.valueOf(v);
        } else if (paramType == int.class) {
            return Integer.parseInt(v);
        } else if (paramType == boolean.class) {
            return Boolean.parseBoolean(v);
        } else if (paramType.getPackageName().startsWith(this.getClass().getPackageName())) {

            // The attribute is another stub type, must find constructor with 1 (key) arg
            Constructor<?> inCons = Stream.of(paramType.getConstructors())
                    .filter(c -> c.getParameterCount() == 1)
                    .findFirst()
                    .orElseThrow(() -> new UnsupportedOperationException("Cannot init inner type \""
                            + paramType + "\" for field \"" + name + "\" : need a single key constructor"));

            Class<?> conArg = inCons.getParameterTypes()[0];
            try {
                return inCons.newInstance(getMappedValue(conArg, v, name));
            } catch (Exception e) {
                throw new UnsupportedOperationException("Cannot init inner type \"" + paramType.getName()
                        + "\" with value \"" + v + "\" of type \"" + conArg.getName() + "\" for field \"" + name + "\"", e);
            }
        } else {
            throw new UnsupportedOperationException("Cannot process field of type \"" + paramType.getName()
                    + "\" for field \"" + name + "\" : need to add a clean mapper code in ManagedDatabaseAccess");
        }
    }


    private <T> void assertComplies(String firstCol, List<Map<String, String>> values, Class<T> type) {
        assertThat(findAll(type).size()).isEqualTo(values.size());

        Map<String, Method> setters = loadSetters(type);

        values.forEach(m -> {
            Object keyVal = getValForSetter(setters.get(firstCol), m.get(firstCol), firstCol);
            T expected = load(type, m);
            T found = this.getOne(keyVal, type);
            assertThat(found).isNotNull();
            assertThat(found).isEqualTo(expected);
        });
    }

    private <T> void initHeavy(int count, Map<String, String> m, Class<T> type) {

        for (int i = 0; i < count; i++) {
            save(loadPattern(type, m, String.valueOf(i + 1)));
        }
    }

    private <T> void update(String action, String firstCol, Map<String, String> m, Class<T> type) {

        Map<String, Method> setters;
        Object keyVal;
        T entity;
        switch (action) {
            case "add":
                save(load(type, m));
                break;
            case "update":

                // Search setters
                setters = loadSetters(type);

                // Prepare key val (search type from setter)
                keyVal = getValForSetter(setters.get(firstCol), m.get(firstCol), firstCol);

                // Get existing entry
                entity = getOne(keyVal, type);

                assertThat(entity).describedAs("Updated entity of type " + type + " for key " + keyVal + " doesn't exist!").isNotNull();

                // Call setters on entry (except on key) to update it
                setters.entrySet().stream()
                        .filter(s -> !s.getKey().equals(firstCol))
                        .forEach(e -> {
                            Method setter = setters.get(e.getKey());
                            Object val = getValForSetter(setter, m.get(e.getKey()), e.getKey());
                            try {
                                setter.invoke(entity, val);
                            } catch (Exception ex) {
                                throw new UnsupportedOperationException("Cannot call setter "
                                        + setter.getName() + " on entity of type " + type, ex);
                            }
                        });

                // Save updated
                this.save(entity);
                break;
            case "delete":
            default:
                // Search setters
                setters = loadSetters(type);

                // Prepare key val (search type from setter)
                keyVal = getValForSetter(setters.get(firstCol), m.get(firstCol), firstCol);

                // Get existing entry
                entity = getOne(keyVal, type);

                // Delete it
                this.deleteOne(entity);
                break;
        }
    }

    /**
     * Init for TABLE 2
     */
    public void initTabTwoData(int nbr, String keyPattern, String valuePattern, String otherPattern) {

        for (int i = 0; i < nbr; i++) {
            this.save(two(keyPattern + i, valuePattern + i, otherPattern + i));
        }
    }

    /**
     * Init for TABLE 1
     */
    public void initTabOneData(int nbr, String presetPattern, String somethingPattern, String valuePattern) {

        for (int i = 0; i < nbr; i++) {
            this.save(one(i, presetPattern + i, somethingPattern + i, valuePattern + i));
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

    /**
     * <p>
     * Matcher spec for the columns for TABLE_ONE. Allows to check Column / ColumnEditData
     * content in an assertion
     * </p>
     *
     * @return names in a list
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
    private <T> List<T> getAllEntitiesForTable(String tableName) {

        Class<?> type = ENTITY_TYPES.get(tableName).getSecond();

        return (List<T>) findAll(type);
    }

    /**
     * @param tableName specified table to process
     * @return entities count for specified table
     */
    public long countTable(String tableName) {
        return getAllEntitiesForTable(tableName).size();
    }

    /**
     * <p>
     * Get easy to use content for Tab 1
     * </p>
     *
     * @param tableName specified table to process
     * @return content for specified table, as a list of maps (similar to <code>DataTable.toMaps()</code>)
     */
    public List<Map<String, String>> getAllContentForTable(String tableName) {

        List<String> cols = getColumnNamesForTable(tableName);

        return getAllEntitiesForTable(tableName).stream().map(i -> {
            Map<String, String> vals = new HashMap<>();
            cols.forEach(c -> vals.put(c, getPropertyValueByColumnName(i, c)));
            return vals;
        })
                .sorted(comparing(m -> m.get("KEY")))
                .collect(Collectors.toList());
    }

    /**
     * <p>
     * Matcher spec for the columns for TABLE_ONE. Allows to check Column / ColumnEditData
     * content in an assertion
     * </p>
     *
     * @return matchers for TABLE_ONE
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
     * @return matchers for TABLE_TWO
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
     * @return matchers for TABLE_THREE
     */
    public List<Matcher<DictionaryEntryEditData.ColumnEditData>> getColumnMatchersForTableThree() {
        return Arrays.asList(
                columnMatcher("KEY", ColumnType.PK_STRING),
                columnMatcher("VALUE", ColumnType.STRING),
                columnMatcher("OTHER", ColumnType.STRING));
    }

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

    private static Matcher<DictionaryEntryEditData.ColumnEditData> columnMatcher(String name, ColumnType type) {
        return allOf(hasProperty("name", equalTo(name)), hasProperty("type", equalTo(type)));
    }
}
