package fr.uem.efluid.model.entities;

import fr.uem.efluid.model.shared.ExportAwareProject;
import fr.uem.efluid.utils.SharedOutputInputUtils;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author elecomte
 * @version 1
 * @since v0.2.0
 */
@Entity
@Table(name = "projects")
public class Project extends ExportAwareProject {

    @Id
    @Type(type = "uuid-char")
    private UUID uuid;

    @NotNull
    private String name;

    private int color;

    @NotNull
    private LocalDateTime createdTime;

    private LocalDateTime importedTime;

    /**
     * @param uuid forced uuid
     */
    public Project(UUID uuid) {
        super();
        this.uuid = uuid;
    }

    /**
     *
     */
    public Project() {
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
        return this.importedTime;
    }

    /**
     * @param importedTime the importedTime to set
     */
    public void setImportedTime(LocalDateTime importedTime) {
        this.importedTime = importedTime;
    }

    /**
     * @return the color
     */
    @Override
    public int getColor() {
        return this.color;
    }

    /**
     * @param color the color to set
     */
    public void setColor(int color) {
        this.color = color;
    }

    /**
     * @see fr.uem.efluid.model.Shared#deserialize(java.lang.String)
     */
    @Override
    public void deserialize(String raw) {

        SharedOutputInputUtils.fromJson(raw)
                .applyUUID("uid", this::setUuid)
                .applyLdt("cre", this::setCreatedTime)
                .applyString("nam", this::setName)
                .applyInt("col", this::setColor);
    }

}
