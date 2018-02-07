package fr.uem.efluid.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import fr.uem.efluid.utils.json.LocalDateModule;
import fr.uem.efluid.utils.json.LocalDateTimeModule;

/**
 * <p>
 * Helper with a very basic data model for processing input and output of <tt>Shared</tt>
 * as basic String values
 * </p>
 * <p>
 * Input/output process can be seen as similare to a serialization process
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
public class SharedOutputInputUtils {

	static final ObjectMapper MAPPER = preparedObjectMapper();

	/**
	 * @param properties
	 * @return
	 */
	public static JsonPropertiesWriter newJson() {
		return new JsonPropertiesWriter();
	}

	/**
	 * @param raw
	 * @return
	 */
	public static OutputJsonPropertiesReader fromJson(String raw) {
		return new OutputJsonPropertiesReader(raw);
	}

	/**
	 * @return prepared Jackson mapper for JSON production
	 */
	private static ObjectMapper preparedObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();

		// LocalDate As formated String (ex : 2015-03-19)
		objectMapper.registerModule(new LocalDateModule());

		// LocalDateTime As formated String (ex : 2015-03-19 08:56)
		objectMapper.registerModule(new LocalDateTimeModule());

		// Exclude empty values
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

		// Allows empty
		objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		return objectMapper;
	}

	/**
	 * Chained writter
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class JsonPropertiesWriter {

		private final Map<String, Object> properties = new HashMap<>();

		/**
		 * @return the properties
		 */
		Map<String, Object> getProperties() {
			return this.properties;
		}

		public JsonPropertiesWriter with(String key, Object value) {
			this.properties.put(key, value);
			return this;
		}

		@Override
		public String toString() {
			try {
				return MAPPER.writeValueAsString(this.properties);
			} catch (JsonProcessingException e) {
				throw new TechnicalException("Cannot serialize to json", e);
			}
		}
	}

	/**
	 * Chained reader (use consumer mode)
	 * 
	 * @author elecomte
	 * @since v0.0.1
	 * @version 1
	 */
	public static class OutputJsonPropertiesReader {

		private final Map<String, Object> jsonProperties;

		@SuppressWarnings("unchecked")
		OutputJsonPropertiesReader(String raw) {
			try {
				this.jsonProperties = MAPPER.readValue(raw, Map.class);
			} catch (IOException e) {
				throw new TechnicalException("Cannot deserialize from json", e);
			}
		}

		/**
		 * @param name
		 * @param type
		 * @return
		 */
		public <T> T getProperty(String name, Class<T> type) {
			Object jsonProperty = this.jsonProperties.get(name);
			if (jsonProperty == null) {
				return null;
			}
			try {
				return MAPPER.readValue(jsonProperty.toString(), type);
			} catch (IOException e) {
				throw new TechnicalException("Cannot deserialize from json", e);
			}
		}

		/**
		 * @param name
		 * @param type
		 * @param apply
		 * @return
		 */
		public <T> OutputJsonPropertiesReader apply(String name, Class<T> type, Consumer<T> apply) {
			T prop = getProperty(name, type);
			apply.accept(prop);
			return this;
		}

	}
}
