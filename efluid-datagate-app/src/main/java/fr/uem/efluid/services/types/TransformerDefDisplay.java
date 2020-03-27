package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.TransformerDef;

import java.time.LocalDateTime;
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

    private final String typeName;

    private final int priority;

    private final LocalDateTime updatedTime;

    public TransformerDefDisplay(TransformerDef transformerDef, String transformerName) {
        super();
        this.uuid = transformerDef.getUuid();
        this.name = transformerDef.getName();
        this.typeName = transformerName;
        this.priority = transformerDef.getPriority();
        this.updatedTime = transformerDef.getUpdatedTime();
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
}
