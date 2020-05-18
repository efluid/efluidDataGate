package fr.uem.efluid.rest.v1.model;

import java.time.LocalDateTime;

/**
 * @author elecomte
 * @version 1
 * @since v0.2.0
 */
public class VersionView {

    private String name;

    private String modelId;

    private LocalDateTime updatedTime;

    private boolean canUpdate;

    public VersionView() {
    }

    /**
     * @param name
     * @param modelId
     * @param updatedTime
     * @param canUpdate
     */
    public VersionView(String name, String modelId, LocalDateTime updatedTime, boolean canUpdate) {
        super();
        this.name = name;
        this.modelId = modelId;
        this.updatedTime = updatedTime;
        this.canUpdate = canUpdate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public void setCanUpdate(boolean canUpdate) {
        this.canUpdate = canUpdate;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the modelId
     */
    public String getModelId() {
        return this.modelId;
    }

    /**
     * @return the updatedTime
     */
    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
    }

    /**
     * @return
     */
    public boolean isCanUpdate() {
        return this.canUpdate;
    }

}
