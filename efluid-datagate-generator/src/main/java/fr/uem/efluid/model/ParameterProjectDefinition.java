package fr.uem.efluid.model;

import fr.uem.efluid.ParameterProject;
import fr.uem.efluid.ProjectColor;
import fr.uem.efluid.model.shared.ExportAwareProject;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.0.1
 */
@SpecifiedWith(ParameterProject.class)
public class ParameterProjectDefinition extends ExportAwareProject {

    public static final String DEFAULT_PROJECT = "Default";

    private UUID uuid;

    private String name;

    private LocalDateTime createdTime;

    private ProjectColor color;

    /**
     *
     */
    public ParameterProjectDefinition() {
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
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {
        // Not implemented
    }

    /**
     * @return the color
     */
    @Override
    public int getColor() {
        return this.color == null ? 0 : this.color.ordinal();
    }

    /**
     * @param color the color to set
     */
    public void setColor(ProjectColor color) {
        this.color = color;
    }
}
