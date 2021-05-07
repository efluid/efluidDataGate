package fr.uem.efluid.model;

import fr.uem.efluid.ParameterDomain;
import fr.uem.efluid.model.shared.ExportAwareVersion;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@SpecifiedWith(ParameterDomain.class)
public class ParameterVersionDefinition extends ExportAwareVersion<ParameterProjectDefinition> {

    private UUID uuid;

    private String name;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    private ParameterProjectDefinition project;

    private String modelIdentity;

    /**
     *
     */
    public ParameterVersionDefinition() {
        super();
    }

    /**
     * @return the uuid
     */
    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the createdTime
     */
    @Override
    public LocalDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * @param createdTime the createdTime to set
     */
    @Override
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * @return the importedTime
     */
    @Override
    public LocalDateTime getImportedTime() {
        return null;
    }

    /**
     * @return the updatedTime
     */
    @Override
    public LocalDateTime getUpdatedTime() {
        return this.updatedTime;
    }

    /**
     * @param updatedTime the updatedTime to set
     */
    @Override
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public void setImportedTime(LocalDateTime time) {
        // Nothing
    }

    /**
     * @return the project
     */
    @Override
    public ParameterProjectDefinition getProject() {
        return this.project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(ParameterProjectDefinition project) {
        this.project = project;
    }

    /**
     * @return the modelIdentity
     */
    @Override
    public String getModelIdentity() {
        return this.modelIdentity;
    }

    /**
     * @param modelIdentity the modelIdentity to set
     */
    public void setModelIdentity(String modelIdentity) {
        this.modelIdentity = modelIdentity;
    }

    /**
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {
        // Not implemented
    }
}
