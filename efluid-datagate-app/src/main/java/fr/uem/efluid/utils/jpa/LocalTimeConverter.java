package fr.uem.efluid.utils.jpa;

import java.sql.Time;
import java.time.LocalTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * <p>
 * JPA Converter for new LocalTime type (hh:mm). Will use a standard
 * <tt>java.sql.Time</tt> to map the new java.time date type.
 * </p>
 * <p>
 * Enabled by default for all entities using <tt>LocalTime</tt> attributes
 * </p>
 * 
 * @author elecomte
 * @since v0.0.1
 * @version 1
 */
@Converter(autoApply = true)
public class LocalTimeConverter implements AttributeConverter<LocalTime, Time> {

	/**
	 * Mapper to DB
	 * 
	 * @param value
	 * @return
	 * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
	 */
	@Override
	public Time convertToDatabaseColumn(LocalTime value) {
		return value == null ? null : Time.valueOf(value);
	}

	/**
	 * Mapper to entity
	 * 
	 * @param value
	 * @return
	 * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
	 */
	@Override
	public LocalTime convertToEntityAttribute(Time value) {
		return value == null ? null : value.toLocalTime();
	}
}