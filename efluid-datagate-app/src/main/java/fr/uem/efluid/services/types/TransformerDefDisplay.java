package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.TransformerDef;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Rendering of a transformer def for listing
 *
 * @author elecomte
 * @version 1
 * @since v1.2.0
 */
public class TransformerDefDisplay {

    private final UUID uuid;

    private final String name;

    private final String type;

    private final String typeName;

    private final int priority;

    private final LocalDateTime updatedTime;

    public TransformerDefDisplay(TransformerDef transformerDef, String transformerName) {
        super();
        this.uuid = transformerDef.getUuid();
        this.name = transformerDef.getName();
        this.type = transformerDef.getType();
        this.typeName = transformerName;
        this.priority = transformerDef.getPriority();
        this.updatedTime = transformerDef.getUpdatedTime();
    }

    public String getType() {
        return type;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getName() {
        return this.name;
    }

    public int getPriority() {
        return this.priority;
    }

    public String getTypeName() {
        return this.typeName;
    }

    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformerDefDisplay that = (TransformerDefDisplay) o;
        return priority == that.priority &&
                uuid.equals(that.uuid) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, priority);
    }
}
