package fr.uem.efluid.services.types;

import fr.uem.efluid.model.entities.Version;
import fr.uem.efluid.rest.v1.model.VersionView;

import java.time.LocalDateTime;

/**
 * <p>
 * DTO for version info
 * </p>
 *
 * @author elecomte
 * @version 1
 * @since v0.2.0
 */
public class VersionData {

    private final String name;

    private final String modelId;

    private final LocalDateTime createdTime;

    private final LocalDateTime updatedTime;

    private final boolean currentVersion;

    private boolean canUpdate;

    /**
     * @param name
     * @param createdTime
     * @param updatedTime
     */
    public VersionData(String name, String modelId, LocalDateTime createdTime, LocalDateTime updatedTime, boolean currentVersion, boolean canUpdate) {
        super();
        this.name = name;
        this.modelId = modelId;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.currentVersion = currentVersion;
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
     * @return the createdTime
     */
    public LocalDateTime getCreatedTime() {
        return this.createdTime;
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

    /**
     * @return true if it's the active version of app (for filtering in a list for example)
     */
    public boolean isCurrentVersion() {
        return this.currentVersion;
    }

    /**
     * @param version
     * @return
     */
    public static VersionData fromEntity(Version version, boolean currentVersion, boolean canUpdate) {
        return new VersionData(
                version.getName(),
                version.getModelIdentity() != null ? version.getModelIdentity() : " n/a ",
                version.getCreatedTime(),
                version.getUpdatedTime(),
                currentVersion,
                canUpdate);
    }

    /**
     * @param data
     * @return
     */
    public static VersionView toView(VersionData data) {
        return data != null ? new VersionView(data.getName(), data.getModelId(), data.getUpdatedTime(), data.isCanUpdate()) : null;
    }
}
