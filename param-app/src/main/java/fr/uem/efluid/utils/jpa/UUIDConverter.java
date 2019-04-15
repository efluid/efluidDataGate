package fr.uem.efluid.utils.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.UUID;

@Converter(autoApply = true)
public class UUIDConverter implements AttributeConverter<UUID, String> {

    /**
     * Mapper to DB
     *
     * @param value
     * @return
     * @see javax.persistence.AttributeConverter#convertToDatabaseColumn(java.lang.Object)
     */
    @Override
    public String convertToDatabaseColumn(UUID value) {
        return value == null ? null : value.toString();
    }

    /**
     * Mapper to entity
     *
     * @param value
     * @return
     * @see javax.persistence.AttributeConverter#convertToEntityAttribute(java.lang.Object)
     */
    @Override
    public UUID convertToEntityAttribute(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}