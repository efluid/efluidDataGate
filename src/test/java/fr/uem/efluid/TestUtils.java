package fr.uem.efluid;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import fr.uem.efluid.utils.ManagedDiffUtils;

/**
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class TestUtils {

	private static final String CSV_SEP = ";";
	private static final String NOT_STRING_IDENTIFIER = "(not string)";

	/**
	 * @param path
	 * @return
	 */
	public static Map<String, String> readDataset(String path) {
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
				.collect(Collectors.toMap(t -> t[0], t -> simulateInternalValue(headers, t)));
	}

	/**
	 * @param model
	 * @param factor
	 * @return
	 */
	public static Map<String, String> multiplyDataset(Map<String, String> model, int factor) {

		Map<String, String> multi = new HashMap<>();

		// Assumes key is a Long stored as string
		long max = model.keySet().stream().mapToLong(Long::decode).max().getAsLong();

		for (int i = 0; i < max * factor; i++) {
			multi.put(String.valueOf(i), getAny(model));
		}

		return multi;
	}

	public static <V> V getAny(Map<String, V> model) {
		V val = model.get(Long.toString(ThreadLocalRandom.current().nextInt(1, model.entrySet().size())));

		// If none at this key, try again ...
		if (val == null) {
			return getAny(model);
		}
		return val;
	}

	/**
	 * @param headers
	 * @param csvLine
	 * @return
	 */
	private static String simulateInternalValue(String[] headers, String[] csvLine) {

		// Keep insertion order
		LinkedHashMap<String, Object> lineContent = new LinkedHashMap<>();

		// All except key
		for (int i = 1; i < headers.length; i++) {
			boolean isString = !headers[i].endsWith(NOT_STRING_IDENTIFIER);
			String header = isString ? headers[i] : headers[i].substring(0, headers[i].length() - NOT_STRING_IDENTIFIER.length());
			lineContent.put(header, isString ? csvLine[i] : Long.decode(csvLine[i]));
		}

		return ManagedDiffUtils.convertToExtractedValue(lineContent);
	}
}
