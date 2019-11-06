package fr.uem.efluid.utils.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

/**
 * <p>
 * JPA Converter for new LocalDate type. Will use a standard <tt>java.sql.Date</tt> to map
 * the new java.time date type.
 * </p>
 * <p>
 * Enabled by default for all entities using <tt>LocalDate</tt> attributes
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@Converter(autoApply = true)
public class LocalDateConverter implements AttributeConverter<LocalDate, Date> {

    /**
     * Mapper to DB
     *
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public Date convertToDatabaseColumn(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    /**
     * Mapper to entity
     *
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public LocalDate convertToEntityAttribute(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}