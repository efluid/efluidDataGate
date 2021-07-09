package fr.uem.efluid.stubs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import fr.uem.efluid.ColumnType;

import fr.uem.efluid.services.types.ExportFile;
import fr.uem.efluid.services.types.Value;
import fr.uem.efluid.tools.diff.ManagedValueConverter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class TestUtils {

    public static final String SOURCE_TABLE_NAME = "TTESTSOURCE";
    public static final String SOURCE_CHILD_TABLE_NAME = "TTESTSOURCE_CHILD";
    private static final String CSV_SEP = ";";
    private static final String NOT_STRING_IDENTIFIER = "(not string)";

    /**
     * @param path
     * @return
     */
    public static Map<String, String> readDataset(String path, final ManagedValueConverter converter) {
        List<String> list = new ArrayList<>();

        try (InputStream is = new FileInputStream(new File("src/test/resources/datasets/" + path))) {
            try (Scanner s = new Scanner(is)) {

                while (s.hasNextLine()) {
                    list.add(s.nextLine().trim());
                }
            }
        } catch (IOException e) {
            throw new AssertionError("Cannot process content of dataset " + path, e);
        }

        String[] headers = list.get(0).split(CSV_SEP);

        return list.stream().skip(1).map(l -> l.split(CSV_SEP))
                .collect(Collectors.toMap(t -> t[0], t -> simulateInternalValue(headers, t, converter)));
    }

    /**
     * @param model
     * @param factor
     * @return
     */
    public static Map<String, String> multiplyDataset(Map<String, String> model, int factor) {

        // Ignore multiply
        if (factor <= 1) {
            return model;
        }

        Map<String, String> multi = new HashMap<>();

        // Assumes key is a Long stored as string
        long max = model.keySet().stream().mapToLong(Long::decode).max().getAsLong();

        for (int i = 0; i < max * factor; i++) {
            multi.put(String.valueOf(i), getAny(model));
        }

        return multi;
    }

    /**
     * @param model
     * @return
     */
    public static <V> V getAny(Map<String, V> model) {
        V val = model.get(Long.toString(ThreadLocalRandom.current().nextInt(1, model.entrySet().size())));

        // If none at this key, try again ...
        if (val == null) {
            return getAny(model);
        }
        return val;
    }

    /**
     * @param datasetEntry
     * @return
     */
    public static SimulatedSource entryToSource(Map.Entry<String, String> datasetEntry, final ManagedValueConverter converter) {
        // Key;Value;Preset;Something
        SimulatedSource source = new SimulatedSource();
        source.setKey(Long.decode(datasetEntry.getKey()));

        Map<String, String> values = converter.expandInternalValue(datasetEntry.getValue()).stream()
                .collect(Collectors.toMap(Value::getName, Value::getValueAsString));

        source.setValue(values.get("VALUE"));
        source.setPreset(values.get("PRESET"));
        source.setSomething(values.get("SOMETHING"));
        return source;
    }

    /**
     * @param file
     * @param name
     */
    public static void writeExportFile(ExportFile file, String name) {

        try {
            Path path = new File("target/" + name).toPath();
            Files.write(path, file.getData());
        } catch (IOException e) {
            fail("Cannot export to " + name);
        }
    }

    /**
     * @param datasetEntry
     * @return
     */
    public static SimulatedSourceChild entryToSourceChild(Map.Entry<String, String> datasetEntry, final ManagedValueConverter converter) {
        // Key;Value;Parent
        SimulatedSourceChild child = new SimulatedSourceChild();
        child.setKey(Long.decode(datasetEntry.getKey()));

        Map<String, String> values = converter.expandInternalValue(datasetEntry.getValue()).stream()
                .collect(Collectors.toMap(Value::getName, Value::getValueAsString));

        child.setValue(values.get("VALUE"));
        child.setParent(new SimulatedSource(Long.decode(values.get("PARENT"))));
        return child;
    }

    /**
     * @param datasToCompare
     * @param dataset
     */
    public static void assertDatasetEquals(Map<String, String> datasToCompare, String dataset, final ManagedValueConverter converter) {
        Map<String, String> expecteds = readDataset(dataset, converter);

        // Not expected size : display global diff
        if (datasToCompare.size() != expecteds.size()) {
            int realSize = datasToCompare.size();
            List<String> missing = expecteds.keySet().stream()
                    .filter(e -> datasToCompare.remove(e) == null)
                    .collect(Collectors.toList());
            List<String> added = datasToCompare.keySet().stream().collect(Collectors.toList());
            throw new AssertionError("Dataset is not of the expected size. " + realSize + " items are present while " + expecteds.size()
                    + " items are expected. New ones are with keys " + added + ", removed ones are with keys " + missing);
        }

        for (Map.Entry<String, String> expected : expecteds.entrySet()) {
            String data = datasToCompare.get(expected.getKey());
            assertNotNull(data, "The expected key \"" + expected.getKey() + "\" is not found in data");
            assertEquals(expected.getValue(), data, "For the key \"" + expected.getKey() + "\", values are different");
        }
    }

    /**
     * @param headers
     * @param csvLine
     * @return
     */
    private static String simulateInternalValue(String[] headers, String[] csvLine, final ManagedValueConverter converter) {

        // Keep insertion order
        LinkedHashMap<String, Object> lineContent = new LinkedHashMap<>();

        // All except key
        for (int i = 1; i < headers.length; i++) {
            boolean isString = !headers[i].endsWith(NOT_STRING_IDENTIFIER);
            String header = isString ? headers[i] : headers[i].substring(0, headers[i].length() - NOT_STRING_IDENTIFIER.length());
            lineContent.put(header, isString ? csvLine[i] : Long.decode(csvLine[i]));
        }

        return converter.convertToExtractedValue(lineContent);
    }

    public static Value value(String name, Object value) {

        final ColumnType type;

        if (value == null) {
            type = ColumnType.NULL;
        } else if (value instanceof String) {
            type = ColumnType.STRING;
        } else if (value instanceof LocalDate) {
            type = ColumnType.TEMPORAL;
        } else {
            type = ColumnType.ATOMIC;
        }

        return new Value() {
            @Override
            public String getName() {
                return name;
            }

            @Override
            public byte[] getValue() {
                return value != null ? value.toString().getBytes(StandardCharsets.UTF_8) : null;
            }

            @Override
            public ColumnType getType() {
                return type;
            }
        };
    }
}
