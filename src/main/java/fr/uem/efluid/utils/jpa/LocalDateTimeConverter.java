package fr.uem.efluid.utils.jpa;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <p>
 * JPA Converter for new LocalDateTime type. Will use a standard
 * <tt>java.sql.Timestamp</tt> to map the new java.time date type.
 * </p>
 * <p>
 * Enabled by default for all entities using <tt>LocalDateTime</tt> attributes
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

	/**
	 * Mapper to DB
	 * 
	 * @param value
	 * @return
	 * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
	 */
	@Override
	public java.sql.Timestamp convertToDatabaseColumn(LocalDateTime value) {
		return value == null ? null : Timestamp.valueOf(value);
	}

	/**
	 * Mapper to entity
	 * 
	 * @param value
	 * @return
	 * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public LocalDateTime convertToEntityAttribute(Timestamp value) {
		return value == null ? null : value.toLocalDateTime();
	}
}