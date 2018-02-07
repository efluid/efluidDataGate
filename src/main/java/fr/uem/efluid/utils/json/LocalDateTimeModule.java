package fr.uem.efluid.utils.json;

import java.io.IOException;
import java.time.LocalDateTime;
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
public class LocalDateTimeModule extends SimpleModule {

	public LocalDateTimeModule() {
		super();
		addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
		addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
	}

	/**
	 * @author elecomte
	 * @since 0.1.0
	 */
	public static class LocalDateTimeDeserializer extends StdScalarDeserializer<LocalDateTime> {

		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestConstants.DATE_TIME_FORMAT);

		/**
		 * 
		 */
		public LocalDateTimeDeserializer() {
			super(LocalDateTime.class);
		}

		@Override
		public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
				throws IOException, JsonProcessingException {

			JsonToken currentToken = jsonParser.getCurrentToken();

			if (currentToken == JsonToken.VALUE_STRING) {
				return LocalDateTime.parse(jsonParser.getText().trim(), this.formatter);
			}

			return null;
		}
	}

	/**
	 * @author elecomte
	 * @since 0.1.0
	 */
	public static class LocalDateTimeSerializer extends StdScalarSerializer<LocalDateTime> {

		private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(RestConstants.DATE_TIME_FORMAT);

		/**
		 * 
		 */
		public LocalDateTimeSerializer() {
			super(LocalDateTime.class);
		}

		@Override
		public void serialize(LocalDateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider provider)
				throws IOException, JsonGenerationException {

			jsonGenerator.writeString(dateTime.format(this.formatter));
		}
	}
}