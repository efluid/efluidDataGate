package fr.uem.efluid.model.metas;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.8
 */
public class ManagedModelDescription {

    private final String identity;

    private final LocalDateTime updatedTime;

    private final String details;

    private final String schema;

    /**
     * @param identity
     * @param updatedTime
     * @param details
     */
    public ManagedModelDescription(String identity, LocalDateTime updatedTime, String details, String schema) {
        super();
        this.identity = identity;
        this.updatedTime = updatedTime;
        this.details = details;
        this.schema = schema;
    }

    /**
     * @return the identity
     */
    public String getIdentity() {
        return this.identity;
    }

    /**
     * @return the updatedTime
     */
    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
    }

    /**
     * @return the details
     */
    public String getDetails() {
        return this.details;
    }

    public String getSchema() {
        return this.schema;
    }
}
