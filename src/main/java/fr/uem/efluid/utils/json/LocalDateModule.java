package fr.uem.efluid.utils.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import fr.uem.efluid.utils.RestConstants;

/**
 * @author elecomte
 * @since 0.1.0
 */
public class LocalDateModule extends SimpleModule {

	static final String DATE_FORMAT = RestConstants.DATE_FORMAT;

	/**
	 * 
	 */
	public LocalDateModule() {
		super();
		addSerializer(LocalDate.class, new LocalDateSerializer());
		addDeserializer(LocalDate.class, new LocalDateDeserializer());
	}

	/**
	 * @author elecomte
	 * @since 0.1.0
	 */
	public static class LocalDateDeserializer extends StdScalarDeserializer<LocalDate> {

		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

		/**
		 * 
		 */
		public LocalDateDeserializer() {
			super(LocalDate.class);
		}

		/**
		 * @param jsonParser
		 * @param deserializationContext
		 * @return
		 * @throws IOException
		 * @throws JsonProcessingException
		 * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
		 *      com.fasterxml.jackson.databind.DeserializationContext)
		 */
		@Override
		public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {

			JsonToken currentToken = jsonParser.getCurrentToken();

			if (currentToken == JsonToken.VALUE_STRING) {
				return LocalDate.parse(jsonParser.getText().trim(), this.formatter);
			}

			return null;
		}
	}

	/**
	 * @author elecomte
	 * @since 0.1.0
	 */
	public static class LocalDateSerializer extends StdScalarSerializer<LocalDate> {

		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

		/**
		 * 
		 */
		public LocalDateSerializer() {
			super(LocalDate.class);
		}

		/**
		 * @param dateTime
		 * @param jsonGenerator
		 * @param provider
		 * @throws IOException
		 * @throws JsonGenerationException
		 * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object,
		 *      com.fasterxml.jackson.core.JsonGenerator,
		 *      com.fasterxml.jackson.databind.SerializerProvider)
		 */
		@Override
		public void serialize(LocalDate date, JsonGenerator jsonGenerator, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			jsonGenerator.writeString(date.format(this.formatter));
		}
	}
}