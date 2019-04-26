package fr.uem.efluid.rest.v1.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Details on active async process
 *
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
public class AsyncProcessView {

    private UUID identifier;

    private String description;

    private LocalDateTime createdTime;

    private boolean sourceError;

    private int percentDone;

    public AsyncProcessView() {
        super();
    }

    public AsyncProcessView(UUID identifier, String description, LocalDateTime createdTime, boolean sourceError, int percentDone) {
        this.identifier = identifier;
        this.description = description;
        this.createdTime = createdTime;
        this.sourceError = sourceError;
        this.percentDone = percentDone;
    }

    public int getPercentDone() {
        return percentDone;
    }

    public void setPercentDone(int percentDone) {
        this.percentDone = percentDone;
    }

    public UUID getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(UUID identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public boolean isSourceError() {
        return this.sourceError;
    }

    public void setSourceError(boolean sourceError) {
        this.sourceError = sourceError;
    }
}
